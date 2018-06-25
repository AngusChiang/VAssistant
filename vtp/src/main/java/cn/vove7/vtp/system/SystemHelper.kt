package cn.vove7.vtp.system

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context

/**
 *
 *
 * Created by Vove on 2018/6/24
 */
object SystemHelper {
    /**
     * 获取剪切板最新内容
     */
    fun getClipBoardContent(context: Context): CharSequence {
        val data = getClipData(context)
        return data.getItemAt(data.itemCount - 1).text
    }

    /**
     * 获取剪切板数据
     * @return ClipData
     */

    fun getClipData(context: Context): ClipData {
        val cs = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        return cs.primaryClip
    }
}