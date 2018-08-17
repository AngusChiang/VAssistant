package cn.vove7.jarvis.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import cn.vove7.datamanager.DAO
import cn.vove7.datamanager.greendao.MapNodeDao
import cn.vove7.datamanager.parse.statusmap.MapNode
import cn.vove7.jarvis.R
import cn.vove7.jarvis.adapters.RecAdapterWithFooter
import cn.vove7.jarvis.utils.BundleBuilder
import cn.vove7.vtp.log.Vog
import cn.vove7.vtp.toast.Voast

/**
 *
 * 指令管理列表
 * Created by 17719 on 2018/8/13
 */

class InstListFragment : VListFragment() {

    private var instType: Int = 0

    override var floatClickListener: View.OnClickListener? = View.OnClickListener {
        Voast.with(context!!).showLong("add")
    }


    companion object {
        const val INST_TYPE_GLOBAL = 0
        const val INST_TYPE_APP_INNER = 1

        fun newInstance(type: Int): InstListFragment {
            val f = InstListFragment()
            f.arguments = BundleBuilder().put("type", type).get()
            return f
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        instType = arguments?.getInt("type") ?: INST_TYPE_GLOBAL
    }

    private val pageSizeLimit = 10
    private val actionNodes = mutableListOf<MapNode>()
    override fun initView(contentView: View) {
        adapter = ListAdapter(actionNodes)

    }

    override fun clearDataSet() {
        actionNodes.clear()
    }

    override fun onGetData(pageIndex: Int) {
        super.onGetData(pageIndex)
        val offsetDatas = DAO.daoSession.mapNodeDao.queryBuilder()
                .where(MapNodeDao.Properties.NodeType.eq(
                        when (instType) {
                            INST_TYPE_APP_INNER -> MapNode.NODE_TYPE_IN_APP
                            else -> MapNode.NODE_TYPE_GLOBAL
                        }
                )).offset(pageIndex * pageSizeLimit)
                .limit(pageSizeLimit).list()

        Vog.d(this, "onGetData $actionNodes")
        actionNodes.addAll(offsetDatas)

        notifyLoadSuccess(offsetDatas.isEmpty())
    }

    class ListAdapter(val globalActionNodes: MutableList<MapNode>) : RecAdapterWithFooter<VHolder>() {
        override fun itemCount(): Int = globalActionNodes.size
        override fun onBindView(holder: VHolder, position: Int) {
            holder.title?.text = globalActionNodes[position].descTitle
        }

        override fun onCreateHolder(parent: ViewGroup, viewType: Int): VHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_inst, parent, false)
            return VHolder(view)
        }
    }


    class VHolder(v: View, adapter: RecAdapterWithFooter<RecAdapterWithFooter.RecViewHolder>? = null)
        : RecAdapterWithFooter.RecViewHolder(v, adapter) {
        var icon: ImageView? = null
        var title: TextView? = null

        init {
            if (adapter == null) {
                icon = v.findViewById(R.id.icon)
                title = v.findViewById(R.id.title)
            }
        }
    }
}