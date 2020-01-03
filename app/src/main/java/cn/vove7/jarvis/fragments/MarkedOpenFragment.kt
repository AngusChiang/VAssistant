package cn.vove7.jarvis.fragments

import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import cn.vove7.common.datamanager.DAO
import cn.vove7.common.datamanager.executor.entity.MarkedData
import cn.vove7.common.datamanager.executor.entity.MarkedData.MARKED_TYPE_SCRIPT_LUA
import cn.vove7.common.datamanager.greendao.MarkedDataDao
import cn.vove7.common.datamanager.parse.DataFrom
import cn.vove7.common.model.UserInfo
import cn.vove7.common.utils.CoroutineExt.launch
import cn.vove7.jarvis.R
import cn.vove7.jarvis.adapters.ListViewModel
import cn.vove7.jarvis.fragments.base.BaseMarkedFragment

/**
 * # MarkedOpenFragment
 *
 * @author 17719247306
 * 2018/9/7
 */
class MarkedOpenFragment : BaseMarkedFragment() {

    override var markedType: String = MarkedData.MARKED_TYPE_SCRIPT_LUA
    override val keyHint: Int = R.string.text_func_name
    override val valueHint: Int = R.string.text_script
    override val lastKeyId: Int=R.string.key_last_sync_marked_open_date

    override val showSel: Boolean = false

    override fun unification(data: MarkedData): ListViewModel<MarkedData>? {
        return ListViewModel(data.key, null, extra = data)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        buildHeader("仅显示我的", lis = CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            onlySelf = isChecked
            refresh()
        })
    }

    var onlySelf = false

    override fun onLoadData(pageIndex: Int) {
        launch {
            val builder = DAO.daoSession.markedDataDao
                    .queryBuilder()
                    .where(MarkedDataDao.Properties.Type.`in`(MarkedData.MARKED_TYPE_SCRIPT_JS, MARKED_TYPE_SCRIPT_LUA))
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
