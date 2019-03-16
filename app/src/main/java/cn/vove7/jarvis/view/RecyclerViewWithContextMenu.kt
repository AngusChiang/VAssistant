package cn.vove7.jarvis.view

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.ContextMenu
import android.view.View

import cn.vove7.vtp.log.Vog

class RecyclerViewWithContextMenu : RecyclerView {
    private val mContextInfo = RecyclerViewContextInfo()

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle) {}

    override fun showContextMenuForChild(originalView: View, x: Float, y: Float): Boolean {
        val layoutManager = layoutManager
        if (layoutManager != null) {
            val position = layoutManager.getPosition(originalView)
            Vog.d("showContextMenuForChild ---> $position")
            mContextInfo.position = position
        }
        return super.showContextMenuForChild(originalView, x, y)
    }

    override fun getContextMenuInfo(): ContextMenu.ContextMenuInfo {
        return mContextInfo
    }

    class RecyclerViewContextInfo : ContextMenu.ContextMenuInfo {
        var position = -1
    }

}
