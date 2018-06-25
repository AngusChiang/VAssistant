package cn.vove7.vtp.file

/**
 *
 *
 * Created by Vove on 2018/6/21
 */
object FileHelper {


    /**
     * 获得文件直观大小
     * 进制：1024
     * 暂时最大转换单位 ***Tb
     */
    fun getAdapterFileSize(size: Long): String {
        var i = 0
        while (size < 10L && i < sizeUnits.size) {
            size shl 10
            i++
        }
        return "$size${sizeUnits[i]}"
    }

    private val sizeUnits = arrayOf("bytes", "KB", "MB", "GB", "TB")
}