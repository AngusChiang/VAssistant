package cn.vove7.jarvis.fragments

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.BottomSheetDialog
import android.support.design.widget.BottomSheetDialogFragment
import android.support.design.widget.CollapsingToolbarLayout
import android.support.v7.widget.Toolbar
import android.view.View
import android.widget.ImageView
import cn.vove7.datamanager.parse.DataFrom
import cn.vove7.datamanager.parse.statusmap.ActionNode
import cn.vove7.jarvis.R
import cn.vove7.jarvis.activities.NewInstActivity


@SuppressLint("ValidFragment")
/**
 * # InstDetailFragment
 * 指令详情BSDialog
 * @author 17719247306
 * 2018/8/24
 */
class InstDetailFragment : BottomSheetDialogFragment() {

    private lateinit var node: ActionNode
    private lateinit var mBehavior: BottomSheetBehavior<*>
    lateinit var contentView: View
    private lateinit var toolbarImg: ImageView
    lateinit var toolbar: Toolbar
    private lateinit var collapsingColl: CollapsingToolbarLayout

    private var load = false

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        contentView = View.inflate(context, R.layout.dialog_inst_detail, null)
        dialog.setContentView(contentView)
        initView()
        mBehavior = BottomSheetBehavior.from(contentView.parent as View)
        setData()
        load = true
        return dialog
    }


    fun setInst(node: ActionNode) {
        this.node = node
        if (!load) return
        setData()
    }

    private fun setData() {
        when (node.from) {//编辑
            DataFrom.FROM_SERVICE, DataFrom.FROM_SHARED -> {
                val editMenu = toolbar.menu.findItem(R.id.menu_edit)
                editMenu.isEnabled = false
                editMenu.title = getString(R.string.text_not_editable)

            }
            DataFrom.FROM_USER -> {
                toolbar.menu.findItem(R.id.menu_edit).isEnabled = true
            }
        }
        contentView.post {
            //标题
            collapsingColl.title = node.descTitle
        }
    }

    private fun initView() {
        collapsingColl = contentView.findViewById(R.id.collapsing_coll)
        toolbar = contentView.findViewById(R.id.toolbar)
        toolbar.inflateMenu(R.menu.menu_inst_detail)
        toolbar.setNavigationOnClickListener { hide() }
        toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.menu_edit -> {//修改
                    val editIntent = Intent(context, NewInstActivity::class.java)
                    editIntent.putExtra("nodeId", node.id)
                    editIntent.putExtra("type", node.nodeType)
                    editIntent.putExtra("pkg", node.actionScope.packageName)
                    editIntent.putExtra("reedit", true)
                    startActivity(editIntent)
                }
                R.id.menu_delete -> {//删除

                }
            }
            return@setOnMenuItemClickListener true
        }
        toolbarImg = contentView.findViewById(R.id.toolbar_img)
    }

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