package cn.vove7.jarvis.view.dialog.base

import android.content.Context
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import cn.vove7.common.utils.runOnUi
import cn.vove7.jarvis.R
import cn.vove7.jarvis.adapters.ListViewModelLoader
import cn.vove7.jarvis.adapters.ListViewModel
import cn.vove7.jarvis.adapters.SimpleListAdapter
import kotlinx.android.synthetic.main.dialog_list_view.view.*

/**
 * # BottomDialogWithList
 *
 * @author Administrator
 * 2018/12/20
 */
abstract class BottomDialogWithList<T>(context: Context, title: String)
    : BaseBottomDialogWithToolbar(context, title), ListViewModelLoader<T>,
        SimpleListAdapter.OnItemClickListener<T> {
    private val listAdapter by lazy { SimpleListAdapter(dataSet, this) }
    override var pageIndex: Int = 0

    private val recyclerView: RecyclerView by lazy {
        myView.recycler_view.apply {
            myView.fast_scroller.attachRecyclerView(this)
            layoutManager = LinearLayoutManager(context)
            this.adapter = listAdapter
        }
    }

    override val pageSizeLimit: Int = 0
    override val dataSet: MutableList<ListViewModel<T>> = mutableListOf()

    override fun changeViewOnLoadDone(allLoad: Boolean) {
        runOnUi {
            hideLoadingBar()
            listAdapter.notifyDataSetChanged()
        }
    }

    override fun changeViewOnLoading() {
        showLoadingBar()
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