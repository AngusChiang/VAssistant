package cn.vove7.common.interfaces

import android.os.Handler
import android.os.Looper
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.utils.TextHelper
import cn.vove7.common.utils.CoroutineExt.launch
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
         launch {
            val s = TextHelper.readFile(fullPath)
            Handler(Looper.getMainLooper()).post {
                if (s == null) {
                    GlobalApp.toastError("打开失败")
                }
                setText(s ?: "")
            }
        }
    }

    fun saveFile(fullPath: String) {//todo loading dialog
        val outputFile = File(fullPath)
        if (!outputFile.canWrite()) {
            GlobalApp.toastError("文件不可写")
        } else {
            if (TextHelper.writeFile(fullPath, getEditorContent() ?: "")) {
                GlobalApp.toastSuccess("保存成功")
            } else {
                GlobalApp.toastError("保存失败，请查看日志")
            }
        }
    }

    fun format()
    fun find()
    fun insert(s: CharSequence?)
}