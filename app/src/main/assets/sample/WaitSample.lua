--
-- @author 17719
-- 2018/8/6 15:48
-- WaitSample.lua
--
require 'checkservice'

executor.openSomething("支付宝")
print("open")
executor.waitForViewId('saoyisao_tv')
print('find saoyisao_tv')