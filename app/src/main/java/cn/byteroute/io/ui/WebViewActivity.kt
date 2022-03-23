package cn.byteroute.io.ui

import android.os.Build
import android.os.Bundle
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import cn.byteroute.io.R
import cn.byteroute.io.annotation.WebType
import cn.byteroute.io.base.BaseActivity
import cn.byteroute.io.base.BaseViewModel
import cn.byteroute.io.common.Key
import kotlinx.android.synthetic.main.activity_web_view.*
import kotlinx.android.synthetic.main.include_toolbar_web.*

class WebViewActivity : BaseActivity<BaseViewModel>() {
    private val title by lazy {
        intent.getStringExtra(Key.WEB_TITLE)
    }
    private val url by lazy {
        intent.getStringExtra(Key.WEB_URL)
    }

    private val type by lazy { intent.getIntExtra(Key.WEB_TYPE, -1) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_view)
        toolbar_title.text = title
        iv_finish.setOnClickListener { finish() }
        iv_back.setOnClickListener {
            if (webview.canGoBack()) {
                webview.goBack()
            } else {
                finish()
            }
        }
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP -> {
                webview.getSettings().mixedContentMode =
                    WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                webview.setLayerType(
                    View.LAYER_TYPE_HARDWARE,
                    null
                )
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT -> webview.setLayerType(
                View.LAYER_TYPE_HARDWARE,
                null
            )
            Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT -> webview.setLayerType(
                View.LAYER_TYPE_SOFTWARE,
                null
            )
        }
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT -> webview.getSettings().layoutAlgorithm =
                WebSettings.LayoutAlgorithm.SINGLE_COLUMN
            else -> webview.getSettings().layoutAlgorithm = WebSettings.LayoutAlgorithm.NORMAL
        }
        webview.getSettings().javaScriptEnabled = true
        webview.getSettings().setSupportZoom(true)
        webview.getSettings().builtInZoomControls = false
        webview.getSettings().savePassword = false
        webview.getSettings().textZoom = 100
        webview.getSettings().databaseEnabled = true
        webview.getSettings().setAppCacheEnabled(true)
        webview.getSettings().loadsImagesAutomatically = true
        webview.getSettings().setSupportMultipleWindows(false)
        webview.getSettings().blockNetworkImage = false
        webview.getSettings().allowFileAccess = true
        webview.getSettings().javaScriptCanOpenWindowsAutomatically = true
        webview.getSettings().loadWithOverviewMode = false
        webview.getSettings().useWideViewPort = false
        webview.getSettings().domStorageEnabled = true
        webview.getSettings().setNeedInitialFocus(true)
        webview.getSettings().defaultTextEncodingName = "utf-8" //设置编码格式
        webview.getSettings().defaultFontSize = 16
        webview.getSettings().minimumFontSize = 12 //设置 WebView 支持的最小字体大小，默认为 8
        webview.getSettings().setGeolocationEnabled(true)
        webview.setWebViewClient(WebViewClient())
        webview.setWebChromeClient(object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                when (newProgress) {
                    100 -> {
                        progress.visibility = View.GONE
                        if (type == WebType.WEB_MD) loadMarkdown(url)
                    }
                }
            }
        })
        when (type) {
            WebType.WEB_MD -> webview.loadUrl("file:///android_asset/markdown.html")
            else -> webview.loadUrl(url!!)
        }

    }

    fun loadMarkdown(url: String?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            webview.evaluateJavascript("javascript:parseMarkdown(\"$url\");", null)
        } else {
            webview.loadUrl("javascript:parseMarkdown(\"$url\");")
        }
    }
}