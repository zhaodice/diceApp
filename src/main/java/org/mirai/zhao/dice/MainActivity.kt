package org.mirai.zhao.dice

import android.Manifest
import android.app.*
import android.app.ActivityManager.RunningAppProcessInfo
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.*
import android.os.Environment.getExternalStorageDirectory
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import fightcent.permissionrequest.PermissionRequest
import fightcent.permissionrequest.PermissionRequestListener
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
    private var selfuin: String? = null
    private lateinit var switch_openDice: SwitchCompat
    private lateinit var switch_publicMode: SwitchCompat
    private lateinit var switch_keyAutoReply: SwitchCompat
    private lateinit var textview_selfuin:TextView
    private var dataLoaded = false
    private var editText_editValue: EditText? = null
    private lateinit var status: TextView
    private var adapter: SentencesAdapter? = null
    private var current_sentences_them: Sentences_them? = null
    private val checkUpdate: Button? = null
    private lateinit var notice: TextView
    private var settings_layout: ScrollView? = null
    private var loadedExtensionalOption=false
    var autoLoginFile // 自动登陆文件
            : File? = null
    private lateinit var button_changeQQ: Button
    private lateinit var reboot: Button
    private lateinit var button_autoLogin: Button
    private var isSelectAccountsShowing=false
    val storage //存储工具类（分离自cocHelper）
            : JsonConfigOperator by lazy{ JsonConfigOperator(AppContext.zhaoDiceData) }
    lateinit var qqArray //qq信息数组
            : Array<String?>
    var status_readDataOK //插件是否安装 QQ账号数据是否能读取
            = false

    companion object {
        private val spinner_values_text = ArrayList<Sentences_them>()

        init {
            if (spinner_values_text.size == 0) {
                spinner_values_text.add(Sentences_them("MASTER_INFO", "（master）骰主信息(文本)"))
                spinner_values_text.add(Sentences_them("MASTER_QQ", "（×）骰主QQ(一行一个)"))
                spinner_values_text.add(Sentences_them("DICE_NAME", "（×）骰娘姓名"))
                spinner_values_text.add(Sentences_them("WHITE_LIST", "（×）群白名单一行一个——清空全局有效"))
                spinner_values_text.add(Sentences_them("PREFIX", "（×）指令前缀"))
                //spinner_values_text.add(new Sentences_them("PATH_DRAW", "（×）自定义牌堆路径，默认值 draw,重启生效"));
                //spinner_values_text.add(new Sentences_them("PATH_PICTURES", "（×）自定义图片路径，默认值 pictures,重启生效"));
                //spinner_values_text.add(new Sentences_them("PATH_VOICE", "（×）自定义语音路径，默认值 sound_robot,重启生效"));
                //spinner_values_text.add(Sentences_them("REPLY", "（×）模糊词回复\n一行一个 关键词/内容 例:\n赵怡然/天才!"))
                spinner_values_text.add(Sentences_them("REPLY_EQU", "（×）匹配词回复\n一行一个 关键词/内容 例:\n赵怡然/天才!"))
                spinner_values_text.add(Sentences_them("DICE_DISMISS_AGREE", "（dismiss）dismiss退群成功"))
                spinner_values_text.add(Sentences_them("DICE_DISMISS_DENIED", "（dismiss）dismiss退群失败-没有权限"))
                spinner_values_text.add(Sentences_them("SENTENCE_LOG_OPEN", "（log on）聊天记录程序被打开"))
                spinner_values_text.add(Sentences_them("SENTENCE_LOG_CLOSE", "（log off）聊天记录程序被关闭"))
                spinner_values_text.add(Sentences_them("SENTENCE_LOG_DENIED", "（log）非masterQQ请求log被拒绝"))
                spinner_values_text.add(Sentences_them("SENTENCE_SETCOC_DENIED", "（setcoc）无权设置房规"))
                spinner_values_text.add(Sentences_them("SENTENCE_DRAW_FAILURE", "（draw/deck）牌堆抽取失败——牌堆找不到或出错"))
                spinner_values_text.add(Sentences_them("SENTENCE_DRAW_SUCCESS", "（draw/deck）牌堆抽取成功"))
                spinner_values_text.add(Sentences_them("SENTENCE_DICE_DENIED", "（bot/robot）无权开关骰子"))
                spinner_values_text.add(Sentences_them("SENTENCE_DICE_OPEN", "（bot/robot on）骰子被打开"))
                spinner_values_text.add(Sentences_them("SENTENCE_DICE_ROBOT_TEXT", "（bot/robot）BOT信息"))
                spinner_values_text.add(Sentences_them("SENTENCE_DICE_HELP_TEXT", "（help）HELP信息"))
                spinner_values_text.add(Sentences_them("SENTENCE_DICE_OPEN_ALREADY", "（bot/robot on/off）骰子已经打开或关闭,或bot指令非法"))
                spinner_values_text.add(Sentences_them("SENTENCE_DICE_CLOSE", "（bot/robot off）骰子被关闭"))
                spinner_values_text.add(Sentences_them("SENTENCE_BIG_FAILURE", "（ra/rb/rp/sc）骰出大失败"))
                spinner_values_text.add(Sentences_them("SENTENCE_FAILURE", "（ra/rb/rp/sc）骰出失败"))
                spinner_values_text.add(Sentences_them("SENTENCE_BIG_SUCCESS", "（ra/rb/rp/sc）骰出大成功"))
                spinner_values_text.add(Sentences_them("SENTENCE_VERY_HARD_SUCCESS", "（ra/rb/rp/sc）骰出极难成功"))
                spinner_values_text.add(Sentences_them("SENTENCE_HARD_SUCCESS", "（ra/rb/rp/sc）骰出困难成功"))
                spinner_values_text.add(Sentences_them("SENTENCE_SUCCESS", "（ra/rb/rp/sc）骰出成功"))
                spinner_values_text.add(Sentences_them("SENTENCE_ILLEGAL_TOO_MUCH", "（×）非法操作，超出资源限制"))
                spinner_values_text.add(Sentences_them("SENTENCE_ILLEGAL", "（×）非法操作，指令不合规"))
                spinner_values_text.add(Sentences_them("SENTENCE_ROLL", "（r）骰点"))
                spinner_values_text.add(Sentences_them("SENTENCE_HIDDEN_ROLL", "（rh）暗骰在群里说点啥"))
                spinner_values_text.add(Sentences_them("SENTENCE_CHANGE_NAME", "（nn）修改名字成功"))
                spinner_values_text.add(Sentences_them("SENTENCE_CHANGE_CARD", "（nn）设置现存档位成功"))
                spinner_values_text.add(Sentences_them("SENTENCE_GET_PAYER_INFO", "（stshow）获取玩家属性"))
                spinner_values_text.add(Sentences_them("SENTENCE_SET_PAYER_INFO", "（st）设置玩家属性"))
                spinner_values_text.add(Sentences_them("SENTENCE_JRRP", "（jrrp）今日人品"))
                spinner_values_text.add(Sentences_them("SENTENCE_PROMOTION_SUCCESS", "（en）技能成长鉴定成功"))
                spinner_values_text.add(Sentences_them("SENTENCE_PROMOTION_FAILURE", "（en）技能成长鉴定失败"))
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
                        is ViewGroup->when(v){
                            is Spinner->{
                                v.isClickable = enable
                                v.isEnabled = enable
                            }
                            is ListView->{
                                v.setClickable(enable)
                                v.setEnabled(enable)
                            }
                            else ->{
                                _setSubControlsEnable(v, enable)
                            }
                        }
                        is EditText->{
                            v.setEnabled(enable)
                            v.setClickable(enable)
                        }
                        is Button->{
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
        selfuin = shareData.getString("selfuin",null)
        val consoleButton:Button=findViewById(R.id.consoleEnter)
        consoleButton.setOnClickListener {
            val i = Intent(this@MainActivity, MiraiConsoleActivity::class.java)
            startActivity(i)
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
                current_sentences_them = adapter!!.getItem(position)
                doInterfaceUpdate()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        checkAndFresh()
    }
    private fun checkAndFresh(){
        //检测是否有读写权限
        try{
            val file=File(AppContext.miraiDir,"check")
            if(file.createNewFile()){
                activityFresh()
                file.delete()
            }else{
                doRequirePermission()
            }
        }catch (e:Throwable){
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
            val sdPath=getExternalStorageDirectory().path
            val oldStorage=File("$sdPath/miraiDice/plugins/ZhaoDice")
            val oldStorageRename=File("$sdPath/miraiDice/plugins/ZhaoDice_")
            if(oldStorage.exists()){
                FileService.copy(oldStorage.absolutePath,AppContext.zhaoDice)
                oldStorage.renameTo(oldStorageRename)
            }
            object : Thread() {
                override fun run() {
                    object :Thread(){
                        override fun run() {
                            sleep(5000)
                            startControlService(this@MainActivity)
                            super.run()
                        }
                    }.start()
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
                    spinner_values_text.add(Sentences_them(s2[0], s2[1]))
            }
            loadedExtensionalOption=true
        }
        autoLoginFile = File(AppContext.zhaoDiceData + "/autoLogin.txt")
        val zhaoDiceDataObj = File(AppContext.zhaoDiceData)
        if (!zhaoDiceDataObj.exists()) zhaoDiceDataObj.mkdirs()
        val files = zhaoDiceDataObj.listFiles()
        if (files != null) {
            val qqs = ArrayList<String>()
            for (file in files) {
                val filename = file.name
                if (filename.startsWith("Global_")) {
                    val qq = filename.substring(7, filename.indexOf("."))
                    if (TextUtils.isDigitsOnly(qq)) {
                        qqs.add(qq)
                    } else {
                        file.delete()
                    }
                }
            }
            if (qqs.size > 0) {
                if (qqs.size > 1) { //大于1个骰娘账号，让用户自己选择
                    qqArray = arrayOfNulls(qqs.size)
                    for (i in qqs.indices) {
                        qqArray[i] = qqs[i]
                    }
                    if(selfuin==null) {
                        selfuin = qqArray[0]
                        shareData.edit().putString("selfuin", selfuin).apply()
                    }
                    button_changeQQ.isEnabled = true
                } else {
                    //才一个骰娘号，不让选择
                    selfuin = qqs[0]
                }
                status_readDataOK = true
            } else {
                showAlterDialog("你需要登陆骰娘账号作为骰娘才能正常使用，请点击【骰娘账号管理】")
            }
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
                if (current_sentences_them != null && selfuin != null) {
                    Handler(Looper.getMainLooper()).post {
                        switch_publicMode.isChecked = storage.getGlobalBoolean(selfuin, "IS_PUBLIC_DICE")
                        switch_openDice.isChecked = storage.getGlobalBoolean(selfuin, "OPEN_IN_GLOBAL")
                        switch_keyAutoReply.isChecked = storage.getGlobalBoolean(selfuin, "KEY_AUTO_REPLY")
                    }
                    val sentence = storage.getGlobalInfo(selfuin, current_sentences_them!!.tag)
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
                MainActivityHand.setTextView(textview_selfuin,selfuin, -0x993301)
                super.run()
            }
        }.start()
    }

    private fun doShowQQSelection(context: MainActivity) {
        if(this::qqArray.isInitialized) {
            if(!isSelectAccountsShowing) {
                synchronized(this){
                    val builder = AlertDialog.Builder(context, android.R.style.Theme_Material_Light_Dialog)
                    builder.setTitle("请选择已经登陆的骰娘QQ号")
                    builder.setItems(qqArray) { _, which ->
                        context.selfuin = qqArray[which]
                        shareData.edit().putString("selfuin", selfuin).apply()
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
            Toast.makeText(this,"没有更多账号可以切换",Toast.LENGTH_SHORT).show()
        }
    }

    private fun doInterfaceDataSaving(): Boolean {
        if (dataLoaded) {
            storage.saveGlobalBoolean(selfuin, "IS_PUBLIC_DICE", switch_publicMode.isChecked)
            storage.saveGlobalBoolean(selfuin, "OPEN_IN_GLOBAL", switch_openDice.isChecked)
            storage.saveGlobalBoolean(selfuin, "KEY_AUTO_REPLY", switch_keyAutoReply.isChecked)
            if (current_sentences_them != null) storage.saveGlobalInfo(selfuin, current_sentences_them!!.tag, editText_editValue!!.text.toString())
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