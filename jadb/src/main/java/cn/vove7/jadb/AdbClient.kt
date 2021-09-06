package cn.vove7.jadb

import android.content.Context
import android.os.Build
import android.util.Log
import android.util.SparseArray
import androidx.core.util.containsKey
import androidx.core.util.forEach
import androidx.core.util.keyIterator
import androidx.core.util.set
import androidx.viewbinding.BuildConfig
import cn.vove7.jadb.AdbProtocol.ADB_AUTH_RSAPUBLICKEY
import cn.vove7.jadb.AdbProtocol.ADB_AUTH_SIGNATURE
import cn.vove7.jadb.AdbProtocol.ADB_AUTH_TOKEN
import cn.vove7.jadb.AdbProtocol.A_AUTH
import cn.vove7.jadb.AdbProtocol.A_CLSE
import cn.vove7.jadb.AdbProtocol.A_CNXN
import cn.vove7.jadb.AdbProtocol.A_MAXDATA
import cn.vove7.jadb.AdbProtocol.A_OKAY
import cn.vove7.jadb.AdbProtocol.A_OPEN
import cn.vove7.jadb.AdbProtocol.A_STLS
import cn.vove7.jadb.AdbProtocol.A_STLS_VERSION
import cn.vove7.jadb.AdbProtocol.A_VERSION
import cn.vove7.jadb.AdbProtocol.A_WRTE
import java.io.Closeable
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.net.Inet4Address
import java.net.Socket
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.net.ssl.SSLSocket
import kotlin.concurrent.thread

private const val TAG = "AdbClient"

typealias OnClose = AdbStream.() -> Unit
typealias OnData = (ByteArray) -> Unit

open class AdbClient(
    private val context: Context,
    private val host: String = Inet4Address.getLoopbackAddress().hostName,
    private val port: Int = 5555,
    private val adbCrypto: AdbCrypto = AdbCrypto.get(context),
    private val name: String = "VAssistant"
) : Closeable {
    private var _localId = 1
    private lateinit var socket: Socket
    private lateinit var plainInputStream: DataInputStream
    private lateinit var plainOutputStream: DataOutputStream

    private var useTls = false

    private val streams by lazy {
        SparseArray<AdbStream>()
    }

    private lateinit var tlsSocket: SSLSocket
    private lateinit var tlsInputStream: DataInputStream
    private lateinit var tlsOutputStream: DataOutputStream

    private val inputStream get() = if (useTls) tlsInputStream else plainInputStream
    private val outputStream get() = if (useTls) tlsOutputStream else plainOutputStream

    private var connected = false

    private var _loopThread: Thread? = null

    fun connect(timeout: Long = 10000) {
        socket = Socket(host, port)
        socket.tcpNoDelay = true
        plainInputStream = DataInputStream(socket.getInputStream())
        plainOutputStream = DataOutputStream(socket.getOutputStream())

        write(A_CNXN, A_VERSION, A_MAXDATA, "host::")

        val adbKey by lazy { AdbKey.get(context) }

        var message = read()
        if (message.command == A_STLS) {
            if (Build.VERSION.SDK_INT < 29) {
                error("Connect to adb with TLS is not supported before Android 9")
            }
            write(A_STLS, A_STLS_VERSION, 0)

            tlsSocket =
                adbKey.sslContext.socketFactory.createSocket(socket, host, port, true) as SSLSocket
            tlsSocket.startHandshake()
            Log.d(TAG, "Handshake succeeded.")

            tlsInputStream = DataInputStream(tlsSocket.inputStream)
            tlsOutputStream = DataOutputStream(tlsSocket.outputStream)
            useTls = true
            message = read()
        } else if (message.command == A_AUTH) {
            if (message.command != A_AUTH && message.arg0 != ADB_AUTH_TOKEN) error("not A_AUTH ADB_AUTH_TOKEN")
            write(
                A_AUTH, ADB_AUTH_SIGNATURE, 0,
                if (useTls && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) adbKey.sign(message.data)
                else adbCrypto.signAdbTokenPayload(message.data)
            )

            message = read()
            if (message.command != A_CNXN) {
                write(
                    A_AUTH, ADB_AUTH_RSAPUBLICKEY, 0,
                    if (useTls) adbKey.adbPublicKey
                    else adbCrypto.getAdbPublicKeyPayload(name)
                )
                //wait for auth (we cannot perceive refuse by user
                val t = thread {
                    kotlin.runCatching {
                        message = read()
                    }
                }
                t.join(if (BuildConfig.DEBUG) 3000 else timeout)
                inputStream.close()
            }
        }

        if (message.command != A_CNXN) error("not A_CNXN")
        connected = true
        _loopThread = startLoop()
    }

    private fun startLoop() = thread {
        try {
            val t = Thread.currentThread()
            while (connected && !t.isInterrupted) {
                //loop read msg
                val message = read()
                val localId = message.arg1
                val remoteId = message.arg0
                when (message.command) {
                    A_OKAY -> {
                        streams[localId]?.notifyConnect(remoteId)
                    }
                    A_WRTE -> {
                        streams[localId]?.apply {
                            updateRemoteId(remoteId)
                            message.data?.also {
                                notifyStreamData(it)
                            }
                        }
                        write(A_OKAY, localId, remoteId)
                    }
                    A_CLSE -> {
                        write(A_CLSE, localId, remoteId)
                        if (streams.containsKey(localId)) {
                            closeDest(streams[localId])
                        } else {
                            Log.d(TAG, "no localId $localId")
                        }
                    }
                    else -> {
                        Log.e(TAG, "not A_OKAY or A_CLSE  $message")
                    }
                }
            }
        } catch (e: IOException) {
            close()
        }
    }

    //会导致重启adbd
    fun tcpip(port: Int) {
        val localId = _localId++
        write(A_OPEN, localId, 0, "tcpip:$port")
        //connect reset
        close()
    }

    @Throws
    fun open(dest: String, timeout: Long = 0, ondata: (OnData)? = null): AdbStream {
        if (!connected) {
            error("not connected...")
        }
        val lid = _localId++
        val destStream = AdbStream(lid, this, dest, ondata = ondata)
        synchronized(streams) {
            streams[lid] = destStream
        }
        write(A_OPEN, lid, 0, dest)

        if (destStream.awaitConnect(timeout) && destStream.closed) {
            synchronized(streams) {
                streams.remove(lid)
            }
            error("destStream closed")
        }
        return destStream
    }

    fun openShell(): AdbStream = open("shell:")

    fun shellCommand(
        command: String,
        timeout: Long = 0,
        ondata: (OnData)? = null
    ): AdbStream {
        return open("shell:$command", timeout, ondata)
    }

    private fun write(command: Int, arg0: Int, arg1: Int, data: ByteArray? = null) = write(
        AdbMessage(command, arg0, arg1, data)
    )

    private fun write(command: Int, arg0: Int, arg1: Int, data: String) =
        write(AdbMessage(command, arg0, arg1, data))

    private fun write(message: AdbMessage) {
        outputStream.write(message.toByteArray())
        outputStream.flush()
        Log.d(TAG, "write ${message.toStringShort()}")
    }

    private fun read(): AdbMessage {
        val buffer = ByteBuffer.allocate(AdbMessage.HEADER_LENGTH).order(ByteOrder.LITTLE_ENDIAN)

        inputStream.readFully(buffer.array(), 0, 24)

        val command = buffer.int
        val arg0 = buffer.int
        val arg1 = buffer.int
        val dataLength = buffer.int
        val checksum = buffer.int
        val magic = buffer.int
        val data: ByteArray?
        if (dataLength >= 0) {
            data = ByteArray(dataLength)
            inputStream.readFully(data, 0, dataLength)
        } else {
            data = null
        }
        val message = AdbMessage(command, arg0, arg1, dataLength, checksum, magic, data)
        message.validateOrThrow()
//        Log.d(TAG, "read ${message.toStringShort()}")
        return message
    }

    override fun close() {
        _loopThread?.interrupt()

        synchronized(streams) {
            streams.keyIterator().forEach { key ->
                kotlin.runCatching { streams[key].close() }
            }
            streams.clear()
        }
        try {
            plainInputStream.close()
        } catch (e: Throwable) {
        }
        try {
            plainOutputStream.close()
        } catch (e: Throwable) {
        }
        try {
            socket.close()
        } catch (e: Exception) {
        }

        if (useTls) {
            try {
                tlsInputStream.close()
            } catch (e: Throwable) {
            }
            try {
                tlsOutputStream.close()
            } catch (e: Throwable) {
            }
            try {
                tlsSocket.close()
            } catch (e: Exception) {
            }
        }
    }

    @Synchronized
    fun closeDest(adbStream: AdbStream?) {
        adbStream ?: return
        adbStream.notifyClose()
        Log.d(TAG, "closeDest[${adbStream.localId}]")
        write(A_CLSE, adbStream.localId, adbStream.remoteId, null)
        streams.remove(adbStream.localId)
    }

    fun writeDest(adbStream: AdbStream, data: ByteArray) {
        Log.d(TAG, "writeDest[${adbStream.localId}]: ${String(data)}")
        write(A_WRTE, adbStream.localId, adbStream.remoteId, data)
    }


}

