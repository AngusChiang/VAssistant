package cn.vove7.executorengine.utils.contact

import android.content.Context
import cn.vove7.parseengine.model.MatchedData
import cn.vove7.vtp.contact.ContactHelper
import cn.vove7.vtp.contact.ContactInfo
import cn.vove7.vtp.log.Vog
import cn.vove7.vtp.text.TextHelper

/**
 * 联系人
 *
 * Created by Vove on 2018/6/19
 */
object ContactHelper {
    /**
     * 提供的服务电话表
     */
    var serveContact = hashMapOf(
            Pair("中国移动", Pair("(中国)?移动", "10086")),
            Pair("中国联通", Pair("(中国)?联通", "10010")),
            Pair("中国电信", Pair("(中国)?电信", "10000"))
    )

    /**
     * 更新 from server
     */
    fun updateServerContact() {

    }

    private val allNum = "1[0-9]*".toRegex()
    /**
     * 存储本地联系人
     */
    private val LOCAL_CONTACT_LIST = HashMap<String, ContactInfo>()

    var lastUpdateTime = 0L
    private const val updateInterval = 30 * 60 * 1000
    private const val limitRate = 0.8f

    /**
     * 模糊匹配 -> 匹配提供
     */

    fun matchPhone(context: Context, s: String, update: Boolean = true): String? {
        if (allNum.matches(s)) {//数字
            return s
        }
        //本地匹配
        val c = LOCAL_CONTACT_LIST[s]
        if (c != null) {
            return c.phones[0]
        }
        val now = System.currentTimeMillis()
        //更新
        if (now - lastUpdateTime > updateInterval && update) {
            lastUpdateTime = now
            LOCAL_CONTACT_LIST.clear()
            LOCAL_CONTACT_LIST.putAll(ContactHelper.getAllContacts(context))
            return matchPhone(context, s, false)
        }
        //模糊匹配 -> 匹配提供
        val matchedList = fuzzyMatching(s)
        return if (matchedList.isNotEmpty()) {
            matchedList[0].data.phones[0]
        } else {//匹配提供
            Vog.d(this,"匹配提供")
            //哈希匹配
            val p = serveContact[s]
            if (p != null)
                return p.second

            //正则匹配
            serveContact.forEach {
                val regex = it.value.first.toRegex()
                if (regex.matches(s)) {
                    return it.value.second
                }
            }
            null
        }
    }

    /**
     * 模糊匹配
     */
    private fun fuzzyMatching(s: String): List<MatchedData<ContactInfo>> {
        val list = LOCAL_CONTACT_LIST.values
        val matchList = mutableListOf<MatchedData<ContactInfo>>()
        list.forEach {
            val rate = when (s) {
                it.contactName -> 1f
            //转拼音比较
                else -> TextHelper.compareSimilarityWithPinyin(s, it.contactName)
            }
            if (rate >= limitRate) {
                Vog.d(this, "${it.contactName}: $rate")
                matchList.add(MatchedData(rate, it))
            }
        }
        Vog.d(this, "模糊匹配联系人: ${matchList.size}")
        return matchList
    }
}