package cn.vove7.jarvis.view.custom.scmoothtextview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;


public class TextWebView extends WebView {

    public TextWebView(Context context) {
        super(context);
        if (isInEditMode()) return;
        initView(null);
    }

    public TextWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(attrs);
    }

    public TextWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(attrs);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initView(@Nullable AttributeSet attrs) {
        if (isInEditMode()) return;
        if (attrs != null) {
            setBackgroundColor(0xffffff);
        }
        setWebChromeClient(new ChromeClient());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            setWebViewClient(new WebClient());
        } else {
            setWebViewClient(new WebClientCompat());
        }
        //addJavascriptInterface();

        WebSettings settings = getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setAppCachePath(getContext().getCacheDir().getPath());
        settings.setAppCacheEnabled(true);
        settings.setSupportZoom(false);
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING);

        settings.setDefaultTextEncodingName("utf-8");
        setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
    }

    public void setSource(@NonNull String source, boolean wrap) {
        loadCode(buildHtml("<xmp style = \"" + (wrap ? "overflow: hidden" : "") + "\">\n" +
                source + "\n" +
                "    </xmp>\n" +
                source + "</xmp>"));
    }

    public void setHtml(String html) {
        loadCode(buildHtml(html));
    }

    private String baseUrl = "file:///android_asset/web/";

    private void loadCode(String page) {
        post(() -> loadDataWithBaseURL(baseUrl, page, "text/html", "utf-8", null));
    }

    private String buildHtml(String html) {
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
                "</html>";
    }


    private class ChromeClient extends WebChromeClient {

    }

    private void startActivity(@Nullable Uri url) {
        getContext().startActivity(new Intent(Intent.ACTION_VIEW, url));
    }

    private class WebClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            startActivity(request.getUrl());
            return true;
        }
    }

    private class WebClientCompat extends WebViewClient {
        @SuppressWarnings("deprecation")
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            startActivity(Uri.parse(url));
            return true;
        }

    }

}