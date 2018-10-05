package cn.vove7.jarvis.fragments

import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import cn.vove7.common.datamanager.DAO
import cn.vove7.common.datamanager.executor.entity.MarkedData
import cn.vove7.common.datamanager.greendao.MarkedDataDao
import cn.vove7.common.datamanager.parse.DataFrom
import cn.vove7.common.model.UserInfo
import cn.vove7.executorengine.helper.AdvanContactHelper
import cn.vove7.jarvis.R
import cn.vove7.jarvis.adapters.ViewModel
import cn.vove7.jarvis.fragments.base.BaseMarkedFragment
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItems
import kotlin.concurrent.thread

/**
 * # MarkedContractFragment
 *
 * @author 17719247306
 * 2018/9/4
 */
class MarkedContractFragment : BaseMarkedFragment<MarkedData>() {

    private var onlySelf = false
    override val keyHint: Int = R.string.text_show_name
    override val valueHint: Int = R.string.text_phone
    override val lastKeyId: Int=R.string.key_last_sync_marked_contact_date

    override fun onSelect() {
        //选择联系人
        val phoneList = AdvanContactHelper.getSimpleList()
        MaterialDialog(context!!)
                .title(R.string.text_select_contact)
                .listItems(items = phoneList, waitForPositiveButton = false) { _, i, s ->
                    setValue(s.split("\n")[1])
                }.show()
    }

    override var markedType: String = MarkedData.MARKED_TYPE_CONTACT

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        buildHeader("仅显示我的", lis = CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            onlySelf = isChecked
            refresh()
        })
    }

    override fun transData(nodes: List<MarkedData>): List<ViewModel> {
        val ss = mutableListOf<ViewModel>()
        nodes.forEach {
            ss.add(ViewModel(it.key, it.value, null, it))
        }
        return ss
    }

    override fun onGetData(pageIndex: Int) {
        thread {
            val builder = DAO.daoSession.markedDataDao
                    .queryBuilder()
                    .where(MarkedDataDao.Properties.Type.eq(MarkedData.MARKED_TYPE_CONTACT))
                    .offset(pageSizeLimit * pageIndex)
                    .limit(pageSizeLimit)

            if (onlySelf) {
                builder.whereOr(MarkedDataDao.Properties.From.eq(DataFrom.FROM_USER),
                        builder.and(MarkedDataDao.Properties.From.eq(DataFrom.FROM_SHARED),
                                MarkedDataDao.Properties.PublishUserId.eq(UserInfo.getUserId()))
                )
            }
            val list = builder.list()

            dataSet.addAll(transData(list))
            resultHandler.sendEmptyMessage(list.size)
        }
    }
}

