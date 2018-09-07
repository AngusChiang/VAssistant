package cn.vove7.jarvis.fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
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
import cn.vove7.common.appbus.AppBus
import cn.vove7.common.datamanager.DaoHelper
import cn.vove7.common.datamanager.parse.DataFrom
import cn.vove7.common.datamanager.parse.model.Action
import cn.vove7.common.datamanager.parse.statusmap.ActionNode
import cn.vove7.jarvis.R
import cn.vove7.jarvis.activities.NewInstActivity
import cn.vove7.jarvis.adapters.ExecuteQueueAdapter
import cn.vove7.vtp.dialog.DialogWithList
import java.util.*


@SuppressLint("ValidFragment")
/**
 * # InstDetailFragment
 * 指令详情BSDialog
 * @author 17719247306
 * 2018/8/24
 */
class InstDetailFragment : BottomSheetDialogFragment() {

    private var node: ActionNode? = null
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
        when (node?.from) {//编辑
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
            collapsingColl.title = node?.descTitle
        }
    }

    private fun initView() {
        if (node == null) {
            hide()
            return
        }
        val node = node!!
        collapsingColl = contentView.findViewById(R.id.collapsing_coll)
        toolbar = contentView.findViewById(R.id.toolbar)
        toolbar.inflateMenu(R.menu.menu_inst_detail)
        toolbar.setNavigationOnClickListener { hide() }
        toolbar.setOnMenuItemClickListener { it ->
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
                        ActionNode.belongGlobal(node.actionScopeType) -> ActionNode.NODE_SCOPE_GLOBAL_2
                        ActionNode.belongInApp(node.actionScopeType) -> ActionNode.NODE_SCOPE_IN_APP_2
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
                R.id.menu_run -> {// .. -> it.parent -> it
                    var p: ActionNode? = node
                    val execQueue = mutableListOf<ActionNode>()
                    while (p != null) {
                        p.action.param.value = null
                        execQueue.add(0, p)
                        p = p.parent
                    }
                    if (ActionNode.belongInApp(node.actionScopeType)) {
                        val pkg = node.actionScope.packageName
                        execQueue.add(0, ActionNode("打开App",
                                Action("smartOpen('$pkg')\n" +
                                        "waitForApp('$pkg')", Action.SCRIPT_TYPE_LUA)))

                    }
                    val d = DialogWithList(context!!, ExecuteQueueAdapter(context!!, execQueue))

                            .setButton(DialogInterface.BUTTON_POSITIVE, R.string.text_run, View.OnClickListener { v ->
                                val que = PriorityQueue<Action>()
                                execQueue.forEach {
                                    que.add(it.action)
                                }
                                AppBus.post(que)
                            })
                            .setButton(DialogInterface.BUTTON_NEGATIVE, R.string.text_cancel, View.OnClickListener { v -> })
                    d.setTitle("执行队列")
                    d.show()
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