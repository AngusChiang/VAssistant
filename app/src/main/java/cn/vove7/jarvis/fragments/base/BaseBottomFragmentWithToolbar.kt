package cn.vove7.jarvis.fragments.base

import android.app.Dialog
import android.os.Bundle
import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.BottomSheetDialog
import android.support.design.widget.BottomSheetDialogFragment
import android.support.design.widget.CollapsingToolbarLayout
import android.support.v7.widget.Toolbar
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import cn.vove7.common.view.toast.ColorfulToast
import cn.vove7.jarvis.R

/**
 * # BaseBottomFragmentWithToolbar
 *
 * @author Administrator
 * 9/21/2018
 */
abstract class BaseBottomFragmentWithToolbar : BottomSheetDialogFragment() {

    lateinit var toast: ColorfulToast
    private lateinit var mBehavior: BottomSheetBehavior<*>
    lateinit var contentView: View
    lateinit var contentContainer: ViewGroup

    private lateinit var toolbarImg: ImageView

    lateinit var toolbar: Toolbar
    private lateinit var collapsingColl: CollapsingToolbarLayout

    var title: String? = null
        set(value) {
            collapsingColl.title = value
            field = value
        }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        contentView = View.inflate(context, R.layout.bottom_dialog_with_toolbar, null)
        collapsingColl = contentView.findViewById(R.id.collapsing_coll)
        toolbar = contentView.findViewById(R.id.toolbar)
        toolbar.setNavigationOnClickListener { hide() }
        contentContainer = contentView.findViewById(R.id.content_container)
        toolbarImg = contentView.findViewById(R.id.toolbar_img)
        toast = ColorfulToast(context!!)
        dialog.setContentView(contentView)

        mBehavior = BottomSheetBehavior.from(contentView.parent as View?)
        val v = onCreateContentView(contentContainer)
        contentContainer.addView(v)

        return dialog
    }

    abstract fun onCreateContentView(parent: View): View

    override fun onStart() {
        super.onStart()
        expand()
    }

    fun expand() {
        mBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    fun hide() {
        mBehavior.state = BottomSheetBehavior.STATE_HIDDEN
    }

    fun collaps() {
        mBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }
}