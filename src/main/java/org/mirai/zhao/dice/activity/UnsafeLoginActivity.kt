package org.mirai.zhao.dice.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.KeyEvent
import android.webkit.*
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.mirai.zhao.dice.R
import org.mirai.zhao.dice.console.AndroidLoginSolver


class UnsafeLoginActivity : AppCompatActivity() {
    private lateinit var unsafeLoginWeb : WebView
    private lateinit var refreshUnsafeWeb: SwipeRefreshLayout
    private lateinit var forceStopUnsafeLogin:Button
    /*companion object{
        private const val disguisedUserAgent =
                "Mozilla/5.0 (Linux; Android 5.1; vivo X6Plus D Build/LMY47I; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/77.0.3865.120 MQQBrowser/6.2 TBS/045330 Mobile Safari/537.36 V1_AND_SQ_7.1.0_0_TIM_D TIM/3.0.0.2858 QQ/6.5.5  NetType/WIFI WebP/0.3.0 Pixel/1080"
    }*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_unsafe_login)
        refreshUnsafeWeb=findViewById(R.id.refresh_unsafe_web)
        unsafeLoginWeb=findViewById(R.id.unsafe_login_web)
        forceStopUnsafeLogin=findViewById(R.id.forceStopUnsafeLogin)
        forceStopUnsafeLogin.setOnClickListener{
            authFinish("")
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


    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebView() {
        //滑块验证码js注入，便于获取ticks
        val insertJavaScript ="!(()=>{let prompt=window.prompt;function processUrl(url){let prefix=\"jsbridge://CAPTCHA/onVerifyCAPTCHA?p=\"if(url.startsWith(prefix)){let json=url.substring(prefix.length);for(let i=json.length;i--;i>0){let j=json.substr(0,i)console.log(j);try{let content=decodeURIComponent(j);let obj=JSON.parse(content);console.log(obj);window.miraiSeleniumComplete=content;prompt(\"MiraiSelenium - ticket\",obj.ticket)break}catch(ignore){}}return true}return false}(()=>{let desc=Object.getOwnPropertyDescriptor(Image.prototype,\"src\");Object.defineProperty(Image.prototype,\"src\",{get:desc.get,set(v){if(processUrl(v))return;desc.set.call(this,v)}})})();(()=>{let desc=Object.getOwnPropertyDescriptor(HTMLIFrameElement.prototype,\"src\");Object.defineProperty(HTMLIFrameElement.prototype,\"src\",{get:desc.get,set(v){if(processUrl(v))return;desc.set.call(this,v)}})})()})()"
        unsafeLoginWeb.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                if (url != null && view != null) {
                    if (url.startsWith("https://ssl.captcha.qq.com/template/wireless_mqq_captcha.html")) {//滑块验证码
                        view.postDelayed({ view.evaluateJavascript(insertJavaScript, null) }, 1200)
                    }
                }
                super.onPageFinished(view, url)
            }
            override fun shouldInterceptRequest(view: WebView, webResourceRequest: WebResourceRequest): WebResourceResponse? {

                val u = webResourceRequest.url
                //Log.d("zhaodice", "zhaodice u.protocol=" + u.scheme + " u.path=" + u.path + " u.query=" + u.query)
                if (u.scheme == "jsbridge") {
                    if (u.path == "/openUrl") {
                        val query=u.query
                        if(query!=null) {
                            val json=query.substring(2)//去掉p=
                            //Log.d("zhaodice","zhaodice-B-"+json)
                            try {
                                val goUrl=JSONObject(json).getString("url")
                                view.post {
                                    unsafeLoginWeb.loadUrl(goUrl)
                                }
                                //.d("zhaodice","zhaodice-C-"+goUrl)
                            }catch (e:Throwable){
                                //Log.d("zhaodice","zhaodice-C-"+e.message)
                            }
                        }
                    }
                }

                return super.shouldInterceptRequest(view, webResourceRequest)
            }


//            override fun shouldInterceptRequest(
//                view: WebView?,
//                request: WebResourceRequest?
//            ): WebResourceResponse? {
//                if (request != null) {
//                    if ("https://report.qqweb.qq.com/report/compass/dc00898" in request.url.toString()) {
//                        authFinish()
//                    }
//                }
//                return super.shouldInterceptRequest(view, request)
//            }
        }

        unsafeLoginWeb.webChromeClient = object : WebChromeClient() {
            override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                // 按下回到qq按钮之后会打印这句话，于是就用这个解决了。。。。
                if (consoleMessage?.message()?.startsWith("手Q扫码验证") == true) {
                    authFinish("")
                }
                return super.onConsoleMessage(consoleMessage)
            }

            override fun onJsPrompt(view: WebView?, url: String?, message: String?, defaultValue: String?, result: JsPromptResult?): Boolean {
                //检测到滑块验证码滑动完成，反馈ticks
                if("MiraiSelenium - ticket"==message &&result!=null&& defaultValue!=null){
                    result.confirm("received")
                    authFinish(defaultValue)
                    return true
                }
                return super.onJsPrompt(view, url, message, defaultValue, result)
            }
        }
        unsafeLoginWeb.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            //userAgentString=disguisedUserAgent
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
        val arrSplit: Array<String> = strUrlParam.split("[&]").toTypedArray()
        for (strSplit in arrSplit) {
            val arrSplitEqual: Array<String> = strSplit.split("[=]").toTypedArray()
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
}