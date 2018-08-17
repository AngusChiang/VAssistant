--
-- @author 17719247306
-- 2018/8/16 0:50
-- system.lua
--

--*
--通过包名打开App
-- ExResult
--
function openAppByPkg(pkg)
    return system.openAppByPkg(pkg)
end

--*
--通过通过关键字匹配
-- return ExResult pkgName if ok
--
function openAppByWord(appWord)
    return system.openAppByWord(appWord)
end

--*
--手电
--ExResult
function openFlashlight()
    return system.openFlashlight()
end

--[[
--获取App信息
--s : 包名或App名
-- App名可能随系统语言变化  不建议使用App名查找
 ]]
function getAppInfo(s)
    return system.getAppInfo(s)
end



