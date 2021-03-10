package org.mirai.zhao.dice.activity

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Html
import android.view.View
import android.widget.Button
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import org.mirai.zhao.dice.AppContext
import org.mirai.zhao.dice.R
import org.mirai.zhao.dice.console.ConsoleService
import org.mirai.zhao.dice.console.OnLogChangedListener


class MiraiConsoleActivity : Activity() {
    var autoRoll = false
    override fun onPause() {
        autoRoll = false
        super.onPause()
    }

    override fun onResume() {
        autoRoll = true
        super.onResume()
    }

    lateinit var tv: TextView
    private lateinit var btn: Button
    lateinit var scrollView:ScrollView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mirai_console)
        autoRoll = true
        scrollView = findViewById(R.id.scrollView)
        tv = findViewById(R.id.textView)
        btn = findViewById(R.id.lockRoll)
        btn.setOnClickListener{
            autoRoll=!autoRoll
            if(autoRoll){
                btn.text = "点击禁止滚动"
            }else{
                btn.text = "点击允许滚动"
            }
        }
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M) {
            scrollView.setOnScrollChangeListener { _: View, _: Int, _: Int, _: Int, _: Int ->
                val contentView: View = scrollView.getChildAt(0)
                if (contentView.measuredHeight == scrollView.scrollY + scrollView.height) {
                    btn.visibility = View.VISIBLE
                } else {
                    btn.visibility = View.INVISIBLE
                }
            }
        }
        val console = (this.applicationContext as AppContext).consoleService
        if (console == null) {
            Toast.makeText(this, "哎呀，mirai服务还没准备好，再多等一会吧>_<，一般30秒内ok~", Toast.LENGTH_LONG).show()
            finish()
        } else {
            ConsoleService.onLogChangedListener = object : OnLogChangedListener {
                override fun logChanged(text: String) {
                    Handler(Looper.getMainLooper()).post {
                        if (autoRoll) {
                            scrollView.fullScroll(ScrollView.FOCUS_DOWN)
                        }
                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                            tv.append(Html.fromHtml(text,Html.FROM_HTML_MODE_LEGACY))
                        else
                            tv.append(Html.fromHtml(text))
                        tv.append(Html.fromHtml("<br/>"))
                    }
                }
            }
            val scrollView = findViewById<ScrollView>(R.id.scrollView)
            scrollView.fullScroll(ScrollView.FOCUS_DOWN)
            if (ConsoleService.androidMiraiLogger != null)
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                    tv.text = Html.fromHtml(ConsoleService.androidMiraiLogger!!.logStorage.build(),Html.FROM_HTML_MODE_LEGACY)
                else
                    tv.text = Html.fromHtml(ConsoleService.androidMiraiLogger!!.logStorage.build())
        }
        val scrollView = findViewById<ScrollView>(R.id.scrollView)
        scrollView.fullScroll(ScrollView.FOCUS_DOWN)
        //tv.setText(ConsoleService.frontEnd.getMainLogger().toString());
    }
}