package cn.vove7.jarvis.view.custom.scmoothtextview

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.webkit.*


class TextWebView : NestedWebView, TextLoader {

    private val baseUrl = "file:///android_asset/web/"

    constructor(context: Context) : super(context) {
        if (isInEditMode) return
        initView(null)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initView(attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initView(attrs)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initView(attrs: AttributeSet?) {
        if (isInEditMode) return
        if (attrs != null) {
            setBackgroundColor(0xffffff)
        }
        webChromeClient = ChromeClient()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            webViewClient = WebClient()
        } else {
            webViewClient = WebClientCompat()
        }
        //addJavascriptInterface();

        val settings = settings
        settings.javaScriptEnabled = true
        settings.setAppCachePath(context.cacheDir.path)
        settings.setAppCacheEnabled(true)
        settings.setSupportZoom(false)
        settings.cacheMode = WebSettings.LOAD_NO_CACHE
        settings.layoutAlgorithm = WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING

        settings.defaultTextEncodingName = "utf-8"
        scrollBarStyle = View.SCROLLBARS_INSIDE_OVERLAY
    }

    override fun setSource(source: String, wrap: Boolean) {
        loadCode(buildHtml("<xmp style = \"" + (if (wrap) "overflow: hidden" else "") + "\">\n" +
                source + "\n" +
                "    </xmp>\n" +
                source + "</xmp>"))
    }

    override fun setHtml(html: String) {
        loadCode(buildHtml(html))
    }

    private fun loadCode(page: String) {
        post { loadDataWithBaseURL(baseUrl, page, "text/html", "utf-8", null) }
    }

    private fun buildHtml(html: String): String {
        return "<html>\n" +
                "\n" +
                "<head>\n" +
                "    <link rel=\"stylesheet\" href=\"text_style.css\">\n" +
                "</head>\n" +
                "\n" +
                "<body>\n" +
                "\n" +
                html +
                "</body>\n" +
                "\n" +
                "</html>"
    }


    private inner class ChromeClient : WebChromeClient()

    private fun startActivity(url: Uri?) {
        context.startActivity(Intent(Intent.ACTION_VIEW, url))
    }

    private inner class WebClient : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
            startActivity(request.url)
            return true
        }
    }

    private inner class WebClientCompat : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            startActivity(Uri.parse(url))
            return true
        }

    }

}

interface TextLoader {
    fun setSource(source: String, wrap: Boolean = false)
    fun setHtml(html: String)
}
