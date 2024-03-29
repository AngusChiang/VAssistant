package cn.vove7.common.helper

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.app.GlobalLog
import cn.vove7.common.appbus.AppBus
import cn.vove7.common.bridges.ChoiceData
import cn.vove7.common.bridges.GenChoiceData
import cn.vove7.common.datamanager.DAO
import cn.vove7.common.datamanager.executor.entity.MarkedData
import cn.vove7.common.datamanager.greendao.MarkedDataDao
import cn.vove7.common.model.MatchedData
import cn.vove7.common.model.RequestPermission
import cn.vove7.common.interfaces.Markable
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
    val context: Context
        get() = GlobalApp.APP
    //Dao
    private val markedContactDao get() = DAO.daoSession.markedDataDao

    override fun addMark(data: MarkedData) {
        //数据库
        markedContactDao.insert(data)
    }

    private val phoneNum = "[\\s\\-0-9]*".toRegex()

    private var lastUpdateTime = 0L

    private const val updateInterval = 30 * 60 * 1000
    private const val limitRate = 0.75f
    /**
     * 存储本地联系人
     */
    private val LOCAL_CONTACT_LIST = HashMap<String, ContactInfo>()

    private fun updateContactList() {
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            AppBus.post(RequestPermission("联系人权限"))
            return
        }
        val now = System.currentTimeMillis()
        //更新
        if (now - lastUpdateTime > updateInterval) {
            lastUpdateTime = now
            LOCAL_CONTACT_LIST.clear()
            try {
                LOCAL_CONTACT_LIST.putAll(ContactHelper.getAllContacts(context))
            } catch (e: Exception) {
                if (e is ClassCastException) {
                    AppBus.post(RequestPermission("联系人权限"))
                } else GlobalLog.err(e)

            }
        }
    }

    /**
     * 纯数字 -> 标记 -> 模糊匹配 -> 匹配提供
     * @return Pair<S,S>  first: 匹配的联系人姓名 or 纯数字 second: 手机号
     */
    fun matchPhone(s: String, update: Boolean = true): Pair<String, Array<String>>? {
        if (phoneNum.matches(s)) {//数字
            return Pair(s, arrayOf(s))
        }
        val markedPhone = markedContactDao.queryBuilder()//by key
                .where(MarkedDataDao.Properties.Key.eq(s), MarkedDataDao.Properties.Type.eq(MarkedData.MARKED_TYPE_CONTACT)).unique()
        if (markedPhone != null) {
            Vog.d("Matched from MarkedData by key $s")
            return Pair(markedPhone.key, arrayOf(markedPhone.value))
        }
        //本地匹配
        val localMatched = LOCAL_CONTACT_LIST[s]
        if (localMatched != null) {
            return Pair(localMatched.contactName, localMatched.phones.toTypedArray())
        }
        //更新列表
        if (update) {
            updateContactList()
            return matchPhone(s, false)
        }
        //模糊匹配 -> 匹配提供
        val matchedList = fuzzyMatching(s)
        return if (matchedList.isNotEmpty()) {
            Pair(matchedList[0].data.contactName, matchedList[0].data.phones.toTypedArray())
        } else {//匹配提供
            Vog.d("match by regex")

            //正则匹配
            val list = markedContactDao.queryBuilder()
                    .where(MarkedDataDao.Properties.Type.eq(MarkedData.MARKED_TYPE_CONTACT)).list()
            list.forEach {
                if (TextHelper.compareSimilarityWithPinyin(GlobalApp.APP, s, it.key) >= 0.75) // key 模糊查询
                    return Pair(it.key, arrayOf(it.value))

                val regex = it.regex
                if (regex.matches(s))
                    return Pair(it.key, arrayOf(it.value))
            }
            Vog.d("from server no one")
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
                Vog.d("${it.contactName}: $rate")
                matchList.add(MatchedData(rate, it))
            }
        }
        Vog.d("模糊匹配联系人: ${matchList.size}")
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

    /**
     * 数据格式：
     * @return List<String>
     */
    fun getSimpleList(): List<Pair<String,String>> {
        val choiceList = mutableListOf<Pair<String,String>>()
        getChoiceData().forEach {
            choiceList.add(Pair(it.title,it.subtitle?:""))
        }
        return choiceList
    }

    fun getContactName(): Array<String> {
        if (LOCAL_CONTACT_LIST.isEmpty()) updateContactList()
        val set = hashSetOf(*LOCAL_CONTACT_LIST.keys.toTypedArray())
        return set.toTypedArray()
    }
}