package cn.vove7.jarvis.fragments.base

import android.view.View
import cn.vove7.common.datamanager.DaoHelper
import cn.vove7.common.datamanager.executor.entity.MarkedData
import cn.vove7.common.netacc.ApiUrls
import cn.vove7.common.netacc.NetHelper
import cn.vove7.common.netacc.model.BaseRequestModel
import cn.vove7.common.netacc.model.ResponseMessage
import cn.vove7.common.netacc.model.SyncMarkedModel
import cn.vove7.jarvis.R
import cn.vove7.jarvis.fragments.SimpleListFragment
import cn.vove7.jarvis.view.utils.TextHelper
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.fragment_base_list.view.*

/**
 * # BaseMarkedFragment
 *
 * @author Administrator
 * 2018/9/16
 */
abstract class BaseMarkedFragment<T> : SimpleListFragment<T>(), OnSyncMarked {
    override var floatClickListener: View.OnClickListener? = View.OnClickListener {
//        onSync(types)
    }

    override fun onSync(types: Array<String>) {
        showProgressBar()
        val syncData = SyncMarkedModel(TextHelper.arr2String(types))
        val requestModel= BaseRequestModel(syncData)

        NetHelper.postJson<List<MarkedData>>(ApiUrls.SYNC_MARKED, requestModel, type = object
            : TypeToken<ResponseMessage<List<MarkedData>>>() {}.type) { _, bean ->
            if (bean != null) {
                if (bean.isOk()) {
                    //
                    DaoHelper.updateMarkedData(types, bean.data ?: emptyList())
                    toast.showShort("同步完成")
                    refresh()
                } else {
                    toast.showShort(bean.message)
                }
            } else toast.showShort(R.string.text_net_err)
            hideProgressBar()
        }
    }

}

interface OnSyncMarked {
    fun onSync(types: Array<String>)
}