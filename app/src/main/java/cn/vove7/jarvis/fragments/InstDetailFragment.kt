package cn.vove7.jarvis.fragments

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.view.View
import android.widget.EditText
import android.widget.TextView
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
import cn.vove7.common.netacc.model.BaseRequestModel
import cn.vove7.common.netacc.model.ResponseMessage
import cn.vove7.common.utils.TextHelper
import cn.vove7.executorengine.bridges.SystemBridge
import cn.vove7.jarvis.R
import cn.vove7.jarvis.activities.NewInstActivity
import cn.vove7.jarvis.adapters.ExecuteQueueAdapter
import cn.vove7.jarvis.fragments.base.BaseBottomFragmentWithToolbar
import cn.vove7.jarvis.utils.AppConfig
import cn.vove7.jarvis.utils.NetHelper
import cn.vove7.jarvis.view.dialog.ProgressDialog
import cn.vove7.vtp.dialog.DialogWithList
import cn.vove7.vtp.log.Vog
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.list.listItems
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
class InstDetailFragment(val node: ActionNode, val onUpdate: () -> Unit) : BaseBottomFragmentWithToolbar() {

//    private var node: ActionNode? = null

    private var load = false
    private val instructions_text: TextView by lazy { bodyView.findViewById<TextView>(R.id.instructions_text) }
    private val examples_text: TextView by lazy { bodyView.findViewById<TextView>(R.id.examples_text) }
    private val regs_text: TextView by lazy { bodyView.findViewById<TextView>(R.id.regs_text) }
    private val version_text: TextView by lazy { bodyView.findViewById<TextView>(R.id.version_text) }
    private val script_text: TextView by lazy { bodyView.findViewById<TextView>(R.id.script_text) }
    private val script_type_text: TextView by lazy { bodyView.findViewById<TextView>(R.id.script_type_text) }

    lateinit var bodyView: View
    override fun onCreateContentView(parent: View): View {

        bodyView = View.inflate(context, R.layout.dialog_inst_detail, null)
        toolbar.inflateMenu(R.menu.menu_inst_detail)
        initView()
        setData()
        load = true
        return bodyView
    }
//
//    fun setInst(node: ActionNode) {
//        this.node = node
//        if (!load) return
//        setData()
//    }

    //todo fill data  share user info
    private fun setData() {
        val uId = UserInfo.getUserId()
        when {//编辑
            DataFrom.userCanEdit(node.from) && node.publishUserId ?: uId == uId -> {
                toolbar.menu.findItem(R.id.menu_edit).isVisible = true
                toolbar.menu.findItem(R.id.menu_share).isVisible = true
//                toolbar.menu.findItem(R.id.menu_add_follow).isVisible = true
                toolbar.menu.findItem(R.id.menu_delete).isVisible = true
            }
            else -> {//不允许删除编辑
                toolbar.menu.findItem(R.id.menu_edit).isVisible = false
                toolbar.menu.findItem(R.id.menu_share).isVisible = false
//                toolbar.menu.findItem(R.id.menu_add_follow).isVisible = false
                toolbar.menu.findItem(R.id.menu_delete).isVisible = false
            }
        }
        toolbar.menu.findItem(R.id.menu_as_global).isVisible = node?.belongInApp() ?: false
        contentView.post {
            //标题
            title = node.actionTitle
            //content
            node.assembly()
            instructions_text.text = node.desc?.instructions ?: "无"
            examples_text.text = node.desc?.example ?: "无"
            regs_text.text = TextHelper.arr2String(node.regs?.toTypedArray()
                ?: arrayOf<Any>(), "\n")
            version_text.text = node.versionCode.toString()
            script_text.text = node.action?.actionScript
            script_type_text.text = node.action?.scriptType
        }

    }

    private fun initView() {
        toolbar.setOnMenuItemClickListener { it ->
            when (it.itemId) {
                R.id.menu_edit -> {//修改
                    if (!AppConfig.checkUser()) {
                        return@setOnMenuItemClickListener true
                    }
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
                    MaterialDialog(context!!).title(text = getString(R.string.text_confirm_2_del) + ": ${node.actionTitle} ?")
                            .negativeButton(R.string.text_help) {
                                SystemBridge().openUrl(ApiUrls.HELP_DEL_INST)// TODO
                            }
                            .message(R.string.text_msg_delete_action_node)
                            .positiveButton(R.string.text_confirm) {
                                val p = ProgressDialog(context!!)
                                if (node.tagId != null) {
                                    NetHelper.postJson<Any>(ApiUrls.DELETE_SHARE_INST,
                                            BaseRequestModel(node.tagId)) { _, bean ->
                                        if (bean?.isOk() == true) {
                                            GlobalLog.log("云端删除成功$node")
                                        } else {
                                            GlobalLog.log("云端删除失败$node ${bean?.message}")
                                        }
                                        del()
                                        p.dismiss()
                                    }
                                } else {
                                    GlobalLog.log("未分享至server$node")
                                    del()
                                    p.dismiss()
                                }
                                onUpdate.invoke()
                                hide()
                            }
                            .negativeButton(R.string.text_cancel)
                            .show()
                }
                R.id.menu_add_follow -> {// 选择: 从已有(inApp Global)关联parentId，或者新建

                    MaterialDialog(context!!).listItems(items = listOf(
                            getString(R.string.text_new),
                            getString(R.string.text_sel_from_existing)
                    ), waitForPositiveButton = false) { _, i, s ->
                        when (i) {
                            0 -> addFollowFromNew()
                            1 -> toast.showShort(R.string.text_coming_soon)//todo
                        }
                    }.show()
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
                    //todo one month need vip
//                    val sp = SpHelper(context!!)
//                    val prompt = sp.getBoolean(R.string.key_show_prompt_inapp_as_global_dialog)
//                    if (prompt) {
                    //重要信息
                    MaterialDialog(context!!).title(text = "设为全局命令")
                            .message(text = "此操作会复制此命令(包括跟随操作)至全局，若不选择复制跟随操作，" +
                                    "选择下方[不包含]即可。\n注意：跟随操作不允许此操作\n跟随操作最多之复制一层")
                            /*.checkBoxPrompt(text = "不再提醒") {
                                sp.set(R.string.key_show_prompt_inapp_as_global_dialog, it)
                            }*/.positiveButton(R.string.text_continue) {
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
    }

    fun del() {
        thread {
            val b = DaoHelper.deleteActionNodeInTX(node?.id)
            GlobalApp.toastShort(
                    if (b) "删除成功" else "删除失败，可至帮助进行反馈"
            )
        }
    }

    private fun addFollowFromNew() {
        val node = node!!
        if (!AppConfig.checkUser()) {
            return
        }
        val editIntent = Intent(context, NewInstActivity::class.java)
        val type = when {
            ActionNode.belongGlobal(node.actionScopeType) -> ActionNode.NODE_SCOPE_GLOBAL
            ActionNode.belongInApp(node.actionScopeType) -> ActionNode.NODE_SCOPE_IN_APP
            else -> {
                GlobalLog.log("maybe error")
                GlobalApp.toastShort("maybe error")
                return
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

    var shareDialog: MaterialDialog? = null

    lateinit var p: ProgressDialog
    private fun doCopy2Global(containSub: Boolean) {
        if (node.parentId != null) {
            toast.showLong("跟随操作不允许此操作")
            return
        }
        p = ProgressDialog(context!!)
        thread {
            val cloneNode: ActionNode?
            try {
                cloneNode = node.cloneGlobal(containSub)
                val s = DaoHelper.copyInApp2Global(cloneNode)
                toast.showLong(s)

            } catch (e: Exception) {
                GlobalLog.err("${e.message} code:id230")
                toast.showLong("复制失败")
            }
            p.dismiss()
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
                        node.versionCode = s
                        Vog.d(this, "new ver---> $s")
                        node.update()
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

}