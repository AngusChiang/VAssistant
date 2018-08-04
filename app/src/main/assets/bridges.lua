--
-- @author Vove
-- Lua->Android调用桥
-- 2018/8/4 10:31
-- bridges/api.lua
--

require 'import'
import 'cn.vove7.androlua.androbridge.*'

--local bridges = luajava.bindClass('cn.vove7.androlua.androbridge.BridgeManager')
local accessibilityApi=bridges.accessibilityApi
local automator=bridges.automator
local serviceBridge=bridges.serviceBridge
local systemBridge=bridges.systemBridge

function toast()

end