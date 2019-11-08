package cn.vove7.jarvis.view.bottomsheet

import android.content.Context
import androidx.annotation.CallSuper
import com.google.android.material.bottomsheet.BottomSheetBehavior
import android.view.View

/**
 * # BottomSheetController
 *
 * @author 11324
 * 2019/1/21
 */
open class BottomSheetController(val context: Context,
                                 val bottomView: View) {
    init {
        bottomView.post {
            initBottomSheetView()
        }
    }

   var behavior: BottomSheetBehavior<*> = BottomSheetBehavior.from(bottomView)


    val isBottomSheetShowing: Boolean
        get() = behavior.state != BottomSheetBehavior.STATE_HIDDEN


    fun hideBottom() {
        behavior.state = BottomSheetBehavior.STATE_HIDDEN
    }

    fun showBottom() {
        behavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    fun expandSheet() {
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    /**
     * 复写onItemClick
     */
    open fun initBottomSheetView() {}

    fun <T : View> f(id: Int): T {
        return bottomView.findViewById(id)
    }
}