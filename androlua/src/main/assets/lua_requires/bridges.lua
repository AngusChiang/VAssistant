--
-- @author Vove
-- Lua->Android调用桥
-- 2018/8/4 10:31
-- bridges/api.lua
--

-- require 'import'
import "cn.vove7.common.appbus.AppBus"
--
bridges = luaman.getBridgeManager()
executor = bridges.get('executor')
http = bridges.get('http')
androRuntime = bridges.get('androRuntime')
shell = bridges.get('shell')
serviceBridge = bridges.get('serviceBridge')
system = bridges.get('system')
automator = bridges.get('automator')
input = bridges.get('input')
dialog = bridges.get('dialog')

function notifyFailed(s)
    executor.executeFailed(s)
end


function log(msg)
    luaman.log(msg)
end

-- require 'utils'
-- require 'executor'
-- require 'view_op'
-- require 'global_op'
-- require 'storages'
-- require 'settings'

