package org.mirai.zhao.dice.activity.ui.login

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.Observer
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.lifecycle.ViewModelProvider
import net.mamoe.mirai.utils.BotConfiguration
import org.mirai.zhao.dice.R
import org.mirai.zhao.dice.activity.AccountsActivity
import org.mirai.zhao.dice.console.ConsoleService

class LoginActivity : AppCompatActivity() {

    private lateinit var loginViewModel: LoginViewModel
    private var miraiProtocol:BotConfiguration.MiraiProtocol=BotConfiguration.MiraiProtocol.ANDROID_WATCH
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)

        ConsoleService.startControlService(this)
        val username = findViewById<EditText>(R.id.username)
        val password = findViewById<EditText>(R.id.password)
        val login = findViewById<Button>(R.id.login)
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
        loginViewModel = ViewModelProvider(this, LoginViewModelFactory(this)).get(LoginViewModel::class.java)

        loginViewModel.loginFormState.observe(this@LoginActivity, Observer {
            val loginState = it ?: return@Observer
            loading.visibility = View.GONE
            // disable login button unless both username / password is valid
            login.isEnabled = loginState.isDataValid

            if (loginState.usernameError != null) {
                username.error = getString(loginState.usernameError)
            }
            if (loginState.passwordError != null) {
                if (loginState.serverResult != null) {
                    password.error = "服务器返回("+loginState.serverResult+")"+"\n"+getString(loginState.passwordError)
                }else{
                    password.error = getString(loginState.passwordError)
                }
            }
            login.isEnabled=true
        })

        loginViewModel.loginResult.observe(this@LoginActivity, Observer {
            val loginResult = it ?: return@Observer

            loading.visibility = View.GONE
            login.isEnabled=true
            if (loginResult.error != null) {
                showLoginFailed(loginResult.error)
            }else{
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
                login.isEnabled=false
                loginViewModel.login(username.text.toString(), password.text.toString(),miraiProtocol)
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
 * Extension function to simplify setting an afterTextChanged action to EditText components.
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