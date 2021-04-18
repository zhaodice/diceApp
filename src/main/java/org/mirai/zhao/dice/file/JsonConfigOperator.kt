package org.mirai.zhao.dice.file

import android.text.TextUtils
import org.json.JSONException
import org.json.JSONObject
import org.mirai.zhao.dice.file.TextFileOperator.read
import org.mirai.zhao.dice.file.TextFileOperator.write
import java.io.File

class JsonConfigOperator(val dataPath: String) {
    private fun saveConfig(id: String, data: JSONObject?) {
        synchronized(dataPath) {
            val config_path = "$dataPath/$id.json"
            val configFile = File(config_path)
            write(configFile, data.toString())
        }
    }

    private fun readConfig(id: String, nullable: Boolean = false): JSONObject? {
        synchronized(dataPath) {
            try {
                val config_path = "$dataPath/$id.json"
                return JSONObject(read(File(config_path)))
            } catch (ignored: Throwable) {
            }
        }
        return if (nullable) null else JSONObject()
    }

    fun saveBaseInfo(id: String, key: String, value: Any?) { //保存信息
        val baseInfo = getBaseInfo(id)
        try {
            baseInfo.put(key, value)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        saveBaseInfo(id, baseInfo)
    }

    fun getBaseInfo(id: String, key: String): Any? {
        val baseInfo = getBaseInfo(id)
        return baseInfo.opt(key)
    }

    fun saveBaseInfo(id: String, baseInfo: JSONObject?) { //保存信息
        val config = readConfig(id)
        try {
            config!!.put("baseInfo", baseInfo)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        saveConfig(id, config)
    }

    fun getBaseInfo(id: String): JSONObject {
        val config = readConfig(id)
        var baseInfo = config!!.optJSONObject("baseInfo")
        if (baseInfo == null) baseInfo = JSONObject()
        return baseInfo
    }

    fun savePlayerName(id: String, playerName: String?) {
        //仅仅是修改当前的角色名称，不对其他数据变动
        saveBaseInfo(id, "name", playerName)
    }

    fun deletePlayerInfo(id: String, playerName: String?) {
        val obj = readConfig(id)
        var muti_abilities = obj!!.optJSONObject("abilities")
        if (muti_abilities == null) muti_abilities = JSONObject() else muti_abilities.remove(playerName)
        try {
            obj.put("abilities", muti_abilities)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        saveConfig(id, obj)
    }

    fun saveMetaInfo(ID: String, type: String, key: String, value: String?) { //保存元信息
        var obj = readConfig(type + "_" + ID, true)
        if (obj == null) obj = readConfig(type)
        try {
            obj!!.put(key, value)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        saveConfig(type + "_" + ID, obj)
    }

    fun saveGlobalInfo(selfuin: String, key: String, value: String?) { //保存全局配置信息
        saveMetaInfo(selfuin, "Global", key, value)
    }

    fun saveGlobalBoolean(selfuin: String, key: String, value: Boolean) { //保存全局配置信息
        saveMetaInfo(selfuin, "Global", key, if (value) "on" else "off")
    }

    fun getGlobalBoolean(selfuin: String, key: String?): Boolean { //获取全局配置信息
        return getGlobalInfo(selfuin, key, "") == "on"
    }

    fun saveGroupInfo(groupuin: String, key: String, value: String?) { //保存群配置信息
        saveMetaInfo(groupuin, "Group", key, value)
    }

    fun savePersonInfo(QQ: String, key: String, value: String?) { //保存消息者配置信息
        saveMetaInfo(QQ, "Person", key, value)
    }

    fun getGlobalInfo(selfuin: String, key: String?): String { //获取全局配置信息
        return getGlobalInfo(selfuin, key, "")
    }

    fun getGlobalInfo(selfuin: String, key: String?, def: String): String { //获取全局配置信息
        return getMetaInfo(selfuin, "Global", key, def)
    }

    fun getGroupInfo(groupuin: String, key: String?, def: String): String { //获取群配置信息
        return getMetaInfo(groupuin, "Group", key, def)
    }

    fun getPersonInfo(QQ: String, key: String?, def: String): String { //获取消息者配置信息
        return getMetaInfo(QQ, "Person", key, def)
    }

    fun getMetaInfo(ID: String, type: String, key: String?, def: String): String {
        var obj = readConfig(type + "_" + ID, true)
        if (obj == null) obj = readConfig(type, true)
        if (obj == null) {
            //默认配置信息
            obj = JSONObject()
            if ("Global" == type) saveConfig(type + "_" + ID, obj)
        }
        var str = obj.optString(key, def)
        if (TextUtils.isEmpty(str)) str = def
        return str
    }
}