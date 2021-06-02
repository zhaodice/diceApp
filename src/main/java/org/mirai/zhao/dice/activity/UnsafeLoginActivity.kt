package org.mirai.zhao.dice.activity

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.webkit.*
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.mirai.zhao.dice.R
import org.mirai.zhao.dice.activity.ui.login.LoginActivity
import org.mirai.zhao.dice.console.AndroidLoginSolver


class UnsafeLoginActivity : AppCompatActivity() {
    private lateinit var unsafeLoginWeb : WebView
    private lateinit var refreshUnsafeWeb: SwipeRefreshLayout
    private lateinit var copyUrlBtn:Button
    private lateinit var notice:TextView
    private var enforceFinishedStep = 0
    companion object{
        private const val disguisedUserAgent =
                "Mozilla/5.0 (Linux; Android 5.1; vivo X6Plus D Build/LMY47I; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/77.0.3865.120 MQQBrowser/6.2 TBS/045330 Mobile Safari/537.36 V1_AND_SQ_7.1.0_0_TIM_D TIM/3.0.0.2858 QQ/6.5.5  NetType/WIFI WebP/0.3.0 Pixel/1080"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_unsafe_login)
        refreshUnsafeWeb=findViewById(R.id.refresh_unsafe_web)
        unsafeLoginWeb=findViewById(R.id.unsafe_login_web)
        copyUrlBtn=findViewById(R.id.forceStopUnsafeLogin)
        notice=findViewById(R.id.notice)

        if(enforceFinishedStep==0){
            copyUrlBtn.text = "无法完成身份验证？点我复制链接！"
            copyUrlBtn.setOnClickListener{
                val clip: ClipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val myClip = ClipData.newPlainText("text", unsafeLoginWeb.url)
                clip.setPrimaryClip(myClip)

                val alterDialog = AlertDialog.Builder(this)
                alterDialog.setTitle("成功")
                alterDialog.setMessage("链接已经复制到了剪切板，现在你需要做\n" +
                        "1.【不要返回，按HOME切换】切换APP到你正准备认证的QQ\n" +
                        "2.然后粘贴链接到没人看见的地方，比如“我的电脑”\n" +
                        "3.点入链接，进入二维码认证\n" +
                        "4.长按二维码，点击“扫描二维码”，完成认证\n" +
                        "5.回到本APP，点击“我已在外面完成认证”")
                alterDialog.setPositiveButton("我已在外面完成认证") { dialogInterface, _ -> dialogInterface.cancel() }
                alterDialog.setOnCancelListener { authFinish("") }
                alterDialog.show()
            }
        }

        initWebView()
        val url= intent.getStringExtra("url")
        if(url==null) {
            finish()
            return
        }
        unsafeLoginWeb.loadUrl(url)
        refreshUnsafeWeb.setOnRefreshListener {
            unsafeLoginWeb.reload()
            GlobalScope.launch {
                delay(1000)
                runOnUiThread {
                    refreshUnsafeWeb.isRefreshing = false
                }
            }
        }
        //  Toast.makeText(this, "请在完成验证后点击右上角继续登录", Toast.LENGTH_LONG).show()
    }


    @SuppressLint("SetJavaScriptEnabled", "AddJavascriptInterface")
    private fun initWebView() {
        //滑块验证码js注入，便于获取ticks
        unsafeLoginWeb.webViewClient = object : WebViewClient() {

            override fun onPageFinished(view: WebView?, url: String?) {
                if (url != null && view != null) {
                    println("Load WebView: $url")
                    if (url.startsWith("https://ssl.captcha.qq.com/template/wireless_mqq_captcha.html")) {//滑块验证码
                        notice.text = getString(R.string.slidNotice)
                        //ToastUtils.show(this@UnsafeLoginActivity,"滑的时候注意了，尽可能在图案里滑，别滑到下面的空白区域，否则会很不丝滑。",Toast.LENGTH_LONG)
                    }
                    println("界面加载完毕！注入JS...")
                    val js = "mqq.invoke = function(a,b,c){return bridge.invoke(a,b,JSON.stringify(c))};"
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
                        view.evaluateJavascript(js) {}
                    else
                        view.loadUrl("javascript:$js")
                }
                super.onPageFinished(view, url)
            }
            /*
            override fun shouldInterceptRequest(view: WebView, webResourceRequest: WebResourceRequest): WebResourceResponse? {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                    val u = webResourceRequest.url
                    //Log.d("zhaodice", "zhaodice u.protocol=" + u.scheme + " u.path=" + u.path + " u.query=" + u.query)
                    if (u.scheme == "jsbridge") {
                        if (u.path == "/openUrl") {
                            val query = u.query
                            if (query != null) {
                                val json = query.substring(2)//去掉p=
                                //Log.d("zhaodice","zhaodice-B-"+json)
                                try {
                                    val goUrl = JSONObject(json).getString("url")
                                    view.post {
                                        unsafeLoginWeb.loadUrl(goUrl)
                                    }
                                    //.d("zhaodice","zhaodice-C-"+goUrl)
                                } catch (e: Throwable) {
                                    //Log.d("zhaodice","zhaodice-C-"+e.message)
                                }
                            }
                        }
                        try {
                            return WebResourceResponse("text/plain", "UTF-8", assets.open("empty.txt"))
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                }
                return super.shouldInterceptRequest(view, webResourceRequest)
            }*/
        }
        unsafeLoginWeb.addJavascriptInterface(Bridge(), "bridge")
        unsafeLoginWeb.webChromeClient = object : WebChromeClient() {
            override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                // 按下回到qq按钮之后会打印这句话，于是就用这个解决了。。。。
                if (consoleMessage?.message()?.startsWith("手Q扫码验证") == true) {
                    authFinish("")
                }
                return super.onConsoleMessage(consoleMessage)
            }
            /*
            override fun onJsPrompt(view: WebView?, url: String?, message: String?, defaultValue: String?, result: JsPromptResult?): Boolean {
                //检测到滑块验证码滑动完成，反馈ticks
                if("MiraiSelenium - ticket"==message &&result!=null&& defaultValue!=null){
                    result.confirm("received")
                    authFinish(defaultValue)
                    return true
                }
                return super.onJsPrompt(view, url, message, defaultValue, result)
            }*/
        }
        unsafeLoginWeb.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            userAgentString=disguisedUserAgent
        }

    }
    private fun authFinish(s: String) {
        AndroidLoginSolver.INSTANCE.verificationResult.complete(s)//继续登陆
        finish()
    }

/*

    /**
     * 解析出url参数中的键值对
     * 如 "Action=del&id=123"，解析出Action:del,id:123存入map中
     * @param strUrlParam url地址
     * @return url请求参数部分
     */

    fun urlParams(strUrlParam: String): Map<String, String> {
        val mapRequest: MutableMap<String, String> = HashMap()
        //每个键值为一组
        val arrSplit: Array<String> = strUrlParam.split("[&]")
        for (strSplit in arrSplit) {
            val arrSplitEqual: Array<String> = strSplit.split("[=]")
            //解析出键值
            if (arrSplitEqual.size > 1) { //正确解析
                mapRequest[arrSplitEqual[0]] = arrSplitEqual[1]
            } else {
                if (arrSplitEqual[0] !== "") { //只有参数没有值，不加入
                    mapRequest[arrSplitEqual[0]] = ""
                }
            }
        }
        return mapRequest
    }
*/


    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (unsafeLoginWeb.canGoBack()) {
                unsafeLoginWeb.goBack()
                return true
            }
        }
        return false
    }
    inner class Bridge {
        @JavascriptInterface
        fun invoke(cls: String?, method: String?, data: String?) {
            if (data != null) {
                val jsData = JSONObject(data)
                if (method == "onVerifyCAPTCHA") {
                    authFinish(jsData.getString("ticket"))
                }
            }
        }
    }
}