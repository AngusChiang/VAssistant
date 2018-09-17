package cn.vove7.common.datamanager

import android.util.Pair
import cn.vove7.common.datamanager.executor.entity.MarkedData
import cn.vove7.common.datamanager.parse.DataFrom

/**
 * # InitDbData
 *
 * @author 17719
 * 2018/8/8
 */
abstract class InitDbData {
    fun init() {
//        initCommonData()
//        initSelf()
    }

    open fun initSelf() {}

    private fun initCommonData() {
        val appAdInfoDao = DAO.daoSession.appAdInfoDao
//        if (appAdInfoDao.queryBuilder().count() <= 5L) {
        appAdInfoDao.deleteAll()
        arrayOf(//TODO test depths
                AppAdInfo("网易云首屏广告", "com.netease.cloudmusic", "LoadingActivity", "Skip###跳过")
                , AppAdInfo("网易云Resume广告", "com.netease.cloudmusic", "LoadingAdActivity", "Skip###跳过")
                , AppAdInfo("租八戒首屏广告", "com.rentpig.customer", "WelcomeActivity").setDepths("0,0,0,0,0,1,1,0,2,0,1").setType("ImageView")
                , AppAdInfo("Turbo VPN 弹框广告1", "free.vpn.unblock.proxy.turbovpn", "VpnMainActivity").setDescs("Interstitial close button")
                , AppAdInfo("Turbo VPN 弹框广告2", "free.vpn.unblock.proxy.turbovpn", "VpnMainActivity").setViewId("cancelImageView")
                , AppAdInfo("Turbo VPN 弹框广告3", "free.vpn.unblock.proxy.turbovpn", "VpnMainActivity").setViewId("close_button_image")
//                    , AppAdInfo("Turbo VPN 弹框广告3", "free.vpn.unblock.proxy.turbovpn", "FullNativeAdActivity").setViewId("cancelImageView")

        ).forEach {
            appAdInfoDao.insert(it)
        }
//        }

        val markedDataDao = DAO.daoSession.markedDataDao
        markedDataDao.deleteAll()
        arrayOf(
                Pair("中国移动", Pair("(中国)?移动(客服)?", "10086")),
                Pair("中国联通", Pair("(中国)?联通(客服)?", "10010")),
                Pair("中国电信", Pair("(中国)?电信(客服)?", "10000"))
        ).forEach {
            val data = MarkedData()
            data.key = it.first
            data.regStr = it.second.first
            data.value = it.second.second
            data.type = MarkedData.MARKED_TYPE_CONTACT
            data.from = DataFrom.FROM_SERVICE
            markedDataDao.insert(data)
        }

        arrayOf(
                MarkedData("手电", MarkedData.MARKED_TYPE_SCRIPT_LUA, "((手电(筒)?)|(闪光灯)|(照明(灯)))", "system.openFlashlight()")
                , MarkedData("网易云", MarkedData.MARKED_TYPE_APP, "网易云(音乐)?", "com.netease.cloudmusic")
                , MarkedData("支付宝", MarkedData.MARKED_TYPE_APP, "(Alipay)|(支付宝)", "com.eg.android.AlipayGphone")
                , MarkedData("蓝牙", MarkedData.MARKED_TYPE_SCRIPT_LUA, "蓝牙", "system.openBluetooth()")
                , MarkedData("wifi", MarkedData.MARKED_TYPE_SCRIPT_LUA, "(Alipay)|(支付宝)", "system.openWlan()")
        ).forEach {
            markedDataDao.insert(it)
        }
    }
}
