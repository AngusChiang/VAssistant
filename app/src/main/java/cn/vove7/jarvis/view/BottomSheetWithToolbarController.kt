package cn.vove7.jarvis.view

import android.content.Context
import android.support.annotation.MenuRes
import android.support.design.widget.BottomSheetBehavior
import android.support.v7.widget.Toolbar
import android.view.View
import android.widget.TextView
import cn.vove7.jarvis.R

/**
 * # BottomSheetWithToolbarController
 *
 * @author Administrator
 * 2018/11/6
 */
class BottomSheetWithToolbarController<Type>(
        context: Context, bottomView: View, var bottomTitle: String? = null,
        @MenuRes val menuId: Int? = null
) : BottomSheetController<Type>(context, bottomView) {
    init {
        bottomView.visibility = View.VISIBLE
        initBottomSheetView()
    }


    fun setTitle(title: String?) {
        if (title != null) {
            this.bottomTitle = title
            bottomToolbar.title = title
        }
    }


    lateinit var bottomToolbar: Toolbar
    lateinit var errLayout: View
    lateinit var errText: TextView
    lateinit var progressBar: View

    companion object {
        const val SHOW_LIST = 0
        const val SHOW_PROCESS = 1
        const val SHOW_ERR = 2
    }

    /**
     * 复写onItemClick
     */

    override fun initBottomSheetView(hide: Boolean) {
        super.initBottomSheetView(hide)

        bottomToolbar = f(R.id.bottom_toolbar)
        progressBar = f(R.id.refresh_bar)
        errLayout = f(R.id.err_layout)
        bottomToolbar.setNavigationOnClickListener { hideBottom() }
        behavior = BottomSheetBehavior.from(bottomView)
        if (menuId !== null)
            bottomToolbar.inflateMenu(menuId)

        setTitle(bottomTitle)
    }

    fun onSuccess() {
        changeBottomLayout(SHOW_LIST)
    }

    fun onLoading() {
        changeBottomLayout(SHOW_PROCESS)
    }

    fun onErr(msg: String? = null) {
        if (msg != null)
            errText.text = msg
        changeBottomLayout(SHOW_ERR)
    }

    //改变bottom布局，刷新，err
    fun changeBottomLayout(showIndex: Int) {
        val views = arrayOf(bottomListView, progressBar, errLayout)

        for ((index, v) in views.withIndex()) {
            if (showIndex == index)
                v.visibility = View.VISIBLE
            else
                v.visibility = View.GONE
        }
    }

}
