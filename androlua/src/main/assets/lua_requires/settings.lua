--
-- @author Administrator
-- 9/26/2018 12:23 AM
-- settings.lua
--
-- require 'import'
import 'cn.vove7.common.bridges.SettingsBridge'
-- json = require('json')

function registerSettings(name, table, version)
    return SettingsBridge.registerSettings(name, json.encode(table), version)
end

