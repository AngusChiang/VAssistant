package cn.vove7.jarvis.tools

import android.os.AsyncTask
import cn.vove7.common.interfaces.DownloadInfo

import java.io.File
import java.io.InputStream
import java.io.RandomAccessFile

import cn.vove7.common.interfaces.DownloadProgressListener
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.*


/**
 * Created by Vove on 2017/10/9.
 * DownloadTask
 */

class DownloadTask<T>(private val downloadUrl: String, private val dir: String,
                      private val filename: String,
                      private val downloadListener: DownloadProgressListener,
                      data: T? = null
) : AsyncTask<Any, Int, Int>() {

    private var isPause = false
    private var isCanceled = false

    val info: DownloadInfo<T>

    init {
        val id = Random().nextInt()
        info = DownloadInfo(id, "", data)
    }

    override fun doInBackground(vararg params: Any): Int? {
        val directory = File(dir)
        if (!directory.exists() && !directory.mkdir()) {
            downloadListener.onFailed(info, Exception("文件夹创建失败"))
            return STATUS_FAILED
        }

        var file: File? = null
        try {
            var downloadLength: Long = 0//文件长度

            file = File(dir, filename)
            if (file.exists()) {//续点
                downloadLength = file.length()
            }
            //long contentLength = getContentLength(downloadUrl);
            //if (contentLength == 0) {
            //   LogHelper.d(null, "contentLength = 0");
            //
            //   return STATUS_CONTENT_LENGTH_0;
            //} else
            //if (file.exists()) {
            //
            //    LogHelper.d(null, "文件已下载-db-->" + filename);
            //    return STATUS_SUCCESS;
            //}

            val client = OkHttpClient()
            val request = Request.Builder()//断点续传，指定位置
                    .addHeader("RANGE", "bytes=$downloadLength-")
                    .url(downloadUrl)
                    .build()
            val response = client.newCall(request).execute()
            val body = response.body()
            if (body != null && body.contentLength() != 0L) {
                body.byteStream()?.use { inputStream ->
                    RandomAccessFile(file, "rw").use { saveFile ->
                        saveFile.seek(downloadLength)//
                        val b = ByteArray(1024)
                        //            int total = 0;
                        var len = 0
                        while ((inputStream.read(b).also { len = it }) != -1) {
                            when {
                                isCanceled -> return STATUS_CANCELED
                                isPause -> return STATUS_PAUSE
                                else -> {
                                    saveFile.write(b, 0, len)

//                                    downloadListener.onDownloading(info,)
                                }
                            }
                        }
                        body.close()
                        return STATUS_SUCCESS
                    }
                }

            } else {
                return STATUS_CONTENT_LENGTH_0
            }


        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                if (isCanceled && file != null) {
                    file.delete()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
        return STATUS_FAILED
    }

    override fun onPostExecute(status: Int?) {//接收doInBackground结果
        when (status) {
//            STATUS_SUCCESS -> downloadListener.onSuccess(info, file)
//            STATUS_CANCELED -> downloadListener.onCancel(info, )
//            STATUS_FAILED -> downloadListener.onFailed(info)
//            STATUS_PAUSE -> downloadListener.onPause(info)
//            STATUS_CONTENT_LENGTH_0 -> downloadListener.onFailed(info, Exception(""))
            else -> {
            }
        }

    }

    fun pauseDownload() {
        isPause = true
    }

    fun cancelDownload() {
        isCanceled = true
    }

    companion object {
        private val STATUS_SUCCESS = 0
        private val STATUS_FAILED = 1
        private val STATUS_PAUSE = 2
        private val STATUS_CANCELED = 3
        private val STATUS_CONTENT_LENGTH_0 = 4
    }

}

