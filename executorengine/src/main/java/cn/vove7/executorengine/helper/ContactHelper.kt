package cn.vove7.executorengine.helper

import android.content.Context
import cn.vove7.datamanager.DAO
import cn.vove7.datamanager.executor.entity.MarkedContact
import cn.vove7.datamanager.greendao.MarkedContactDao
import cn.vove7.datamanager.greendao.ServerContactDao
import cn.vove7.common.bridges.ChoiceData
import cn.vove7.common.bridges.GenChoiceData
import cn.vove7.common.model.MatchedData
import cn.vove7.vtp.contact.ContactHelper
import cn.vove7.vtp.contact.ContactInfo
import cn.vove7.vtp.log.Vog
import cn.vove7.vtp.text.TextHelper

/**
 * 联系人
 *
 * Created by Vove on 2018/6/19
 */
class ContactHelper(val context: Context) : GenChoiceData {
    //Dao
    private val markedContactDao = DAO.daoSession.markedContactDao
    private val serverContactDao = DAO.daoSession.serverContactDao

    /**
     * 服务提供的电话表
     */
//    var serverContact = hashMapOf(
//            Pair("中国移动", Pair("(中国)?移动", "10086")),
//            Pair("中国联通", Pair("(中国)?联通", "10010")),
//            Pair("中国电信", Pair("(中国)?电信", "10000"))
//    )

    fun addMark(marked: MarkedContact) {
        //数据库
        markedContactDao.insert(marked)
    }

    private val allNum = "1[0-9]*".toRegex()

    private var lastUpdateTime = 0L

    companion object {
        private const val updateInterval = 30 * 60 * 1000
        private const val limitRate = 0.8f
        /**
         * 存储本地联系人
         */
        private val LOCAL_CONTACT_LIST = HashMap<String, ContactInfo>()
    }

    fun updateContactList() {
        val now = System.currentTimeMillis()
        //更新
        if (now - lastUpdateTime > updateInterval) {
            lastUpdateTime = now
            LOCAL_CONTACT_LIST.clear()
            LOCAL_CONTACT_LIST.putAll(ContactHelper.getAllContacts(context))
        }
    }

    /**
     * 纯数字 -> 标记 -> 模糊匹配 -> 匹配提供
     */
    fun matchPhone(context: Context, s: String, update: Boolean = true): String? {
        if (allNum.matches(s)) {//数字
            return s
        }
        val markedData = markedContactDao.queryBuilder().where(MarkedContactDao.Properties.Key.eq(s)).unique()
        if (markedData != null) {
            Vog.d(this, "Matched from MarkedData")
            return markedData.phone
        }
        //本地匹配
        val localMatched = LOCAL_CONTACT_LIST[s]
        if (localMatched != null) {
            return localMatched.phones[0]
        }
        //更新列表
        if (update) {
            updateContactList()
            return matchPhone(context, s, false)
        }
        //模糊匹配 -> 匹配提供
        val matchedList = fuzzyMatching(s)
        return if (matchedList.isNotEmpty()) {
            matchedList[0].data.phones[0]
        } else {//匹配提供
            Vog.d(this, "匹配 from server")
            //通过key查找
            serverContactDao.queryBuilder().where(ServerContactDao.Properties.Key.eq(s)).unique()?.value

            //正则匹配
            val list = serverContactDao.loadAll()
            list.forEach {
                val regex = it.regexStr.toRegex()
                if (regex.matches(s))
                    return it.value
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

    /**
     * 获取标识列表
     */
    override fun getChoiceData(): List<ChoiceData> {
        val choiceList = mutableListOf<ChoiceData>()
        if (LOCAL_CONTACT_LIST.isEmpty()) {
            updateContactList()
        }
        LOCAL_CONTACT_LIST.values.sorted().forEach {
            it.phones.forEach { p ->
                choiceList.add(ChoiceData(it.contactName, null, p, originalData = it))
            }
        }
        choiceList.sort()
        return choiceList
    }
}