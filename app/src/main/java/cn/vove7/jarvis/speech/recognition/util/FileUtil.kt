package cn.vove7.jarvis.speech.recognition.util

import android.content.res.AssetManager

import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.nio.charset.Charset

/**
 * Created by fujiayi on 2017/5/19.
 */

object FileUtil {

    fun makeDir(dirPath: String): Boolean {
        val file = File(dirPath)
        return if (file.exists()) {
            true
        } else {
            file.mkdirs()
        }
    }

    fun getContentFromAssetsFile(assets: AssetManager, source: String): String {
        val `is`: InputStream?
        val fos: FileOutputStream? = null
        var result = ""
        try {
            `is` = assets.open(source)
            val lenght = `is`!!.available()
            val buffer = ByteArray(lenght)
            `is`.read(buffer)
            result = String(buffer,Charset.forName("utf8"))
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return result
    }

    @Throws(IOException::class)
    fun copyFromAssets(assets: AssetManager, source: String, dest: String,
                       isCover: Boolean): Boolean {
        val file = File(dest)
        var isCopyed = false
        if (isCover || !isCover && !file.exists()) {
            var `is`: InputStream? = null
            var fos: FileOutputStream? = null
            try {
                `is` = assets.open(source)
                val path = dest
                fos = FileOutputStream(dest)
                val buffer = ByteArray(1024)
                var size = 0
                size = `is`!!.read(buffer, 0, 1024)
                while (size >= 0) {
                    fos.write(buffer, 0, size)
                    size = `is`.read(buffer, 0, 1024)
                }
                isCopyed = true
            } finally {
                if (fos != null) {
                    try {
                        fos.close()
                    } finally {
                        if (`is` != null) {
                            `is`.close()
                        }
                    }
                }
            }

        }
        return isCopyed
    }
}
