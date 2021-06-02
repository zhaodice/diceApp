package org.mirai.zhao.dice

import android.Manifest
import android.annotation.SuppressLint
import android.app.*
import android.app.ActivityManager.RunningAppProcessInfo
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import fightcent.permissionrequest.PermissionRequest
import fightcent.permissionrequest.PermissionRequestListener
import org.mirai.zhao.dice.activity.AccountsActivity
import org.mirai.zhao.dice.activity.MiraiConsoleActivity
import org.mirai.zhao.dice.activity.ui.login.LoginActivity
import org.mirai.zhao.dice.console.ConsoleService.Companion.startControlService
import org.mirai.zhao.dice.file.FileService
import org.mirai.zhao.dice.file.JsonConfigOperator
import org.mirai.zhao.dice.file.TextFileOperator.read
import java.io.File
import java.util.*


class MainActivity : AppCompatActivity() {
    var alterDialog: AlertDialog? = null
    private var currentEditQQ
        get() = AppContext.currentEditQQ
        set(value) {
            AppContext.currentEditQQ = value
            storage = JsonConfigOperator(AppContext.zhaoDiceData)
        }
    private lateinit var switch_openDice: SwitchCompat
    private lateinit var switch_publicMode: SwitchCompat
    private lateinit var switch_keyAutoReply: SwitchCompat
    private lateinit var textview_selfuin:TextView
    private var dataLoaded = false
    private var editText_editValue: EditText? = null
    private lateinit var status: TextView
    private var adapter: SentencesAdapter? = null
    private var current_sentencesThem: SentencesThem? = null
    private val checkUpdate: Button? = null
    private lateinit var notice: TextView
    private var settings_layout: ScrollView? = null
    private var loadedExtensionalOption=false
    private lateinit var button_changeQQ: Button
    private lateinit var reboot: Button
    private lateinit var button_autoLogin: Button
    private var isSelectAccountsShowing=false
    private var storage //存储工具类（分离自cocHelper）
            : JsonConfigOperator = JsonConfigOperator(AppContext.zhaoDiceData)
    private var qqArray = AccountsActivity.getAccountsList() //qq信息数组

    var status_readDataOK //插件是否安装 QQ账号数据是否能读取
            = false

    companion object {
        private val spinner_values_text = ArrayList<SentencesThem>()

        init {
            if (spinner_values_text.size == 0) {
                spinner_values_text.add(SentencesThem("MASTER_INFO", "（master）骰主信息(文本)"))
                spinner_values_text.add(SentencesThem("MASTER_QQ", "（×）骰主QQ(一行一个)"))
                spinner_values_text.add(SentencesThem("DICE_NAME", "（×）骰娘姓名"))
                spinner_values_text.add(SentencesThem("WHITE_LIST", "（×）群白名单一行一个——清空全局有效"))
                spinner_values_text.add(SentencesThem("PREFIX", "（×）指令前缀"))
                spinner_values_text.add(SentencesThem("REPLY_EQU", "（×）匹配词回复\n一行一个 关键词/内容 例:\n赵怡然/天才!"))
                spinner_values_text.add(SentencesThem("DICE_DISMISS_AGREE", "（dismiss）dismiss退群成功"))
                spinner_values_text.add(SentencesThem("DICE_DISMISS_DENIED", "（dismiss）dismiss退群失败-没有权限"))
                spinner_values_text.add(SentencesThem("SENTENCE_LOG_OPEN", "（log on）聊天记录程序被打开"))
                spinner_values_text.add(SentencesThem("SENTENCE_LOG_CLOSE", "（log off）聊天记录程序被关闭"))
                spinner_values_text.add(SentencesThem("SENTENCE_LOG_DENIED", "（log）非masterQQ请求log被拒绝"))
                spinner_values_text.add(SentencesThem("SENTENCE_SETCOC_DENIED", "（setcoc）无权设置房规"))
                spinner_values_text.add(SentencesThem("SENTENCE_DRAW_FAILURE", "（draw/deck）牌堆抽取失败——牌堆找不到或出错"))
                spinner_values_text.add(SentencesThem("SENTENCE_DRAW_SUCCESS", "（draw/deck）牌堆抽取成功"))
                spinner_values_text.add(SentencesThem("SENTENCE_DICE_DENIED", "（bot/robot）无权开关骰子"))
                spinner_values_text.add(SentencesThem("SENTENCE_DICE_OPEN", "（bot/robot on）骰子被打开"))
                spinner_values_text.add(SentencesThem("SENTENCE_DICE_ROBOT_TEXT", "（bot/robot）BOT信息"))
                spinner_values_text.add(SentencesThem("SENTENCE_DICE_HELP_TEXT", "（help）HELP信息"))
                spinner_values_text.add(SentencesThem("SENTENCE_DICE_OPEN_ALREADY", "（bot/robot on/off）骰子已经打开或关闭,或bot指令非法"))
                spinner_values_text.add(SentencesThem("SENTENCE_DICE_CLOSE", "（bot/robot off）骰子被关闭"))
                spinner_values_text.add(SentencesThem("SENTENCE_BIG_FAILURE", "（ra/rb/rp/sc）骰出大失败"))
                spinner_values_text.add(SentencesThem("SENTENCE_FAILURE", "（ra/rb/rp/sc）骰出失败"))
                spinner_values_text.add(SentencesThem("SENTENCE_BIG_SUCCESS", "（ra/rb/rp/sc）骰出大成功"))
                spinner_values_text.add(SentencesThem("SENTENCE_VERY_HARD_SUCCESS", "（ra/rb/rp/sc）骰出极难成功"))
                spinner_values_text.add(SentencesThem("SENTENCE_HARD_SUCCESS", "（ra/rb/rp/sc）骰出困难成功"))
                spinner_values_text.add(SentencesThem("SENTENCE_SUCCESS", "（ra/rb/rp/sc）骰出成功"))
                spinner_values_text.add(SentencesThem("SENTENCE_ILLEGAL_TOO_MUCH", "（×）非法操作，超出资源限制"))
                spinner_values_text.add(SentencesThem("SENTENCE_ILLEGAL", "（×）非法操作，指令不合规"))
                spinner_values_text.add(SentencesThem("SENTENCE_ROLL", "（r）骰点"))
                spinner_values_text.add(SentencesThem("SENTENCE_HIDDEN_ROLL", "（rh）暗骰在群里说点啥"))
                spinner_values_text.add(SentencesThem("SENTENCE_CHANGE_NAME", "（nn）修改名字成功"))
                spinner_values_text.add(SentencesThem("SENTENCE_CHANGE_CARD", "（nn）设置现存档位成功"))
                spinner_values_text.add(SentencesThem("SENTENCE_GET_PAYER_INFO", "（stshow）获取玩家属性"))
                spinner_values_text.add(SentencesThem("SENTENCE_SET_PAYER_INFO", "（st）设置玩家属性"))
                spinner_values_text.add(SentencesThem("SENTENCE_JRRP", "（jrrp）今日人品"))
                spinner_values_text.add(SentencesThem("SENTENCE_PROMOTION_SUCCESS", "（en）技能成长鉴定成功"))
                spinner_values_text.add(SentencesThem("SENTENCE_PROMOTION_FAILURE", "（en）技能成长鉴定失败"))
            }
        }
    }

    @SuppressLint("BatteryLife")
    private fun ignoreBatteryOptimization(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = activity.getSystemService(Context.POWER_SERVICE) as PowerManager
            //  判断当前APP是否有加入电池优化的白名单，如果没有，弹出加入电池优化的白名单的设置对话框。
            val hasIgnored = powerManager.isIgnoringBatteryOptimizations(activity.packageName)
            if (!hasIgnored) {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                intent.data = Uri.parse("package:" + activity.packageName)
                startActivity(intent)
            } else {
                Toast.makeText(activity, "您已授权忽略电池优化", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private val switchCheckListener = View.OnClickListener { doInterfaceDataSaving() }

    internal class MainActivityHand(mainLooper: Looper?) : Handler(mainLooper!!) {
        override fun handleMessage(msg: Message) {
            if (msg.what == 1) {
                val data = msg.data
                val view = msg.obj as ViewGroup
                val enable = data.getBoolean("enable")
                _setSubControlsEnable(view, enable)
            } else if (msg.what == 2) {
                val data = msg.data
                val view = msg.obj as TextView
                val content = data.getString("content")
                val color = data.getInt("color")
                view.text = content
                view.setTextColor(color)
            } else if (msg.what == 3) {
                val data = msg.data
                val view = msg.obj as EditText
                val content = data.getString("content")
                view.setText(content)
            }
            super.handleMessage(msg)
        }

        companion object {
            /**
             * 遍历布局，并禁用所有子控件
             *
             * @param viewGroup
             * 布局对象
             */
            private fun _setSubControlsEnable(viewGroup: ViewGroup, enable: Boolean) {
                for (i in 0 until viewGroup.childCount) {
                    when(val v = viewGroup.getChildAt(i)){
                        is ViewGroup -> when (v) {
                            is Spinner -> {
                                v.isClickable = enable
                                v.isEnabled = enable
                            }
                            is ListView -> {
                                v.setClickable(enable)
                                v.setEnabled(enable)
                            }
                            else -> {
                                _setSubControlsEnable(v, enable)
                            }
                        }
                        is EditText -> {
                            v.setEnabled(enable)
                            v.setClickable(enable)
                        }
                        is Button -> {
                            if ("enable" != v.getTag()) v.setEnabled(enable)
                        }
                    }
                }
            }

            fun setSubControlsEnable(view: View?, enable: Boolean) {
                val msg = Message()
                val data = Bundle()
                msg.obj = view
                data.putBoolean("enable", enable)
                msg.what = 1
                msg.data = data
                MainActivityHand(view!!.context.mainLooper).sendMessage(msg)
            }

            fun setTextView(view: TextView?, content: String?, color: Int) {
                val msg = Message()
                val data = Bundle()
                msg.obj = view
                data.putString("content", content)
                data.putInt("color", color)
                msg.what = 2
                msg.data = data
                MainActivityHand(view!!.context.mainLooper).sendMessage(msg)
            }

            fun setEditText(view: EditText?, content: String?) {
                val msg = Message()
                val data = Bundle()
                msg.obj = view
                data.putString("content", content)
                msg.what = 3
                msg.data = data
                MainActivityHand(view!!.context.mainLooper).sendMessage(msg)
            }
        }
    }

    /**
     * App前后台状态
     */
    var isForeground = false
    private var notice_switch //提示切换变量
            = false

    override fun onResume() {
        super.onResume()
        if (!isForeground) {
            //由后台切换到前台
            isForeground = true
        }
    }

    override fun onPause() {
        if (!isAppOnForeground) {
            //由前台切换到后台
            isForeground = false
            if (doInterfaceDataSaving()) Toast.makeText(this, "已智能自动保存", Toast.LENGTH_SHORT).show()
            finish()
        }
        super.onPause()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 0) activityFresh()
        //doInterfaceUpdate(1000);
        super.onActivityResult(requestCode, resultCode, data)
    }

    private lateinit var shareData: SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        shareData = getSharedPreferences("app", MODE_PRIVATE)
        currentEditQQ = shareData.getString("selfuin", null)
        val consoleButton:Button=findViewById(R.id.consoleEnter)
        consoleButton.setOnClickListener {
            val i = Intent(this@MainActivity, MiraiConsoleActivity::class.java)
            startActivity(i)
        }
        findViewById<Button>(R.id.keeplive).setOnClickListener{
            ignoreBatteryOptimization(this)
        }
        textview_selfuin=findViewById(R.id.selfuin)
        switch_openDice = findViewById(R.id.switch_openDice)
        switch_publicMode = findViewById(R.id.switch_publicMode)
        switch_keyAutoReply = findViewById(R.id.switch_keyAutoReply)
        val switch_bootStart = findViewById<SwitchCompat>(R.id.switch_bootStart)
        val spinner_values = findViewById<Spinner>(R.id.spinner_values)
        editText_editValue = findViewById(R.id.editText_editValue)
        button_changeQQ = findViewById(R.id.changeQQ)
        val button_saveData = findViewById<Button>(R.id.saveData)
        //Button button_startMirai = findViewById(R.id.startMirai);
        button_autoLogin = findViewById(R.id.autoLogin)
        reboot = findViewById(R.id.reboot)
        settings_layout = findViewById(R.id.settings_layout)
        MainActivityHand.setSubControlsEnable(settings_layout, false)
        notice = findViewById(R.id.notice)
        status = findViewById(R.id.status)
        status.setTextColor(Color.RED)
        notice.setOnClickListener { view ->
            val A = getString(R.string.notice_1)
            val B = getString(R.string.notice_2)
            val v = view as TextView
            if (!notice_switch.also { notice_switch = it }) {
                v.text = B
            } else {
                v.text = A
            }
        }
        reboot.setOnClickListener {
            val intent = baseContext.packageManager.getLaunchIntentForPackage(baseContext.packageName)
            val restartIntent = PendingIntent.getActivity(applicationContext, 0, intent, PendingIntent.FLAG_ONE_SHOT)
            val mgr = getSystemService(ALARM_SERVICE) as AlarmManager
            mgr[AlarmManager.RTC, System.currentTimeMillis() + 1000] = restartIntent
            Process.killProcess(Process.myPid())
        }
        button_autoLogin.setOnClickListener { view ->
            val i = Intent(view.context, LoginActivity::class.java)
            startActivityForResult(i, 0)
        }
        button_changeQQ.setOnClickListener {
            qqArray = AccountsActivity.getAccountsList()//更新QQ信息
            doShowQQSelection(this@MainActivity)
        }
        button_saveData.setOnClickListener { if (doInterfaceDataSaving()) Toast.makeText(this@MainActivity, "存好了！", Toast.LENGTH_LONG).show() }
        switch_openDice.setOnClickListener(switchCheckListener)
        switch_publicMode.setOnClickListener(switchCheckListener)
        switch_keyAutoReply.setOnClickListener(switchCheckListener)
        switch_bootStart.setOnCheckedChangeListener { _, b -> shareData.edit().putBoolean("bootStart", b).apply() }
        switch_bootStart.isChecked = shareData.getBoolean("bootStart", true)
        adapter = SentencesAdapter(this, spinner_values_text)
        spinner_values.adapter = adapter
        spinner_values.setOnLongClickListener { false }
        spinner_values.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
                doInterfaceDataSaving()
                current_sentencesThem = adapter!!.getItem(position)
                doInterfaceUpdate()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        checkAndFresh()
    }
    private fun checkAndFresh(){
        //检测是否有读写权限
        try{
            val file=File(AppContext.miraiDir, "check")
            file.parentFile?.mkdirs()
            if(file.exists()||file.createNewFile()){
                activityFresh()
                file.delete()
            }else{
                doRequirePermission()
            }
        }catch (e: Throwable){
            doRequirePermission()
        }
    }
    /**
     * 获取Android设备中所有正在运行的App
     */
    private val isAppOnForeground: Boolean
        get() {
            val activityManager = applicationContext
                    .getSystemService(ACTIVITY_SERVICE) as ActivityManager
            val packageName = applicationContext.packageName
            /**
             * 获取Android设备中所有正在运行的App
             */
            /**
             * 获取Android设备中所有正在运行的App
             */
            val appProcesses = activityManager
                    .runningAppProcesses ?: return false
            for (appProcess in appProcesses) {
                // The name of the process that this object is associated with.
                if (appProcess.processName == packageName && appProcess.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    return true
                }
            }
            return false
        }

    private fun doRequirePermission() {
        PermissionRequest(this).request(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), object : PermissionRequestListener {
            private fun noPermission() {
                showAlterDialog("没有存储权限，程序不能正常管理赵骰的数据文件。")
            }

            override fun onAllowAllPermissions() {
                activityFresh()
            }

            override fun onDenySomePermissions(denyPermissions: Collection<String>) {
                noPermission()
            }

            override fun onDenyAndNeverAskAgainSomePermissions(denyAndNeverAskAgainPermissions: Collection<String>) {
                noPermission()
            }
        })
        //permissionLoaded();
    }

    private fun activityFresh() {
        if (AppContext.consoleService == null) {
            val sdPath=AppContext.dataStorage
            val oldStorage=File("$sdPath/miraiDice/plugins/ZhaoDice")
            val oldStorageRename=File("$sdPath/miraiDice/plugins/ZhaoDice_")
            val newStorage=File(AppContext.zhaoDice)
            if(!newStorage.exists())
                newStorage.mkdirs()
            if(oldStorage.exists()&&newStorage.exists()){
                FileService.copy(oldStorage.absolutePath, newStorage.absolutePath)
                oldStorage.renameTo(oldStorageRename)
            }
            object : Thread() {
                override fun run() {
                    startControlService(this@MainActivity)
                    super.run()
                }
            }.start()
        }
        if(!loadedExtensionalOption) {
            val s = read(File(AppContext.zhaoDiceData + "/extensionalOption.txt")).split("\n")
            for (value in s) {
                val s2: List<String> = value.split(" ")
                if (s2.size == 2)
                    spinner_values_text.add(SentencesThem(s2[0], s2[1]))
            }
            loadedExtensionalOption=true
        }
        val accounts = AccountsActivity.getAccountsList()
        if (accounts.size>0) {
            if (accounts.size>1) { //大于1个骰娘账号，让用户自己选择
                if(currentEditQQ==null) {
                    currentEditQQ = qqArray[0]
                    shareData.edit().putString("selfuin", currentEditQQ).apply()
                }
                button_changeQQ.isEnabled = true
            } else {
                //才一个骰娘号，不让选择
                currentEditQQ = qqArray[0]
            }
            status_readDataOK = true
            if(!shareData.getBoolean("readNotice", false)) {
                shareData.edit().putBoolean("readNotice", true).apply()
                val alterDialog = AlertDialog.Builder(this@MainActivity)
                alterDialog.setTitle("第一次使用必读")
                alterDialog.setMessage("APP默认情况下可能会被系统杀后台，无法长期挂机，如有需要长时间稳定运行，请自行搜索自己手机型号后台白名单相关设置(包括省电，清内存等等)，如百度：“华为后台设置教程”")
                alterDialog.setPositiveButton("确认") { dialogInterface, _ ->
                    ignoreBatteryOptimization(this)
                    dialogInterface.cancel()
                }
                alterDialog.create().show()
            }
        } else {
            showAlterDialog("你需要登陆骰娘账号作为骰娘才能正常使用，请点击【骰娘账号管理】")
        }
        doInterfaceUpdate()
    }

    private fun doInterfaceUpdate(delay: Long = 0) { //更新UI界面
        object : Thread() {
            override fun run() {
                if (delay > 0) {
                    try {
                        sleep(delay)
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }
                }
                if (current_sentencesThem != null && currentEditQQ != null) {
                    val id=currentEditQQ as String
                    Handler(Looper.getMainLooper()).post {
                        switch_publicMode.isChecked = storage.getGlobalBoolean(id, "IS_PUBLIC_DICE")
                        switch_openDice.isChecked = storage.getGlobalBoolean(id, "OPEN_IN_GLOBAL")
                        switch_keyAutoReply.isChecked = storage.getGlobalBoolean(id, "KEY_AUTO_REPLY")
                    }
                    val sentence = storage.getGlobalInfo(id, current_sentencesThem!!.tag)
                    //editText_editValue.setText(sentence);
                    MainActivityHand.setEditText(editText_editValue, sentence)
                    dataLoaded = true
                }
                if (status_readDataOK) {
                    MainActivityHand.setTextView(status, "一切正常！控制台正常工作\n温馨提示:在其他端登陆骰娘账号可能导致本系统不稳定\n请操作完毕后【退出其他端登陆的骰娘账号】并【点击重启APP按钮】", -0x993301)
                    MainActivityHand.setSubControlsEnable(settings_layout, true)
                } else {
                    if (!status_readDataOK) MainActivityHand.setTextView(status, "错误：QQ账号未正确登陆", Color.RED)
                }
                MainActivityHand.setTextView(textview_selfuin, currentEditQQ, -0x993301)
                super.run()
            }
        }.start()
    }

    private fun doShowQQSelection(context: MainActivity) {
        if(qqArray.size>1) {
            if(!isSelectAccountsShowing) {
                synchronized(this){
                    val view=if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP)
                        android.R.style.Theme_Material_Light_Dialog
                    else
                        android.R.style.Theme_DeviceDefault_Light_Dialog
                    val builder = AlertDialog.Builder(context, view)
                    builder.setTitle("请选择已经登陆的骰娘QQ号")
                    builder.setItems(Array(qqArray.size){
                        qqArray[it]
                    }) { _, which ->
                        context.currentEditQQ = qqArray[which]
                        shareData.edit().putString("selfuin", currentEditQQ).apply()
                        doInterfaceUpdate()
                        isSelectAccountsShowing = false
                    }
                    builder.setOnCancelListener {
                        isSelectAccountsShowing = false
                    }
                    builder.show()
                    isSelectAccountsShowing = true
                }
            }
        }else{
            Toast.makeText(this, "没有更多账号可以切换", Toast.LENGTH_SHORT).show()
        }
    }

    private fun doInterfaceDataSaving(): Boolean {
        if (dataLoaded) {
            val id=currentEditQQ as String
            storage.saveGlobalBoolean(id, "IS_PUBLIC_DICE", switch_publicMode.isChecked)
            storage.saveGlobalBoolean(id, "OPEN_IN_GLOBAL", switch_openDice.isChecked)
            storage.saveGlobalBoolean(id, "KEY_AUTO_REPLY", switch_keyAutoReply.isChecked)
            if (current_sentencesThem != null)
                storage.saveGlobalInfo(id, current_sentencesThem!!.tag, editText_editValue!!.text.toString())
            return true
        }
        return false
    }

    private fun showAlterDialog(content: String) {
        val alterDialog = AlertDialog.Builder(this)
        alterDialog.setTitle("错误")
        alterDialog.setMessage(content)
        alterDialog.setPositiveButton("知道真相后离开") { dialogInterface, _ -> dialogInterface.cancel() }
        alterDialog.setNegativeButton("骰娘账号管理") { _, _ ->
            val k = Intent(this@MainActivity, LoginActivity::class.java)
            startActivityForResult(k, 0)
        }
        alterDialog.setOnCancelListener { finish() }
        this.alterDialog = alterDialog.create()
        alterDialog.show()
    }

    override fun onDestroy() {
        if (alterDialog != null) alterDialog!!.dismiss()
        super.onDestroy()
    }
}