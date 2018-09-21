package cn.vove7.executorengine.helper

import android.content.Context
import android.content.pm.PackageManager
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.bridges.ChoiceData
import cn.vove7.common.bridges.GenChoiceData
import cn.vove7.common.datamanager.DAO
import cn.vove7.common.datamanager.executor.entity.MarkedData
import cn.vove7.common.datamanager.greendao.MarkedDataDao
import cn.vove7.common.model.MatchedData
import cn.vove7.executorengine.model.Markable
import cn.vove7.vtp.contact.ContactHelper
import cn.vove7.vtp.contact.ContactInfo
import cn.vove7.vtp.log.Vog
import cn.vove7.vtp.text.TextHelper
import java.util.*

/**
 * 联系人
 *
 * Created by Vove on 2018/6/19
 */
object AdvanContactHelper : GenChoiceData, Markable {
    val context = GlobalApp.APP
    //Dao
    private val markedContactDao = DAO.daoSession.markedDataDao

    override fun addMark(data: MarkedData) {
        //数据库
        markedContactDao.insert(data)
    }

    private val phoneNum = "[\\s\\-0-9]*".toRegex()

    private var lastUpdateTime = 0L

    private const val updateInterval = 30 * 60 * 1000
    private const val limitRate = 0.8f
    /**
     * 存储本地联系人
     */
    private val LOCAL_CONTACT_LIST = HashMap<String, ContactInfo>()

    fun updateContactList() {//todo check
//        if (context.checkSelfPermission("android.permission.READ_CONTACTS")
//                != PackageManager.PERMISSION_GRANTED) {
//
//            return
//        }
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
        if (phoneNum.matches(s)) {//数字
            return s
        }
        val markedPhone = markedContactDao.queryBuilder()//by key
                .where(MarkedDataDao.Properties.Key.eq(s), MarkedDataDao.Properties.Type.eq(MarkedData.MARKED_TYPE_CONTACT)).unique()
        if (markedPhone != null) {
            Vog.d(this, "Matched from MarkedData by key $s")
            return markedPhone.value
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
            val list = markedContactDao.queryBuilder()
                    .where(MarkedDataDao.Properties.Type.eq(MarkedData.MARKED_TYPE_CONTACT)).list()
            list.forEach {
                val regex = it.regex
                if (regex.matches(s))
                    return it.value
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

    fun getSimpleList(): List<String> {
        val choiceList = mutableListOf<String>()
        getChoiceData().forEach {
            choiceList.add("${it.title}\n${it.subtitle}")
        }
        return choiceList
    }
}