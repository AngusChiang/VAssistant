package cn.vove7.common.netacc.model

/**
 * Created by Administrator on 2018/10/6
 */
data class LastDateInfo(
        var instGlobal: Long = 0,
        var instInApp: Long = 0,
        var markedContact: Long = 0,
        var markedApp: Long = 0,
        var markedOpen: Long = 0,
        var markedAd: Long
)