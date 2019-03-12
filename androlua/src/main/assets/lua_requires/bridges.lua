--
-- @author Vove
-- Lua->Android调用桥
-- 2018/8/4 10:31
-- bridges/api.lua
--

require 'import'
import 'cn.vove7.androlua.androbridge.*'
import "cn.vove7.common.appbus.AppBus"
import "cn.vove7.common.appbus.SpeechAction"
--
bridges = luaman.getBridgeManager()
executor = bridges.getExecutor()
http = bridges.getHttpBridge()
androRuntime = bridges.getRootHelper()
serviceBridge = bridges.getServiceBridge()
system = bridges.getSystemBridge()
automator = bridges.getAutomator()

function notifyFailed(s)
    executor.executeFailed(s)
end

function log(msg)
    luaman.log(msg)
end

require 'utils'
require 'executor'
require 'view_op'
require 'system'
require 'global_op'
require 'storages'
require 'settings'

