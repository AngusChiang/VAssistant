package cn.vove7.vtp.asset

import android.content.Context
import java.io.BufferedReader
import java.io.InputStreamReader


/**
 * Asset类
 */
object AssetHelper {

    fun getStrFromAsset(context: Context, fileName: String): String? {
        try {
            val inputReader = InputStreamReader(context.assets.open(fileName))
            val bufReader = BufferedReader(inputReader)
            return bufReader.readText()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
}