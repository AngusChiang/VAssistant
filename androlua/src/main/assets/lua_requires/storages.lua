--
-- @author Vove
-- 2018/8/16 22:13
-- storages
--
--[[
--SharedPreference
 ]]

import 'cn.vove7.vtp.sharedpreference.SpHelper'
local SP = luajava.bindClass('cn.vove7.vtp.sharedpreference.SpHelper')
function SpHelper(name)
    log('sp: ', name)
    return SP(app, name)
end