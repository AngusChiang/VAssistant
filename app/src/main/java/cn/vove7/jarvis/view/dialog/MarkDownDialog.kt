package cn.vove7.jarvis.view.dialog

import android.content.Context
import android.view.View
import br.tiagohm.markdownview.MarkdownView
import cn.vove7.jarvis.R
import cn.vove7.jarvis.view.dialog.base.CustomizableDialog
import java.io.File

/**
 * # MarkDownDialog
 *
 * @author Administrator
 * 2018/12/17
 */
class MarkDownDialog(context: Context, title: String?)
    : CustomizableDialog(context, title) {
    val markDownView: MarkdownView by lazy {
        view.findViewById<MarkdownView>(R.id.markdown_view)
    }

    val view: View by lazy {
        layoutInflater.inflate(R.layout.dialog_markdown_view, null).also {
            it.setPadding(45,0,45,0)
        }
    }
    override fun initView(): View = view

    fun loadFromAsset(path: String) {
        markDownView.loadMarkdownFromAsset(path)
    }

    fun loadText(md: String) {
        markDownView.loadMarkdown(md)
    }

    fun loadFromFile(path: String) {
        val f = File(path)
        if (f.exists()) {
            markDownView.loadMarkdownFromFile(f)
        } else {
            loadText("文件不存在")
        }
    }

    fun loadFromUrl(url: String) {
        markDownView.loadMarkdownFromUrl(url)
    }


}