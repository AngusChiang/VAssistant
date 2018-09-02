package cn.vove7.jarvis.fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
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
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.app.GlobalLog
import cn.vove7.common.datamanager.DaoHelper
import cn.vove7.common.datamanager.parse.DataFrom
import cn.vove7.common.datamanager.parse.statusmap.ActionNode
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
                    editIntent.putExtra("type", node.actionScopeType)
                    val scope = node.actionScope
                    if (scope != null)
                        editIntent.putExtra("pkg", node.actionScope.packageName)
                    editIntent.putExtra("reedit", true)
                    startActivity(editIntent)
                }
                R.id.menu_delete -> {//删除
                    AlertDialog.Builder(context).setTitle("确认删除操作: " + node.descTitle + " ?")
                            .setPositiveButton(R.string.text_confirm) { _, _ ->
                                DaoHelper.delectActionNode(node.id)
                            }
                            .setNegativeButton(R.string.text_cancel, null)
                            .show()
                }
                R.id.menu_add_follow -> {
                    val editIntent = Intent(context, NewInstActivity::class.java)
                    val type = when {
                        arrayListOf(ActionNode.NODE_SCOPE_GLOBAL, ActionNode.NODE_SCOPE_GLOBAL_2)
                                .contains(node.actionScopeType) -> ActionNode.NODE_SCOPE_GLOBAL_2
                        arrayListOf(ActionNode.NODE_SCOPE_IN_APP, ActionNode.NODE_SCOPE_IN_APP_2)
                                .contains(node.actionScopeType) -> ActionNode.NODE_SCOPE_IN_APP_2
                        else -> {
                            GlobalLog.log("maybe error")
                            GlobalApp.toastShort("maybe error")
                            return@setOnMenuItemClickListener true
                        }
                    }
                    editIntent.putExtra("nodeId", node.id)
                    editIntent.putExtra("type", type)
                    editIntent.putExtra("parent_title", node.descTitle)
                    val scope = node.actionScope
                    if (scope != null)
                        editIntent.putExtra("pkg", node.actionScope.packageName)
                    editIntent.putExtra("reedit", false)
                    startActivity(editIntent)
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