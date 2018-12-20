package cn.vove7.jarvis.view.dialog.base

import android.content.Context
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ProgressBar
import cn.vove7.common.utils.gone
import cn.vove7.common.utils.runOnUi
import cn.vove7.common.utils.show
import cn.vove7.jarvis.R
import cn.vove7.jarvis.ViewModelLoader
import cn.vove7.jarvis.adapters.ListViewModel
import cn.vove7.jarvis.adapters.SimpleListAdapter
import cn.vove7.jarvis.fragments.base.BaseBottomDialogWithToolbar
import com.pluscubed.recyclerfastscroll.RecyclerFastScroller

/**
 * # BottomDialogWithList
 *
 * @author Administrator
 * 2018/12/20
 */
abstract class BottomDialogWithList<T>(context: Context, title: String)
    : BaseBottomDialogWithToolbar(context, title), ViewModelLoader<T>,
        SimpleListAdapter.OnItemClickListener<T> {
    private val pbar by lazy { myView.findViewById<ProgressBar>(R.id.p_bar) }
    private val listAdapter by lazy { SimpleListAdapter(dataSet, this) }
    override var pageIndex: Int = 0

    val recyclerView: RecyclerView by lazy {
        myView.findViewById<RecyclerView>(R.id.recycler_view).apply {
            myView.findViewById<RecyclerFastScroller>(R.id.fast_scroller).attachRecyclerView(this)
            layoutManager = LinearLayoutManager(context)
            this.adapter = listAdapter
        }
    }

    override val pageSizeLimit: Int = 0
    override val dataSet: MutableList<ListViewModel<T>> = mutableListOf()

    override fun changeViewOnLoadDone(allLoad: Boolean) {
        runOnUi {
            pbar.gone()
            listAdapter.notifyDataSetChanged()
        }
    }

    override fun changeViewOnLoading() {
        runOnUi {
            pbar.show()
        }
    }


    override fun onBackPressed() {
        if(toolbar.hasExpandedActionView()) {
            toolbar.collapseActionView()
            return
        }
        super.onBackPressed()
    }

    private val myView by lazy { layoutInflater.inflate(R.layout.dialog_list_view, null) }
    override fun onCreateContentView(parent: View): View = myView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        recyclerView // init
        toolbar.setNavigationIcon(R.drawable.ic_close_black_24dp)
        toolbar.setNavigationOnClickListener { dismiss() }
        refreshList()
        setOnDismissListener {
            toolbar.collapseActionView()
        }
    }
}