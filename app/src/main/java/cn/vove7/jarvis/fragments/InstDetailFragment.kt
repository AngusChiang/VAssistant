package cn.vove7.jarvis.fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.BottomSheetDialog
import android.support.design.widget.BottomSheetDialogFragment
import android.support.design.widget.CollapsingToolbarLayout
import android.support.v7.widget.Toolbar
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.app.GlobalLog
import cn.vove7.common.appbus.AppBus
import cn.vove7.common.datamanager.DAO
import cn.vove7.common.datamanager.DaoHelper
import cn.vove7.common.datamanager.parse.DataFrom
import cn.vove7.common.datamanager.parse.model.Action
import cn.vove7.common.datamanager.parse.model.ActionDesc
import cn.vove7.common.datamanager.parse.statusmap.ActionNode
import cn.vove7.common.model.UserInfo
import cn.vove7.common.netacc.ApiUrls
import cn.vove7.common.netacc.NetHelper
import cn.vove7.common.netacc.model.BaseRequestModel
import cn.vove7.common.netacc.model.ResponseMessage
import cn.vove7.common.view.toast.ColorfulToast
import cn.vove7.executorengine.bridges.SystemBridge
import cn.vove7.jarvis.R
import cn.vove7.jarvis.activities.NewInstActivity
import cn.vove7.jarvis.adapters.ExecuteQueueAdapter
import cn.vove7.jarvis.view.dialog.ProgressDialog
import cn.vove7.vtp.dialog.DialogWithList
import cn.vove7.vtp.log.Vog
import cn.vove7.vtp.sharedpreference.SpHelper
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.checkbox.checkBoxPrompt
import com.afollestad.materialdialogs.customview.customView
import com.google.gson.reflect.TypeToken
import java.util.*
import kotlin.concurrent.thread


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

    lateinit var toast: ColorfulToast
    private var load = false

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        contentView = View.inflate(context, R.layout.dialog_inst_detail, null)
        dialog.setContentView(contentView)
        toast = ColorfulToast(context!!)
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
        val uId = UserInfo.getUserId()
        when {//编辑
            node?.from == DataFrom.FROM_USER && node?.publishUserId ?: uId == uId -> {
                toolbar.menu.findItem(R.id.menu_edit).isVisible = true
                toolbar.menu.findItem(R.id.menu_share).isVisible = true
                toolbar.menu.findItem(R.id.menu_add_follow).isVisible = true
            }
            else -> {
                toolbar.menu.findItem(R.id.menu_edit).isVisible = false
                toolbar.menu.findItem(R.id.menu_share).isVisible = false
                toolbar.menu.findItem(R.id.menu_add_follow).isVisible = false
            }
        }
        toolbar.menu.findItem(R.id.menu_as_global).isVisible = node?.belongInApp() ?: false
        contentView.post {
            //标题
            collapsingColl.title = node?.actionTitle
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
                    AlertDialog.Builder(context).setTitle(getString(R.string.text_confirm_2_del) + ": ${node.actionTitle} ?")
                            .setNeutralButton(getString(R.string.text_help)) { _, _ ->
                                try {
                                    SystemBridge().openUrl("http://baidu.com")// TODO
                                } catch (e: ActivityNotFoundException) {
                                    GlobalApp.toastShort("无可用浏览器")
                                }
                            }
                            .setMessage(getString(R.string.text_msg_delete_action_node))
                            .setPositiveButton(R.string.text_confirm) { _, _ ->
                                val p = ProgressDialog(context!!)

                                thread {
                                    val b = DaoHelper.deleteActionNodeInTX(node.id)
                                    p.dismiss()
                                    GlobalApp.toastShort(
                                            if (b) "删除成功" else "删除失败，可至帮助进行反馈"
                                    )
                                }
                            }
                            .setNegativeButton(R.string.text_cancel, null)
                            .show()
                }
                R.id.menu_add_follow -> {//todo 选择: 从已有(inApp Global)关联parentId，或者新建

                    val editIntent = Intent(context, NewInstActivity::class.java)
                    val type = when {
                        ActionNode.belongGlobal(node.actionScopeType) -> ActionNode.NODE_SCOPE_GLOBAL
                        ActionNode.belongInApp(node.actionScopeType) -> ActionNode.NODE_SCOPE_IN_APP
                        else -> {
                            GlobalLog.log("maybe error")
                            GlobalApp.toastShort("maybe error")
                            return@setOnMenuItemClickListener true
                        }
                    }
                    editIntent.putExtra("nodeId", node.id)
                    editIntent.putExtra("type", type)
                    editIntent.putExtra("parent_title", node.actionTitle)
                    val scope = node.actionScope
                    if (scope != null)
                        editIntent.putExtra("pkg", node.actionScope.packageName)
                    editIntent.putExtra("reedit", false)
                    startActivity(editIntent)
                }
                R.id.menu_run -> {
                    showRunDialog()
                }
                R.id.menu_share -> {
                    if (UserInfo.isLogin().not()) {
                        toast.red().showShort("请登录")
                    } else if (node.parentId != null && node.parentTagId == null) {//检查parent
                        toast.red().showShort("请先上传上级操作")
                    } else
                        showShareDialog()
                }
                R.id.menu_as_global -> {//copy as global
                    val sp = SpHelper(context!!)
//                    val prompt = sp.getBoolean(R.string.key_show_prompt_inapp_as_global_dialog)

//                    if (prompt) {
                    //重要信息
                    MaterialDialog(context!!).title(text = "设为全局命令")
                            .message(text = "此操作会复制此命令(包括跟随操作)至全局，若不选择复制跟随操作，" +
                                    "选择下方[不包含]即可。\n注意：跟随操作不允许此操作\n跟随操作最多之复制一层")
                            .checkBoxPrompt(text = "不再提醒") {
                                sp.set(R.string.key_show_prompt_inapp_as_global_dialog, it)
                            }.positiveButton(R.string.text_continue) {
                                doCopy2Global(true)
                            }.neutralButton(text = "不包含") {
                                doCopy2Global(false)
                            }
                            .negativeButton().show()
//                    } else {
//                        doCopy2Global()
//                    }
                }
            }
            return@setOnMenuItemClickListener true
        }
        toolbarImg = contentView.findViewById(R.id.toolbar_img)
    }

    var shareDialog: MaterialDialog? = null

    lateinit var p: ProgressDialog
    private fun doCopy2Global(containSub: Boolean) {
        if (node != null) {
            if (node!!.parentId != null) {
                toast.showLong("跟随操作不允许此操作")
                return
            }
            p = ProgressDialog(context!!)
            thread {
                val cloneNode: ActionNode?
                try {
                    cloneNode = node!!.cloneGlobal(containSub)
                    val s = DaoHelper.copyInApp2Global(cloneNode)
                    toast.showLong(s)

                } catch (e: Exception) {
                    GlobalLog.err("${e.message} code:id230")
                    toast.showLong("复制失败")
                }
                p.dismiss()
            }
        } else {
            GlobalLog.err("code: id211")
            toast.red().showShort(R.string.text_error_occurred)
        }

    }

    /**
     * 更新 or upload
     */
    private fun showShareDialog() {
        if (node == null) {
            hide()
            GlobalLog.err("id188")
            toast.red().showShort(R.string.text_error_occurred)
            return
        }
        //判断首次分享
        val upgrade = node!!.tagId != null
        //dialog 说明|示例
        val dView = layoutInflater.inflate(R.layout.dialog_share_inst, null)
        var desc = node!!.desc
        val descText = dView.findViewById<EditText>(R.id.desc_text)
        val exText = dView.findViewById<EditText>(R.id.example_text)
        if (desc != null) {
            descText.setText(desc.instructions)
            exText.setText(desc.example)
        }
        if (shareDialog == null) {
            val sId = if (upgrade)
                R.string.text_upgrade
            else R.string.text_share
            shareDialog = MaterialDialog(context!!)
                    .title(sId)
                    .customView(view = dView)
                    .positiveButton(sId) {
                        //check info
                        val descStr = descText.text.toString()
                        val exStr = exText.text.toString()
                        if (descStr.trim() == "") {
                            toast.red().showShort("")
                            return@positiveButton
                        }
                        //set desc
                        desc = ActionDesc(descStr, exStr)
                        DAO.daoSession.actionDescDao.insert(desc)
                        node?.descId = desc.id
                        //set userId
                        node?.publishUserId = UserInfo.getUserId()
                        //update
                        node?.update()
                        node!!.desc = desc
                        //assembly
                        node!!.assembly()
                        //share
                        if (upgrade) upgrade()
                        else firstShare()
                    }.negativeButton(R.string.text_cancel)
        }
        shareDialog!!.show()
    }

    private fun upgrade() {
        val type = object : TypeToken<ResponseMessage<Int>>() {}.type
        NetHelper.postJson<Int>(ApiUrls.UPGRADE_INST, BaseRequestModel(node), type = type, callback = { _, bean ->
            if (bean != null) {
                if (bean.isOk()) {
                    toast.blue().showShort(R.string.text_share_success)
                    //sign tag
                    val s = bean.data ?: -1
                    if (s < 0) {
                        return@postJson
                    }
                    thread {
                        //更新 tagId
                        node!!.versionCode = s
                        Vog.d(this, "new ver---> $s")
                        node!!.update()
                    }
                } else {
                    toast.red().showShort(bean.message)
                }
            } else {
                toast.red().showShort(R.string.text_net_err)
            }
        })
    }

    private fun firstShare() {
        val type = object : TypeToken<ResponseMessage<String>>() {}.type
        NetHelper.postJson<String>(ApiUrls.SHARE_INST, BaseRequestModel(node), type = type, callback = { _, bean ->
            if (bean != null) {
                if (bean.isOk()) {
                    toast.blue().showShort(R.string.text_share_success)
                    //sign tag
                    val s = bean.data
                    thread {
                        //更新 tagId
                        Vog.d(this, "share new tag---> $s")
                        node!!.tagId = s
                        node!!.update()
                        node!!.follows.forEach { child ->
                            child.parentTagId = s
                            child.update()
                        }
                    }
                } else {
                    toast.red().showShort(bean.message)
                }
            } else {
                toast.red().showShort(R.string.text_net_err)
            }
        })

    }

    private fun showRunDialog() {
        if (node == null) {
            hide()
            GlobalLog.err("id243")
            toast.red().showShort(R.string.text_error_occurred)
            return
        }
        // .. -> it.parent -> it
        var p: ActionNode? = node
        val execQueue = mutableListOf<ActionNode>()
        while (p != null) {
            p.action.param.value = null
            execQueue.add(0, p)
            p = p.parent
        }
        if (ActionNode.belongInApp(node!!.actionScopeType)) {
            val pkg = node!!.actionScope.packageName
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