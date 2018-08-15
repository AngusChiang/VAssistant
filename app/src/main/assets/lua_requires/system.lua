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





