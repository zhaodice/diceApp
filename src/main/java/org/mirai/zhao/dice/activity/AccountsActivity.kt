package org.mirai.zhao.dice.activity

import android.app.AlertDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import org.mirai.zhao.dice.AppContext
import org.mirai.zhao.dice.R
import org.mirai.zhao.dice.file.FileService
import org.mirai.zhao.dice.file.TextFileOperator
import java.io.File
import java.lang.StringBuilder


class AccountsActivity : AppCompatActivity() {
    companion object {
        private val autoLoginFile: File by lazy { File(AppContext.autoLoginFile) }
        private val accountListData : ArrayList<String> by lazy { getAccountsList() }
        fun getAccountsList(): ArrayList<String> {
            val arraylist=ArrayList<String>()
            val accounts_text= TextFileOperator.read(autoLoginFile)
            val accounts = accounts_text.split("\n")
            for (element in accounts) {
                val accountInfo = element.split(" ")
                if (accountInfo.size >= 2) {
                    arraylist.add(accountInfo[0])
                }
            }
            return arraylist
        }
        fun deleteAccount(qq:String){
            val arraylist=StringBuilder()
            val accounts_text= TextFileOperator.read(autoLoginFile)

            val accounts = accounts_text.split("\n")
            for (i in 0 until accounts.size) {
                val account = accounts[i]
                val accountInfo = account.split(" ")
                if (accountInfo.size >= 2&&!accountInfo[0].equals(qq)) {
                    arraylist.append(account)
                    arraylist.append("\n")
                }
            }
            TextFileOperator.write(autoLoginFile,arraylist.toString())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_accounts)
        val accountList=findViewById<ListView>(R.id.accountList)
        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, accountListData) //新建并配置ArrayAapeter
        accountList.setAdapter(adapter)
        accountList.setOnItemClickListener{ _, _, position, _ ->
            val qq=accountListData.get(position)
            AlertDialog.Builder(this).setTitle("确认删除？").setMessage(String.format("确认删除登陆的骰娘账号【%s】吗？删除后重启本程序后将不再是自动登陆状态",qq))
                    .setIcon(android.R.drawable.ic_dialog_info)
                    .setPositiveButton("确定") { _, _ -> // 点击“确认”后的操作
                        deleteAccount(qq)
                        accountListData.remove(qq)
                        adapter.notifyDataSetChanged()
                    }
                    .setNegativeButton("不") { _, _ ->
                        // 点击“返回”后的操作,这里不设置没有任何操作
                    }.show()
        }
    }
}