package cn.vove7.common.datamanager

import android.support.annotation.CallSuper
import android.util.Pair
import cn.vove7.common.datamanager.executor.entity.MarkedContact
import cn.vove7.common.datamanager.executor.entity.MarkedOpen
import cn.vove7.common.datamanager.parse.DataFrom
import cn.vove7.vtp.log.Vog

/**
 * # InitDbData
 *
 * @author 17719
 * 2018/8/8
 */
abstract class InitDbData {
    @CallSuper
    open fun init() {
        initCommonData()
    }

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

        val markedContactDao = DAO.daoSession.markedContactDao
        if (markedContactDao.queryBuilder().count() == 0L) {
            arrayOf(
                    Pair("中国移动", Pair("(中国)?移动(客服)?", "10086")),
                    Pair("中国联通", Pair("(中国)?联通(客服)?", "10010")),
                    Pair("中国电信", Pair("(中国)?电信(客服)?", "10000"))
            ).forEach {
                val data = MarkedContact()
                data.key = it.first
                data.contactName = it.first
                data.regexStr = it.second.first
                data.phone = it.second.second
                data.from = DataFrom.FROM_SERVICE
                markedContactDao.insert(data)
            }
        } else Vog.d(this, "serverContact 存在数据")

        val markedOpenDAO = DAO.daoSession.markedOpenDao
        if (markedOpenDAO.queryBuilder().count() == 0L) {
            arrayOf(
                    MarkedOpen("手电", MarkedOpen.MARKED_TYPE_SCRIPT_JS, "((手电(筒)?)|(闪光灯)|(照明(灯)))", "system.openFlashlight()")
                    , MarkedOpen("网易云", MarkedOpen.MARKED_TYPE_APP, "网易云(音乐)?", "com.netease.cloudmusic")
                    , MarkedOpen("支付宝", MarkedOpen.MARKED_TYPE_APP, "(Alipay)|(支付宝)", "com.eg.android.AlipayGphone")
                    , MarkedOpen("蓝牙", MarkedOpen.MARKED_TYPE_SCRIPT_JS, "蓝牙", "system.openBluetooth()")
                    , MarkedOpen("wifi", MarkedOpen.MARKED_TYPE_SCRIPT_JS, "(Alipay)|(支付宝)", "system.openWlan()")
            ).forEach {
                markedOpenDAO.insert(it)
            }
        }
    }

}