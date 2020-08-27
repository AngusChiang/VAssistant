



smartOpen("支付宝")
smartOpen("Alipay")
waitForApp('com.eg.android.AlipayGphone')

equalsText('首页','Home').id('tab_description').tryClick()
var sacn = id('saoyisao_tv')

sacn.waitFor().tryClick()
