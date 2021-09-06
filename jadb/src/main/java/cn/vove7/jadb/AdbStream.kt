package cn.vove7.jadb

import java.io.Closeable


/**
 * # AdbStream
 *
 * @author Vove
 * @date 2021/8/24
 */
class AdbStream(
    val localId: Int,
    val client: AdbClient,
    val dest: String,
    var remoteId: Int = 0,
    private var ondata: OnData? = null,
    private var onClosed: OnClose? = null,
) : Closeable {

    var closed = false
        private set

    private val lock = Object()
    private val closeLock = Object()

    private var _data = ByteArray(0)

    val data get() = _data

    var saveOutput = true

    fun noStoreOutput() {
        saveOutput = false
    }

    fun notifyConnect(remoteId: Int) {
        updateRemoteId(remoteId)
        synchronized(lock) {
            lock.notify()
        }
        closed = false
    }

    fun updateRemoteId(remoteId: Int) {
        this.remoteId = remoteId
    }

    fun notifyStreamData(data: ByteArray) {
        if (saveOutput) {
            val d = ByteArray(data.size + _data.size)
            System.arraycopy(_data, 0, d, 0, _data.size)
            System.arraycopy(data, 0, d, _data.size, data.size)
            _data = d
        }
        ondata?.invoke(data)
    }

    override fun close() {
        synchronized(lock) {
            lock.notify()
        }
        client.closeDest(this)
    }

    fun write(data: String) {
        write(data.toByteArray())
    }

    fun write(data: ByteArray) {
        if (closed) {
            error("AdbStream($localId) is closed")
        }
        client.writeDest(this, data)
    }

    fun awaitConnect(timeout: Long): Boolean {
        if (!closed) return true
        synchronized(lock) {
            lock.wait(timeout)
        }
        return !closed
    }

    fun onData(onData: OnData?) {
        ondata = onData
    }

    fun onClose(onClosed: OnClose?) {
        this.onClosed = onClosed
    }

    fun notifyClose() {
        closed = true
        onClosed?.invoke(this)
        synchronized(closeLock) {
            closeLock.notify()
        }
    }

    fun interrupt() {
        if(!closed) {
            write(byteArrayOf(0x03))
        }
    }

    fun awaitClose(timeout: Long = 0) {
        synchronized(closeLock) {
            closeLock.wait(timeout)
        }
    }

}