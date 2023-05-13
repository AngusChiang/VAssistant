package cn.vove7.jarvis.fragments

import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import cn.vove7.common.datamanager.DAO
import cn.vove7.common.datamanager.executor.entity.MarkedData
import cn.vove7.common.datamanager.greendao.MarkedDataDao
import cn.vove7.common.datamanager.parse.DataFrom
import cn.vove7.common.model.UserInfo
import cn.vove7.common.utils.CoroutineExt.launch
import cn.vove7.jarvis.R
import cn.vove7.jarvis.adapters.ListViewModel
import cn.vove7.jarvis.fragments.base.BaseMarkedFragment
import cn.vove7.jarvis.view.dialog.SelectContactDialog

/**
 * # MarkedContractFragment
 *
 * @author Vove
 * 2018/9/4
 */
class MarkedContractFragment : BaseMarkedFragment() {

    private var onlySelf = false
    override val keyHint: Int = R.string.text_show_name
    override val valueHint: Int = R.string.text_phone
    override val lastKeyId: Int = R.string.key_last_sync_marked_contact_date

    val selContactDialog: SelectContactDialog by lazy {
        SelectContactDialog(context!!) {
            setValue(it.second)
        }
    }

    override fun onSelect() {
        //选择联系人
        selContactDialog.show()
    }


    override var markedType: String = MarkedData.MARKED_TYPE_CONTACT

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        buildHeader("仅显示我的", lis = CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            onlySelf = isChecked
            refresh()
        })
    }

    override fun unification(data: MarkedData): ListViewModel<MarkedData>? {
        return ListViewModel(data.key, data.value, null, data)
    }

    override fun onLoadData(pageIndex: Int) {
        launch {
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
            notifyLoadSuccess(list)
        }
    }
}

