--
-- @author Vove
-- 2018/8/17 17:46
-- AppInfoSample.lua
--


appQQ = system.getAppInfo('com.tencent.mobileqq')
if (appQQ) then
    print(appQQ)
    print(appQQ.name) -- getName()
else
    print('没安装QQ')
end

