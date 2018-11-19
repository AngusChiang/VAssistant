package cn.vove7.common.interfaces

import android.os.Handler
import android.os.Looper
import cn.vassistant.plugininterface.app.GlobalApp
import cn.vove7.common.utils.TextHelper
import cn.vove7.common.utils.ThreadPool.runOnPool
import java.io.File

/**
 * # CodeEditorOperation
 *
 * @author Administrator
 * 2018/10/10
 */
interface CodeEditorOperation {
    fun setDark(d: Boolean)
    val selectedText: String?
    var isEdit: Boolean

    fun setText(s: CharSequence)
    fun getEditorContent(): String?
    fun gotoLine()
    fun undo()
    fun redo()

     fun openFile(fullPath: String) {
         runOnPool {
            val s = TextHelper.readFile(fullPath)
            Handler(Looper.getMainLooper()).post {
                if (s == null) {
                    GlobalApp.toastShort("打开失败")
                }
                setText(s ?: "")
            }
        }
    }

    fun saveFile(fullPath: String) {//todo loading dialog
        val outputFile = File(fullPath)
        if (!outputFile.canWrite()) {
            GlobalApp.toastShort("文件不可写")
        } else {
            if (TextHelper.writeFile(fullPath, getEditorContent() ?: "")) {
                GlobalApp.toastShort("保存成功")
            } else {
                GlobalApp.toastShort("保存失败，请查看日志")
            }
        }
    }

    fun format()
    fun find()
    fun insert(s: CharSequence?)
}