package cn.vove7.datamanager

import android.support.annotation.CallSuper
import android.util.Pair
import cn.vove7.datamanager.executor.entity.MarkedOpen
import cn.vove7.datamanager.executor.entity.ServerContact
import cn.vove7.vtp.log.Vog

/**
 * # InitDbData
 *
 * @author 17719
 * 2018/8/8
 */
abstract class InitDbData {
    @CallSuper
    open fun init(){
        initCommonData()
    }

    private fun initCommonData() {

        val serverContactDao = DAO.daoSession.serverContactDao
        if (serverContactDao.queryBuilder().count() == 0L) {
            arrayOf(
                    Pair("中国移动", Pair("(中国)?移动(客服)?", "10086")),
                    Pair("中国联通", Pair("(中国)?联通(客服)?", "10010")),
                    Pair("中国电信", Pair("(中国)?电信(客服)?", "10000"))
            ).forEach {
                val data = ServerContact()
                data.key = it.first
                data.regexStr = it.second.first
                data.value = it.second.second
                serverContactDao.insert(data)
            }
        } else Vog.d(this, "serverContact 存在数据")

        val markedOpenDAO = DAO.daoSession.markedOpenDao
        if (markedOpenDAO.queryBuilder().count() == 0L) {
            arrayOf(
                    MarkedOpen("手电", MarkedOpen.MARKED_TYPE_SYS_FUN, "((手电(筒)?)|(闪光灯)|(照明(灯)))", "openFlash")
                    , MarkedOpen("网易云", MarkedOpen.MARKED_TYPE_APP, "网易云(音乐)?", "com.netease.cloudmusic")
                    , MarkedOpen("支付宝", MarkedOpen.MARKED_TYPE_APP, "(Alipay)|(支付宝)", "com.eg.android.AlipayGphone")
            ).forEach {
                markedOpenDAO.insert(it)
            }
        }
    }

}