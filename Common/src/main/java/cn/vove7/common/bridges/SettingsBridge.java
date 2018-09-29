package cn.vove7.common.bridges;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import cn.vove7.common.app.GlobalLog;
import cn.vove7.common.appbus.AppBus;
import cn.vove7.common.datamanager.DAO;
import cn.vove7.common.datamanager.DaoHelper;
import cn.vove7.common.datamanager.InstSettings;
import cn.vove7.common.model.InstSettingItem;
import cn.vove7.vtp.log.Vog;

/**
 * name -> settings json raw eversion
 * Created by Administrator on 9/26/2018
 */
public class SettingsBridge {
    private static final Type rawType = new TypeToken<TreeMap<String, InstSettingItem>>() {
    }.getType();
    private static final Type kvType = new TypeToken<TreeMap<String, Object>>() {
    }.getType();

    private InstSettings instSettings;
    private String name;
    private Map<String, Object> dataValues;

    /**
     * 已存在时
     *
     * @param name
     */
    public SettingsBridge(String name) {
        this.name = name;
        instSettings = DaoHelper.INSTANCE.getInsetSettingsByName(name);
        if (instSettings != null) {
            dataValues = getG().fromJson(instSettings.getDataJson(), kvType);
        }
    }

    public SettingsBridge(String name, String rawJson, int version) {
        this.name = name;
        instSettings = DaoHelper.INSTANCE.getInsetSettingsByName(name);
        if (instSettings == null) {//不存在
            createNew(rawJson);
            AppBus.INSTANCE.post(AppBus.EVENT_INST_SAVE_COMPLETE);
        } else if (instSettings.getVersion() < version) {//check version
            upgrade(rawJson, version);
            AppBus.INSTANCE.post(AppBus.EVENT_INST_SAVE_COMPLETE);
        } else {//填充数据
            dataValues = getG().fromJson(instSettings.getDataJson(), kvType);
        }
    }

    /**
     * 注册指令设置
     * 检查存在 -> 检查版本 更新?
     * 新建
     *
     * @param name
     * @param json
     */
    public static SettingsBridge registerSettings(String name, String json, int version) throws Exception {
        if (name == null) {
            throw new Exception("setting name must not null");
        }
        SettingsBridge bridge = new SettingsBridge(name, json, version);
        return bridge;
    }


    /**
     * 创建文件
     * 初始化
     * 遍历datas new key不存在，放入，old key 无删除
     */
    private void createNew(String rawJson) {
        instSettings = new InstSettings(name, rawJson);
        Map<String, InstSettingItem> settings = getG().fromJson(rawJson, rawType);
        String dataJson = getG().toJson(parseDefaultValues(settings));
        instSettings.setDataJson(dataJson);//
        instSettings.insertNew();
        GlobalLog.INSTANCE.log("new settings: " + name);
    }

    /**
     * 升级版本
     * old a b c
     * new a b d
     * --> a b d
     *
     * @param newVersion
     */
    private void upgrade(String newJson, int newVersion) {
        //oldparseDefaultValues(
        Map<String, Object> oldDatas = getG().fromJson(instSettings.getDataJson(), kvType);
        //new default data
        Map<String, InstSettingItem> newSettings = getG().fromJson(newJson, rawType);
        Map<String, Object> newDatas = parseDefaultValues(newSettings);
        Set<String> newKeySet = (newDatas.keySet());
        //compare
        //update
        for (Map.Entry<String, Object> o : newDatas.entrySet()) {//new -> old
            if (!oldDatas.containsKey(o.getKey())) {//不包含
                oldDatas.put(o.getKey(), o.getValue());
            }//包含不replace
        }
        List<String> rk = new ArrayList<>();//待remove
        for (String k : oldDatas.keySet()) {//去除旧数据
            if (!newKeySet.contains(k)) //新key不包含old
                rk.add(k);
        }
        for (String k : rk)
            oldDatas.remove(k);
        //newJson
        instSettings.setRawJson(newJson);

        //result :oldDatas
        //update
        dataValues = oldDatas;
        instSettings.setVersion(newVersion);
        updateDataValues();
    }

    private void updateDataValues() {
        instSettings.setDataJson(getG().toJson(dataValues));
        instSettings.update();
    }

    private static Map<String, Object> parseDefaultValues(Map<String, InstSettingItem> ss) {
        Map<String, Object> datas = new HashMap<>();
        for (Map.Entry<String, InstSettingItem> s : ss.entrySet()) {
            InstSettingItem item = s.getValue();
            if (InstSettingItem.TYPE_SINGLE_CHOICE.equals(s.getValue().getType())) {//单选
                item.setDefaultValue(item.getItems()[0]);
            }
            datas.put(s.getKey(), item.getDefaultValue());
        }
        return datas;
    }

    /**
     * @return 设置信息
     */
    public Map<String, InstSettingItem> getInstSettingItems() {
        try {
            String rawJson = DaoHelper.INSTANCE.getInsetSettingsByName(name).getRawJson();
            return new Gson().fromJson(rawJson, rawType);
        } catch (Exception e) {
            GlobalLog.INSTANCE.err(e);
            return null;
        }
    }
    /**
     * 检查一个存在name实例
     */
    public SettingsBridge getConfig(String name){
        InstSettings inst=DaoHelper.INSTANCE.getInsetSettingsByName(name);
        if(inst!=null){
            return new SettingsBridge(name);
        }
        return null;
    }

    /**
     * 重置设置
     */
    public void resetSettings() {
        //获取raw 转 存
        String rawJson = instSettings.getRawJson();
        Map<String, InstSettingItem> settings = getG().fromJson(rawJson, rawType);
        String dataJson = getG().toJson(parseDefaultValues(settings));
        instSettings.setDataJson(dataJson);//
        instSettings.update();
    }

    /**
     * 重置设置
     */
    //public void save2Db(InstSettings instSettings) {
    //    if (instSettings.getRawJson() == null) {
    //        Vog.INSTANCE.d(this, "set ---> create");
    //
    //        //spHelper.set(name + "_raw_settings", settingItemsJson);
    //        //spHelper.set(name + "_version", version);
    //    } else {
    //        Vog.INSTANCE.d(this, "set ---> set");
    //        //settingItemsJson = spHelper.getString(name + "_raw_settings");
    //    }
    //    //save settingItemsJson
    //
    //    //Map<String, Object> settingValues;// 存储信息
    //    if (settings != null) {
    //        datas = parseDefaultValues(settings);
    //        saveDatas2Db();
    //        AppBus.INSTANCE.post(AppBus.EVENT_INST_SAVE_COMPLETE);
    //        Vog.INSTANCE.d("", "set ---> " + name + "\n" + datas);
    //    } else {
    //        GlobalLog.INSTANCE.err("code: ss99");
    //    }
    //}
    private void initIfNeed() {
        if (dataValues == null) {
            dataValues = getG().fromJson(instSettings.getDataJson(), kvType);
        }
    }

    public void remove() {
        DAO.INSTANCE.getDaoSession().getInstSettingsDao().delete(instSettings);
    }

    public Integer getVersion() {
        return instSettings.getVersion();
    }

    public Integer getInt(String key) {
        initIfNeed();
        Number n = ((Number) dataValues.get(key));
        if (n != null)
            return n.intValue();
        return null;
    }

    public String getString(String key) {
        initIfNeed();
        return (String) dataValues.get(key);
    }

    //public List<String> getArray(String key) {
    //    initIfNeed();
    //    return (List<String>) datas.get(key);
    //}

    public Boolean getBoolean(String key) {
        initIfNeed();
        return (Boolean) dataValues.get(key);
    }

    public void set(String key, Object value) {//类型一致性？
        Vog.INSTANCE.d(this, "set ---> " + key + " -- " + value);
        if (value instanceof Number) {
            value = ((Number) value).intValue();
        }
        initIfNeed();
        dataValues.put(key, value);
        updateDataValues();
    }

    public static Gson getG() {
        return new GsonBuilder().registerTypeAdapter(Double.class, new JsonSerializer<Double>() {
            @Override
            public JsonElement serialize(Double src, Type typeOfSrc, JsonSerializationContext context) {
                if (src == src.longValue()) return new JsonPrimitive(src.longValue());
                return new JsonPrimitive(src);
            }
        }).create();
    }
}
