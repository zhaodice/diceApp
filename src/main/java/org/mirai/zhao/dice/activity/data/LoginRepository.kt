package org.mirai.zhao.dice.activity.data

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.utils.BotConfiguration
//import net.mamoe.mirai.console.MiraiConsole
import org.mirai.zhao.dice.AppContext
import org.mirai.zhao.dice.activity.AccountsActivity
import org.mirai.zhao.dice.console.ConsoleService
import org.mirai.zhao.dice.file.FileService
import org.mirai.zhao.dice.file.TextFileOperator
import java.io.File
import java.io.IOException
import java.lang.StringBuilder

/**
 * Class that requests authentication and user information from the remote data source and
 * maintains an in-memory cache of login status and user credentials information.
 */

class LoginRepository() {
    //val miraiDeviceFile:File= File(ConsoleService.miraiDir+"/device.json")
    val autoLoginFile:File=File(AppContext.autoLoginFile)
    /*
   * 账号是否已经有自动登陆了
   * */
    fun thisAccountHasAutoLogin(qq:String) : Boolean{
        val accounts_text= TextFileOperator.read(autoLoginFile)
        val accounts = accounts_text.split("\n")
        for (element in accounts) {
            val account = element
            val accountInfo = account.split(" ")
            if (accountInfo.size >= 2) {
                val file_qq = accountInfo[0]
                //val password = accountInfo[1]
                if(qq.equals(file_qq)){
                    return true;
                }
            }
        }
        return false;
    }
    /*
   * 设置自动登陆
   * */
    private fun settingAutoLogin(qq:String,password:String){
        if(thisAccountHasAutoLogin(qq))
            AccountsActivity.deleteAccount(qq)//先删账号
        val accountsText = StringBuilder(TextFileOperator.read(autoLoginFile))
        if(!accountsText.endsWith('\n'))
            accountsText.append('\n')
        accountsText.append(String.format("%s %s\n", qq, password))
        TextFileOperator.write(autoLoginFile, accountsText.toString())
    }
    fun login(qq: String, password: String,setProtocol: BotConfiguration.MiraiProtocol): String {
        // handle login
        val result= CompletableDeferred<String>()
        val bot=ConsoleService.autoLogin(qq,password,setProtocol,result)
        return runBlocking {
            val s=result.await()
            if(s.isEmpty()){
                //等待机器人启动
                while(!bot.isOnline){Thread.sleep(1000)}
                settingAutoLogin(qq,password)
                return@runBlocking s;
            }else{
                return@runBlocking s;
            }
        }
    }
}