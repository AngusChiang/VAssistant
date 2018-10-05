package cn.vove7.jarvis.activities

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.app.GlobalLog
import cn.vove7.common.appbus.AppBus
import cn.vove7.common.datamanager.DAO
import cn.vove7.common.datamanager.DaoHelper
import cn.vove7.common.datamanager.greendao.ActionNodeDao
import cn.vove7.common.datamanager.parse.DataFrom
import cn.vove7.common.datamanager.parse.model.Action
import cn.vove7.common.datamanager.parse.model.ActionDesc
import cn.vove7.common.datamanager.parse.statusmap.ActionNode
import cn.vove7.common.model.UserInfo
import cn.vove7.common.netacc.ApiUrls
import cn.vove7.common.netacc.model.BaseRequestModel
import cn.vove7.common.netacc.model.ResponseMessage
import cn.vove7.jarvis.utils.NetHelper
import cn.vove7.common.utils.RegUtils
import cn.vove7.common.utils.TextHelper
import cn.vove7.common.view.toast.ColorfulToast
import cn.vove7.executorengine.exector.MultiExecutorEngine
import cn.vove7.jarvis.R
import cn.vove7.jarvis.adapters.ExecuteQueueAdapter
import cn.vove7.jarvis.adapters.InstSettingListAdapter
import cn.vove7.jarvis.utils.AppConfig
import cn.vove7.jarvis.view.dialog.ProgressDialog
import cn.vove7.vtp.dialog.DialogWithList
import cn.vove7.vtp.easyadapter.BaseListAdapter
import cn.vove7.vtp.log.Vog
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.list.listItems
import com.google.gson.reflect.TypeToken
import io.github.kbiakov.codeview.adapters.Options
import kotlinx.android.synthetic.main.activity_inst_detail.*
import java.util.*
import kotlin.concurrent.thread

/**
 * # InstDetailActivity
 *
 * @author Administrator
 * 2018/10/3
 */
class InstDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inst_detail)
        toolbar.inflateMenu(R.menu.menu_inst_detail)
        initView()
        nodeId = intent.getLongExtra("nodeId", -1)
        if (nodeId == -1L) finish()
        val n = DAO.daoSession.actionNodeDao.queryBuilder()
                .where(ActionNodeDao.Properties.Id.eq(nodeId))
                .unique()
        if (n == null) {
            toast.showShort("不存在")
            finish()
            return
        }
        node = n
        setData()
        initFollows()
        toolbar.setNavigationOnClickListener { finish() }
        load = true
    }

    var title: String? = null
        set(value) {
            collapsing_coll.title = value
            field = value
        }

    var nodeId: Long = -1
    lateinit var node: ActionNode
    val toast: ColorfulToast by lazy { ColorfulToast(this) }
    private var load = false
//    private val instructions_text: TextView by lazy { findViewById<TextView>(R.id.instructions_text) }
//    private val examples_text: TextView by lazy { findViewById<TextView>(R.id.examples_text) }
//    private val regs_text: TextView by lazy { findViewById<TextView>(R.id.regs_text) }
//    private val version_text: TextView by lazy { findViewById<TextView>(R.id.version_text) }
//    private val script_text: CodeView by lazy { findViewById<CodeView>(R.id.script_text) }
//    private val script_type_text: TextView by lazy { findViewById<TextView>(R.id.script_type_text) }


    //todo  share user info
    private fun setData() {
        when {//编辑
            node.belongSelf() -> {
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
        toolbar.menu.findItem(R.id.menu_as_global).isVisible = node.belongInApp()
        Handler().post {
            //标题
            title = node.actionTitle
            //content
            node.assembly()
            instructions_text.text = node.desc?.instructions ?: "无"
            examples_text.text = node.desc?.example ?: "无"
            regs_text.text = TextHelper.arr2String(node.regs?.toTypedArray()
                ?: arrayOf<Any>(), "\n")
            version_text.text = node.versionCode.toString()
//            script_text.setCode(node.action?.actionScript ?: "", )
            script_text.setOptions(Options.get(this)
                    .withLanguage(node.action?.scriptType ?: "lua")
                    .withCode(node.action?.actionScript ?: "").withoutShadows())
            script_type_text.text = node.action?.scriptType
        }
        if (node.action != null) {
            val settingsHeader = RegUtils.getRegisterSettingsTextAndName(node.action.actionScript)
            if (settingsHeader != null) {
                val script = settingsHeader.script
                val version = settingsHeader.version
                settingName = settingsHeader.name
                val instSetting = DaoHelper.getInsetSettingsByName(settingName!!)
                if (instSetting == null || instSetting.version < version) {//数据库不存在数据
                    Vog.d(this, "setData ---> 执行新建or升级")
//                  //执行
                    val engine = MultiExecutorEngine(this, null)
                    when (node.action.scriptType) {
                        Action.SCRIPT_TYPE_LUA -> {
                            engine.onLuaExec(script)
                        }
                        Action.SCRIPT_TYPE_JS -> {
                            engine.onRhinoExec(script)
                        }
                    }
                }
                toolbar.menu.findItem(R.id.menu_settings).isVisible = true
            } else {
                toolbar.menu.findItem(R.id.menu_settings).isVisible = false
            }
        }
    }

    fun initFollows() {
        if (node.follows != null) {
            follow_list.adapter = object : BaseListAdapter<VHolder, ActionNode>(this, node.follows) {
                override fun layoutId(): Int = R.layout.item_normal_icon_title

                override fun onBindView(holder: VHolder, pos: Int, item: ActionNode) {
                    holder.title.text = item.actionTitle
                    holder.icon.visibility = View.GONE
                    holder.subtitle.visibility = View.VISIBLE
                    holder.subtitle.apply {
                        if (item.desc != null) {
                            text = item.desc.instructions
                            if (item.follows.isNotEmpty()) {
                                append("\n跟随 ${item.follows.size}")
                            }
                        } else
                            text = "无介绍"
                    }
                    holder.itemView.setOnClickListener {
                        val intent = Intent(this@InstDetailActivity, InstDetailActivity::class.java)
                        intent.putExtra("nodeId", item.id)
                        startActivity(intent)
                    }

                }

                override fun onCreateViewHolder(view: View): VHolder = VHolder(view)
            }

        }
    }

    class VHolder(v: View) : BaseListAdapter.ViewHolder(v) {

        var icon = v.findViewById<ImageView>(R.id.icon)
        var title = v.findViewById<TextView>(R.id.title)
        var subtitle = v.findViewById<TextView>(R.id.sub_title)

    }

    var settingName: String? = null
    private fun initView() {
        toolbar.setOnMenuItemClickListener { it ->
            when (it.itemId) {
                R.id.menu_edit -> {//修改
                    if (!AppConfig.checkUser()) {
                        return@setOnMenuItemClickListener true
                    }
                    val editIntent = Intent(this, NewInstActivity::class.java)
                    editIntent.putExtra("nodeId", node.id)
                    editIntent.putExtra("type", node.actionScopeType)
                    val scope = node.actionScope
                    if (scope != null)
                        editIntent.putExtra("pkg", node.actionScope.packageName)
                    editIntent.putExtra("reedit", true)
                    startActivity(editIntent)
                }
                R.id.menu_delete -> {//删除
                    MaterialDialog(this).title(text = getString(R.string.text_confirm_2_del) + ": ${node.actionTitle} ?")
//                            .negativeButton(R.string.text_help) {
//                                SystemBridge.openUrl(ApiUrls.HELP_DEL_INST)// TODO
//                            }
                            .message(R.string.text_msg_delete_action_node)
                            .positiveButton(R.string.text_confirm) {
                                val p = ProgressDialog(this)
                                if (node.tagId != null) {
                                    NetHelper.postJson<Any>(ApiUrls.DELETE_SHARE_INST,
                                            BaseRequestModel(node.tagId)) { _, bean ->
                                        if (bean?.isOk() == true) {
                                            GlobalLog.log("云端删除成功$node")
                                        } else {
                                            GlobalLog.log("云端删除失败$node ${bean?.message}")
                                        }
                                        delLocalNode()
                                        p.dismiss()
                                    }
                                } else {
                                    GlobalLog.log("未分享至server$node")
                                    delLocalNode()
                                    DAO.clear()
                                    p.dismiss()
                                }
                                AppBus.post("del_inst")
                                finish()
                            }
                            .negativeButton(R.string.text_cancel)
                            .show()
                }
                R.id.menu_add_follow -> {// 选择: 从已有(inApp Global)关联parentId，或者新建

                    MaterialDialog(this).listItems(items = listOf(
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
                    } else if (node.parentId != null && node.parent.tagId == null) {//检查parent
                        toast.red().showShort("请先上传上级操作")
                    } else
                        showShareDialog()
                }
                R.id.menu_as_global -> {//copy as global
                    //todo one month need vip
//                    val sp = SpHelper(this)
//                    val prompt = sp.getBoolean(R.string.key_show_prompt_inapp_as_global_dialog)
//                    if (prompt) {
                    //重要信息
                    MaterialDialog(this).title(text = "设为全局指令")
                            .message(text = "此操作会复制此指令(包括跟随操作)至全局，若不选择复制跟随操作，" +
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
                R.id.menu_settings -> {//显示设置
                    var d: DialogWithList? = null
                    d = DialogWithList(this, InstSettingListAdapter(this,
                            settingName ?: "") {
                        toast.showLong("设置加载失败")
                        d?.dismiss()
                    })
                    d.setTitle(title ?: "")
                    d.setWidth(0.9)
                    d.show()
                }
            }
            return@setOnMenuItemClickListener true
        }
    }

    private fun delLocalNode() {
        thread {
            val b = DaoHelper.deleteActionNodeInTX(node?.id)
            GlobalApp.toastShort(
                    if (b) "删除成功" else "删除失败，可至帮助进行反馈"
            )
        }
    }

    private fun addFollowFromNew() {
        if (!AppConfig.checkUser()) {
            return
        }
        val editIntent = Intent(this, NewInstActivity::class.java)
        val type = 0 /*when {
            ActionNode.belongGlobal(node.actionScopeType) -> ActionNode.NODE_SCOPE_GLOBAL
            ActionNode.belongInApp(node.actionScopeType) -> ActionNode.NODE_SCOPE_IN_APP
            else -> {
                GlobalLog.log("maybe error")
                GlobalApp.toastShort("maybe error")
                return
            }
        }*/
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
        p = ProgressDialog(this)
        thread {
            val cloneNode: ActionNode?
            try {
                cloneNode = node.cloneGlobal(containSub)
                val s = DaoHelper.insertNewActionNodeInTx(cloneNode)
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
        //判断首次分享
        val upgrade = node.tagId != null
        if (node.parentId != null)
            node.parentTagId = node.parent.tagId
        //dialog 说明|示例
        val dView = layoutInflater.inflate(R.layout.dialog_share_inst, null)
        var desc = node.desc
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
            shareDialog = MaterialDialog(this)
                    .title(sId)
                    .customView(view = dView)
                    .positiveButton(sId) {
                        //check info
                        val descStr = descText.text.toString()
                        val exStr = exText.text.toString()
                        if (descStr.trim() == "") {
                            GlobalApp.toastShort("填写说明")
                            return@positiveButton
                        }
                        //set desc
                        desc = ActionDesc(descStr, exStr)
                        DAO.daoSession.actionDescDao.insert(desc)
                        node.descId = desc.id
                        //set userId
                        node.publishUserId = UserInfo.getUserId()
                        //update
                        node.update()
                        node.desc = desc
                        //assembly
                        node.assembly()
                        //share
                        if (upgrade) upgrade()
                        else firstShare()
                    }.negativeButton(R.string.text_cancel)
        }
        shareDialog!!.show()
    }

    /**
     * 更新返回版本号
     */
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

    /**
     * 首次分享返回tag
     */
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
                        node.from = DataFrom.FROM_SHARED
                        node.tagId = s
                        node.update()
//                        node.follows.forEach { child ->
////                            child.parentTagId = s
//                            child.update()
//                        }
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

        // .. -> it.parent -> it
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
                    Action("openAppByPkg('$pkg',true)\n" +
                            "waitForApp('$pkg')", Action.SCRIPT_TYPE_LUA)))

        }
        val d = DialogWithList(this, ExecuteQueueAdapter(this, execQueue))
                .setButton(DialogInterface.BUTTON_POSITIVE, R.string.text_run, View.OnClickListener { v ->
                    val que = PriorityQueue<Action>()
                    execQueue.forEach {
                        que.add(it.action)
                    }
                    AppBus.post(que)
                })
                .setButton(DialogInterface.BUTTON_NEGATIVE, R.string.text_cancel, View.OnClickListener { v -> })
        d.setWidth(0.9)
        d.setTitle("执行队列")
        d.show()
    }
}