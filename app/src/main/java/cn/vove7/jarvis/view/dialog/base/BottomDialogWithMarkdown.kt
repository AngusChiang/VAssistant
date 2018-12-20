package cn.vove7.jarvis.view.dialog.base

import android.content.Context
import android.view.View
import br.tiagohm.markdownview.MarkdownView
import br.tiagohm.markdownview.css.styles.Bootstrap
import cn.vove7.jarvis.R
import cn.vove7.jarvis.fragments.base.BaseBottomDialogWithToolbar
import java.io.File

/**
 * # BottomDialogWithMarkdown
 *
 * @author Administrator
 * 2018/12/19
 */
class BottomDialogWithMarkdown(context: Context, title: String) : BaseBottomDialogWithToolbar(context, title) {

    private val markDownView: MarkdownView by lazy {
        contentView.findViewById<MarkdownView>(R.id.markdown_view).also {
            it.addStyleSheet(MyStyle())
        }
    }


    val contentView: View by lazy {
        layoutInflater.inflate(R.layout.dialog_markdown_view, null)
    }

    override fun onCreateContentView(parent: View): View = contentView
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
            loadText("文件不存在!")
        }
    }

    fun loadFromUrl(url: String) {
        markDownView.loadMarkdownFromUrl(url)
    }

}

class MyStyle : Bootstrap() {
    init {
        this.addRule("body", "line-height: 1.6", "padding: 5px")

        this.addRule("h1", "font-size: 28px")
        this.addRule("h2", "font-size: 24px")
        this.addRule("h3", "font-size: 18px")
        this.addRule("h4", "font-size: 16px")
        this.addRule("h5", "font-size: 14px")
        this.addRule("h6", "font-size: 14px")
        this.addRule("pre", "position: relative", "padding: 5px 5px", "border: 0", "border-radius: 3px", "background-color: #f6f8fa")
        this.addRule("pre code", "position: relative", "line-height: 1.45", "background-color: transparent")
        this.addRule("table tr:nth-child(2n)", "background-color: #f6f8fa")
        this.addRule("table th", "padding: 6px 13px", "border: 1px solid #dfe2e5")
        this.addRule("table td", "padding: 6px 13px", "border: 1px solid #dfe2e5")
        this.addRule("kbd", "color: #444d56", "font-family: Consolas, \"Liberation Mono\", Menlo, Courier, monospace", "background-color: #fcfcfc", "border: solid 1px #c6cbd1", "border-bottom-color: #959da5", "border-radius: 3px", "box-shadow: inset 0 -1px 0 #959da5")
        this.addRule("pre[language]::before", "content: attr(language)", "position: absolute", "top: 0", "right: 5px", "padding: 2px 1px", "text-transform: uppercase", "color: #666", "font-size: 8.5px")
        this.addRule("pre:not([language])", "padding: 6px 10px")
        this.addRule(".footnotes li p:last-of-type", "display: inline")
        this.addRule(".yt-player", "box-shadow: 0px 0px 12px rgba(0,0,0,0.2)")
        this.addRule(".scrollup", "background-color: #00BF4C")
        this.addRule(".hljs-comment", "color: #8e908c")
        this.addRule(".hljs-quote", "color: #8e908c")
        this.addRule(".hljs-variable", "color: #c82829")
        this.addRule(".hljs-template-variable", "color: #c82829")
        this.addRule(".hljs-tag", "color: #c82829")
        this.addRule(".hljs-name", "color: #c82829")
        this.addRule(".hljs-selector-id", "color: #c82829")
        this.addRule(".hljs-selector-class", "color: #c82829")
        this.addRule(".hljs-regexp", "color: #c82829")
        this.addRule(".hljs-deletion", "color: #c82829")
        this.addRule(".hljs-number", "color: #f5871f")
        this.addRule(".hljs-built_in", "color: #f5871f")
        this.addRule(".hljs-builtin-name", "color: #f5871f")
        this.addRule(".hljs-literal", "color: #f5871f")
        this.addRule(".hljs-type", "color: #f5871f")
        this.addRule(".hljs-params", "color: #f5871f")
        this.addRule(".hljs-meta", "color: #f5871f")
        this.addRule(".hljs-link", "color: #f5871f")
        this.addRule(".hljs-attribute", "color: #eab700")
        this.addRule(".hljs-string", "color: #718c00")
        this.addRule(".hljs-symbol", "color: #718c00")
        this.addRule(".hljs-bullet", "color: #718c00")
        this.addRule(".hljs-addition", "color: #718c00")
        this.addRule(".hljs-title", "color: #4271ae")
        this.addRule(".hljs-section", "color: #4271ae")
        this.addRule(".hljs-keyword", "color: #8959a8")
        this.addRule(".hljs-selector-tag", "color: #8959a8")
    }
}