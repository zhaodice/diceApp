package org.mirai.zhao.dice.activity.ui.login

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.Html
import android.text.Spanned
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import net.mamoe.mirai.message.data.Dice
import net.mamoe.mirai.utils.BotConfiguration
import org.mirai.zhao.dice.R
import org.mirai.zhao.dice.activity.AccountsActivity
import org.mirai.zhao.dice.console.ConsoleService
import org.mirai.zhao.dice.console.OnLogChangedListener

class LoginActivity : AppCompatActivity() {
    //private lateinit var loginProcessPrint: TextView
    private lateinit var loginViewModel: LoginViewModel
    private var miraiProtocol:BotConfiguration.MiraiProtocol=BotConfiguration.MiraiProtocol.ANDROID_PHONE
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)

        ConsoleService.startControlService(this)
        val username = findViewById<EditText>(R.id.username)
        val password = findViewById<EditText>(R.id.password)
        val login = findViewById<Button>(R.id.login)
        val loginProcessPrint = findViewById<TextView>(R.id.loginProcessPrint)
        val loading = findViewById<ProgressBar>(R.id.loading)
        val deleteAccounts = findViewById<Button>(R.id.deleteAccounts)
        val radioGroup = findViewById<RadioGroup>(R.id.radioGroup)
        val protocolWatch = findViewById<RadioButton>(R.id.protocol_watch)
        val protocolPad = findViewById<RadioButton>(R.id.protocol_pad)
        val protocolAndroidPhone = findViewById<RadioButton>(R.id.protocol_android_phone)
        radioGroup.setOnCheckedChangeListener{ _: RadioGroup, _: Int ->
            miraiProtocol=when{
                protocolAndroidPhone.isChecked -> BotConfiguration.MiraiProtocol.ANDROID_PHONE
                protocolWatch.isChecked -> BotConfiguration.MiraiProtocol.ANDROID_WATCH
                protocolPad.isChecked -> BotConfiguration.MiraiProtocol.ANDROID_PAD
                else -> BotConfiguration.MiraiProtocol.ANDROID_PAD
            }
        }
        ConsoleService.onLogChangedListener = object : OnLogChangedListener {
            val maxLogLine = 10
            val logs=ArrayList<Spanned>()
            init{
                loginProcessPrint.visibility = View.GONE
                loginProcessPrint.text = ""
                repeat(maxLogLine){
                    logs.add(Html.fromHtml(""))
                }
            }
            override fun logChanged(text: String) {
                Handler(Looper.getMainLooper()).post {
                    if(loginProcessPrint.visibility == View.VISIBLE) {
                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                            logs.add(Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY))
                        else
                            logs.add(Html.fromHtml(text))
                        if(logs.size>=maxLogLine){
                            logs.removeAt(0)
                        }
                        loginProcessPrint.text = ""
                        for(log in logs){
                            loginProcessPrint.append(log)
                            loginProcessPrint.append(Html.fromHtml("<br/>"))
                        }
                    }
                }
            }
        }
        loginViewModel = ViewModelProvider(this, LoginViewModelFactory(this)).get(LoginViewModel::class.java)

        loginViewModel.loginFormState.observe(this@LoginActivity, Observer {
            val loginState = it ?: return@Observer
            loading.visibility = View.GONE
            loginProcessPrint.visibility = View.GONE
            // disable login button unless both username / password is valid
            login.isEnabled = loginState.isDataValid

            if (loginState.usernameError != null) {
                username.error = getString(loginState.usernameError)
            }
            if (loginState.passwordError != null) {
                val str = getString(loginState.passwordError)
                var lineNum =0
                str.toCharArray().forEach { char->
                    if(char=='\n'){
                        lineNum++
                    }
                }
                if(lineNum>2){
                    val alterDialog = AlertDialog.Builder(this)
                    alterDialog.setTitle("错误")
                    alterDialog.setMessage(loginState.serverResult+'\n'+str)
                    alterDialog.setPositiveButton("确定") { dialogInterface, _ -> dialogInterface.cancel() }
                    alterDialog.show()
                    password.error = "登陆出错"
                }else{
                    if (loginState.serverResult != null) {
                        password.error = loginState.serverResult
                    } else {
                        password.error = str
                    }
                }
            }
            login.isEnabled = true
        })

        loginViewModel.loginResult.observe(this@LoginActivity, Observer {
            val loginResult = it ?: return@Observer
            loginProcessPrint.visibility = View.GONE
            loading.visibility = View.GONE
            login.isEnabled = true
            if (loginResult.error != null) {
                showLoginFailed(loginResult.error)
            } else {
                updateUiWithUser()
            }
            setResult(Activity.RESULT_OK)

            //Complete and destroy login activity once successful
            finish()
        })

        username.afterTextChanged {
            loginViewModel.loginDataChanged(
                    username.text.toString(),
                    password.text.toString()
            )
        }
        deleteAccounts.setOnClickListener{
            val i=Intent(this@LoginActivity, AccountsActivity::class.java)
            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(i)
        }
        password.apply {
            afterTextChanged {
                loginViewModel.loginDataChanged(
                        username.text.toString(),
                        password.text.toString()
                )
            }

            setOnEditorActionListener { _, actionId, _ ->
                when (actionId) {
                    EditorInfo.IME_ACTION_DONE ->
                        loginViewModel.login(
                                username.text.toString(),
                                password.text.toString(),
                                miraiProtocol
                        )
                }
                false
            }

            login.setOnClickListener {
                loading.visibility = View.VISIBLE
                loginProcessPrint.visibility = View.VISIBLE
                login.isEnabled=false
                loginViewModel.login(username.text.toString(), password.text.toString(), miraiProtocol)
            }
        }
    }

    private fun updateUiWithUser() {
        val welcome = getString(R.string.welcome)
        // TODO : initiate successful logged in experience
        Toast.makeText(
                applicationContext,
                welcome,
                Toast.LENGTH_LONG
        ).show()
    }

    private fun showLoginFailed(@StringRes errorString: Int) {
        Toast.makeText(applicationContext, errorString, Toast.LENGTH_SHORT).show()
    }
}

/**
 * Extension java.util.function to simplify setting an afterTextChanged action to EditText components.
 */
fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    })
}