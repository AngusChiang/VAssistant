

var a = ViewFinder().id('js_args').findFirst()
for (var i=1;i<3;i++){
    a.setText(i + 's')
    sleep(1000)
}

smartOpen("支付宝")
smartOpen("Alipay")
//waitForApp('com.eg.android.AlipayGphone')

ViewFinder().equalsText('首页','Home').id('tab_description').tryClick()
var sacn = ViewFinder().id('saoyisao_tv')

sacn.waitFor().tryClick()
