package cn.vove7.executorengine.helper

import android.content.Context
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.bridges.ChoiceData
import cn.vove7.common.bridges.GenChoiceData
import cn.vove7.common.datamanager.DAO
import cn.vove7.common.datamanager.executor.entity.MarkedContact
import cn.vove7.common.datamanager.greendao.MarkedContactDao
import cn.vove7.common.model.MatchedData
import cn.vove7.executorengine.model.Markable
import cn.vove7.vtp.contact.ContactHelper
import cn.vove7.vtp.contact.ContactInfo
import cn.vove7.vtp.log.Vog
import cn.vove7.vtp.text.TextHelper
import java.text.Collator
import java.util.*

/**
 * 联系人
 *
 * Created by Vove on 2018/6/19
 */
class ContactHelper(val context: Context) : GenChoiceData, Markable<MarkedContact> {
    //Dao
    private val markedContactDao = DAO.daoSession.markedContactDao

    override fun addMark(data: MarkedContact) {
        //数据库
        markedContactDao.insert(data)
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
        val markedData = markedContactDao.queryBuilder()//by key
                .where(MarkedContactDao.Properties.Key.eq(s)).unique()
        if (markedData != null) {
            Vog.d(this, "Matched from MarkedData by key $s")
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
            Vog.d(this, "match by regex")

            //正则匹配
            val list = markedContactDao.loadAll()
            list.forEach {
                val regex = it.regexStr.toRegex()
                if (regex.matches(s))
                    return it.phone
            }
            Vog.d(this, "from server no one")
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
                else -> TextHelper.compareSimilarityWithPinyin(GlobalApp.APP, s, it.contactName)
            }
            if (rate >= limitRate) {
                Vog.d(this, "${it.contactName}: $rate")
                matchList.add(MatchedData(rate, it))
            }
        }
        Vog.d(this, "模糊匹配联系人: ${matchList.size}")
        return matchList
    }

    private val CHINA_COMPARE = Collator.getInstance(java.util.Locale.CHINA)

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