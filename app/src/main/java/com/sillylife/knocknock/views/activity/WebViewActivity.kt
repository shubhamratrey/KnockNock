package com.sillylife.knocknock.views.activity

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.webkit.*
import com.sillylife.knocknock.R
import com.sillylife.knocknock.constants.BundleConstants
import kotlinx.android.synthetic.main.activity_webview.*

class WebViewActivity : BaseActivity() {

    val TAG = WebViewActivity::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webview)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            toolbar?.setTitleTextColor(getColor(R.color.black))
        }

        toolbar?.setNavigationOnClickListener {
            onBackPressed()
        }
        toolbar?.setNavigationIcon(R.drawable.ic_left_arrow)

        setWebView()
        if (intent.hasExtra(BundleConstants.WEB_URL)) {
            loadUrl(webView, intent.getStringExtra(BundleConstants.WEB_URL)!!.toString())
        }  else if(intent!=null && intent.dataString?.contains("local-web")!!){
            val url = intent.dataString!!.replace("local-web://", "http://")
            loadUrl(webView, url)
        }else {
            loadUrl(webView, "https://zoopzam.com/")
        }
    }

    private fun loadUrl(view: WebView?, url: String) {
        val uri: Uri.Builder = Uri.parse(url).buildUpon()
        uri.appendQueryParameter("package_name", packageName)
        view?.loadUrl(uri.toString())
        showBackBtn()
    }

    private fun setWebView() {
        try {
            val webSettings = webView.settings
            if (!webSettings.javaScriptEnabled) {
                webSettings.javaScriptEnabled = true
            }
            webSettings.allowFileAccess = true
            webSettings.allowContentAccess = true
            webSettings.allowFileAccessFromFileURLs = true
            webSettings.allowUniversalAccessFromFileURLs = true
            webSettings.loadWithOverviewMode = true
            webSettings.useWideViewPort = true
            webSettings.userAgentString = (webSettings.userAgentString + "local-web")
            webView?.addJavascriptInterface(WebViewJavaScriptInterface(), "Android")
            webView?.webViewClient = object : WebViewClient() {

                override fun onLoadResource(view: WebView?, url: String?) {
                    super.onLoadResource(view, url)
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    toolbar?.title = view?.title
                    Log.d("webviewurl", url ?: "saya")
                    showBackBtn()
                }

                @SuppressWarnings("deprecation")
                override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                    when {
                        url!!.startsWith("mailto:") -> {
                            //Handle mail Urls
                            startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse(url)))
                        }
                        url.startsWith("tel:") -> {
                            //Handle telephony Urls
                            startActivity(Intent(Intent.ACTION_DIAL, Uri.parse(url)))
                        }
                        else -> {
                            loadUrl(view, url)
                        }
                    }
                    return true
                }

                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                    val uri: Uri? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        request?.url!!
                    } else {
                        null
                    }
                    when {
                        uri?.toString()?.startsWith("mailto:") == true -> {
                            //Handle mail Urls
                            startActivity(Intent(Intent.ACTION_SENDTO, uri))
                        }
                        uri?.toString()?.startsWith("tel:") == true -> {
                            //Handle telephony Urls
                            startActivity(Intent(Intent.ACTION_DIAL, uri))
                        }
                        else -> {
                            if (uri?.host?.equals("play.google.com") == true || uri?.authority?.equals("play.google.com") == true) {
                                if (uri?.query?.contains(APP_PACKAGENAME) == true && appInstalled(APP_PACKAGENAME)) {
                                    val launchIntent = packageManager.getLaunchIntentForPackage(APP_PACKAGENAME)
                                    startActivity(launchIntent)
                                } else {
                                    if (appInstalled("com.android.vending")) {
                                        val intent = Intent(Intent.ACTION_VIEW)
                                        intent.data = Uri.parse(uri?.toString())
                                        startActivity(intent)
                                    } else {
                                        loadUrl(view, uri.toString())
                                    }
                                }
                            } else {
                                if (uri != null) {
                                    loadUrl(view, uri.toString())
                                }
                            }
                        }
                    }
                    return true
                }
            }

            webView?.webChromeClient = object : WebChromeClient() {

                override fun onReceivedTitle(view: WebView?, title: String?) {
                    toolbar?.title = view?.title
                    showBackBtn()
                }

                override fun onShowFileChooser(
                        webView: WebView?, filePathCallback: ValueCallback<Array<Uri>>?,
                        fileChooserParams: FileChooserParams?
                ): Boolean {

                    return true
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    inner class WebViewJavaScriptInterface {
        @JavascriptInterface
        fun processPageClick(string: String): Boolean {
            finish()
            return true
        }
    }

    fun hideKeyboard() {
        val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val v = currentFocus ?: return
        inputManager.hideSoftInputFromWindow(v.windowToken, 0)
    }

//    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        menuInflater.inflate(R.menu.main, menu)
//        return true
//    }

//    override fun onSupportNavigateUp(): Boolean {
//        val navController = findNavController(R.id.nav_host_fragment)
//        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
//    }

    fun showBackBtn() {
        toolbar?.setNavigationIcon(R.drawable.ic_left_arrow)
        /*if (webView?.canGoBack() == true) {
            toolbar?.setNavigationIcon(R.drawable.ic_left_arrow)
        } else {
            toolbar?.navigationIcon = null
        }*/
    }

    override fun onBackPressed() {
        if (webView?.canGoBack() == true) {
            webView?.goBack()
        } else {
            super.onBackPressed()
        }
    }

    private fun appInstalled(uri: String): Boolean {
        val pm = packageManager
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES)
            return true
        } catch (e: PackageManager.NameNotFoundException) {
        }
        return false
    }

    companion object {
        const val APP_PACKAGENAME = "com.zoopzam.partner"
    }

    override fun onDestroy() {
        super.onDestroy()
    }

}