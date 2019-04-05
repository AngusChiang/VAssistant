--
-- @author Vove
-- 2018/8/6 15:48
-- WaitViewSample.lua
--
requireAccessibility()
smartOpen("com.eg.android.AlipayGphone")
--waitForApp('com.eg.android.AlipayGphone')
ViewFinder().equalsText({'首页','Home'}).id('tab_description').tryClick()
sacn = ViewFinder().id('saoyisao_tv')
sacn.waitFor().tryClick()
