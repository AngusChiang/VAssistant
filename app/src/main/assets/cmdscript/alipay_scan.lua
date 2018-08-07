--
-- @author 17719
-- 2018/8/7 1:07
-- alipay_scan.lua
--
ViewFinder().equalsText('首页').id('tab_description').tryClick()
ViewFinder().equalsText('Home').id('tab_description').tryClick()
sacn = ViewFinder().id('saoyisao_tv')

sacn.waitFor().tryClick()