package org.mirai.zhao.dice.file;

import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

public class JsonConfigOperator {
    final String dataPath;
    public JsonConfigOperator(String dataPath){
        this.dataPath=dataPath;
    }
    private void saveConfig(String id, JSONObject data){
        synchronized (dataPath) {
            String config_path = dataPath + "/" + id + ".json";
            File configFile = new File(config_path);
            TextFileOperator.write(configFile,data.toString());
        }
    }
    private JSONObject readConfig(String id){
        return readConfig(id,false);
    }
    private JSONObject readConfig(String id, boolean nullable){
        synchronized (dataPath) {
            try {
                String config_path = dataPath + "/" + id + ".json";
                return new JSONObject(TextFileOperator.read(new File(config_path)));
            } catch (Throwable ignored) {

            }
        }
        if(nullable)
            return null;
        return new JSONObject();
    }
    void saveBaseInfo(String id, String key, Object value){//保存信息
        JSONObject baseInfo=getBaseInfo(id);
        try {
            baseInfo.put(key,value);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        saveBaseInfo(id,baseInfo);
    }
    Object getBaseInfo(String id, String key){
        JSONObject baseInfo=getBaseInfo(id);
        return baseInfo.opt(key);
    }
    void saveBaseInfo(String id, JSONObject baseInfo) {//保存信息
        JSONObject config=readConfig(id);
        try {
            config.put("baseInfo",baseInfo);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        saveConfig(id,config);
    }
    JSONObject getBaseInfo(String id) {
        JSONObject config=readConfig(id);
        JSONObject baseInfo=config.optJSONObject("baseInfo");
        if(baseInfo==null)
            baseInfo=new JSONObject();
        return baseInfo;
    }
    void savePlayerName(String id, String playerName){
        //仅仅是修改当前的角色名称，不对其他数据变动
        saveBaseInfo(id,"name",playerName);
    }
    void deletePlayerInfo(String id, String playerName){
        JSONObject obj=readConfig(id);
        JSONObject muti_abilities=obj.optJSONObject("abilities");
        if(muti_abilities==null)
            muti_abilities=new JSONObject();
        else
            muti_abilities.remove(playerName);
        try {
            obj.put("abilities",muti_abilities);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        saveConfig(id,obj);
    }
    void saveMetaInfo(String ID, String type, String key, String value) {//保存元信息
        JSONObject obj=readConfig(type+"_"+ID,true);
        if(obj==null)
            obj=readConfig(type);
        try {
            obj.put(key,value);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        saveConfig(type+"_"+ID,obj);
    }
    public void saveGlobalInfo(String selfuin,String key,String value) {//保存全局配置信息
        saveMetaInfo(selfuin,"Global",key,value);
    }
    public void saveGlobalBoolean(String selfuin,String key,boolean value) {//保存全局配置信息
        saveMetaInfo(selfuin,"Global",key,value?"on":"off");
    }
    public boolean getGlobalBoolean(String selfuin,String key){//获取全局配置信息
        return getGlobalInfo(selfuin,key,"").equals("on");
    }
    public void saveGroupInfo(String groupuin,String key,String value) {//保存群配置信息
        saveMetaInfo(groupuin,"Group",key,value);
    }
    public void savePersonInfo(String QQ,String key,String value) {//保存消息者配置信息
        saveMetaInfo(QQ,"Person",key,value);
    }
    public String getGlobalInfo(String selfuin,String key){//获取全局配置信息
        return getGlobalInfo(selfuin,key,"");
    }

    public String getGlobalInfo(String selfuin, String key, String def){//获取全局配置信息
        return getMetaInfo(selfuin,"Global",key,def);
    }
    public String getGroupInfo(String groupuin, String key, String def){//获取群配置信息
        return getMetaInfo(groupuin,"Group",key,def);
    }
    public String getPersonInfo(String QQ, String key, String def){//获取消息者配置信息
        return getMetaInfo(QQ,"Person",key,def);
    }
    public String getMetaInfo(String ID,String type, String key, String def){
        JSONObject obj=readConfig(type+"_"+ID,true);
        if(obj==null)
            obj=readConfig(type,true);
        if(obj==null) {
            //默认配置信息
            obj=new JSONObject();
            if("Global".equals(type))
                saveConfig(type+"_"+ID,obj);
        }
        String str=obj.optString(key,def);
        if(TextUtils.isEmpty(str))
            str=def;
        return str;
    }
}
