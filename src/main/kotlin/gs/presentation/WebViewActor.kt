package gs.presentation

import android.content.Intent
import android.net.Uri
import android.webkit.WebView
import gs.kar.R
import gs.property.IProperty
import gs.property.IWhen
import java.net.URL


class WebViewActor(
        private val parent: android.view.View,
        private val url: IProperty<URL>,
        private val forceEmbedded: Boolean = false,
        private val reloadOnError: Boolean = false,
        private val javascript: Boolean = false
) {

    private val RELOAD_ERROR_MILLIS = 5 * 1000L

    private val web: WebView = parent.findViewById(R.id.web_view) as WebView
    private val handler: android.os.Handler
    private var loaded = false
    private var reloadCounter = 0
    private var listener: IWhen? = null

    init {
        web.visibility = android.view.View.INVISIBLE
        if (javascript) web.settings.javaScriptEnabled = true
        web.settings.domStorageEnabled = true
        val cookie = android.webkit.CookieManager.getInstance()
        cookie.setAcceptCookie(true)
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            cookie.setAcceptThirdPartyCookies(web, true)
        }

        handler = android.os.Handler {
            loaded = true
            reloadCounter++
            web.loadUrl(url().toExternalForm())
            true
        }

        web.webViewClient =object : android.webkit.WebViewClient() {
            override fun shouldOverrideUrlLoading(view: android.webkit.WebView, url: String): Boolean {
                if (url.contains(url().host) || forceEmbedded) {
                    view.loadUrl(url)
                    return false
                } else {
                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url))
                    intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                    try { parent.context.startActivity(intent) } catch (e: Exception) {}
                    return true
                }
            }

            override fun onReceivedError(view: android.webkit.WebView?, request: android.webkit.WebResourceRequest?,
                                         error: android.webkit.WebResourceError?) {
                val url = if (android.os.Build.VERSION.SDK_INT >= 21) request?.url?.toString() else null
                handleError(url)
            }

            override fun onReceivedError(view: android.webkit.WebView?, errorCode: Int,
                                         description: String?, failingUrl: String?) {
                handleError(failingUrl)
            }

            override fun onPageFinished(view: android.webkit.WebView?, url2: String?) {
                if (loaded) {
                    web.visibility = android.view.View.VISIBLE
                }
            }
        }

        listener = url.doOnUiWhenChanged().then {
            reload()
        }

        try {
            handler.sendEmptyMessage(0)
        } catch (e: Exception) {
            handleError(null)
        }
    }

    fun reload() {
        reloadCounter = 0
        web.loadUrl(url().toExternalForm())
    }

    fun openInBrowser() {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.setData(Uri.parse(url().toString()))
        parent.context.startActivity(intent)
    }

    private fun handleError(url: String?) {
        if (!reloadOnError) return
        try {
            loaded = false
            if (reloadCounter++ <= 10) handler.sendEmptyMessageDelayed(0, RELOAD_ERROR_MILLIS)
        } catch (e: Exception) {}
    }
}
