package cn.vove7.common.interfaces

import java.io.File

interface DownloadProgressListener {

    /**
     * 下载成功
     * 在UI线程
     */
    fun onSuccess(info: DownloadInfo<*>, file: File)
    fun onStart(info: DownloadInfo<*>)

    fun onCancel(info: DownloadInfo<*>){}
    fun onPause(info: DownloadInfo<*>){}

    /**
     * 下载进度
     * 在UI线程
     */
    fun onDownloading(info: DownloadInfo<*>, progress: Int)

    /**
     * 下载异常信息
     * 在UI线程
     */
    fun onFailed(info: DownloadInfo<*>, e: Exception)

}

class DownloadInfo<T>(
        val id: Int,
        val fullPath: String,
        val data: T? = null
)