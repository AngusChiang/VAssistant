
importClass(Packages.cn.vove7.common.bridges.SettingsBridge)

function registerSettings(name, map,v){
    return SettingsBridge.registerSettings(name,JSON.stringify(map),v)
}