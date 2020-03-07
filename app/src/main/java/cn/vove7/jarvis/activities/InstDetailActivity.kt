package cn.vove7.jarvis.activities

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.text.InputType
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import cn.vove7.common.app.AppConfig
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.app.GlobalLog
import cn.vove7.common.app.log
import cn.vove7.common.appbus.AppBus
import cn.vove7.common.bridges.SystemBridge
import cn.vove7.common.datamanager.DAO
import cn.vove7.common.datamanager.DaoHelper
import cn.vove7.common.datamanager.greendao.ActionNodeDao
import cn.vove7.common.datamanager.parse.DataFrom
import cn.vove7.common.datamanager.parse.model.Action
import cn.vove7.common.datamanager.parse.model.ActionDesc
import cn.vove7.common.datamanager.parse.statusmap.ActionNode
import cn.vove7.common.helper.AdvanAppHelper
import cn.vove7.common.model.UserInfo
import cn.vove7.common.net.ApiUrls
import cn.vove7.common.net.WrapperNetHelper
import cn.vove7.common.utils.RegUtils
import cn.vove7.common.utils.TextHelper
import cn.vove7.executorengine.parse.ParseEngine
import cn.vove7.jarvis.R
import cn.vove7.jarvis.activities.base.BaseActivity
import cn.vove7.jarvis.adapters.ExecuteQueueAdapter
import cn.vove7.jarvis.adapters.InstSettingListAdapter
import cn.vove7.jarvis.services.MainService
import cn.vove7.jarvis.tools.ItemWrap
import cn.vove7.jarvis.tools.ShortcutUtil
import cn.vove7.jarvis.tools.Tutorials
import cn.vove7.jarvis.view.dialog.ProgressDialog
import cn.vove7.vtp.app.AppInfo
import cn.vove7.vtp.dialog.DialogWithList
import cn.vove7.vtp.easyadapter.BaseListAdapter
import cn.vove7.vtp.extend.gone
import cn.vove7.vtp.extend.show
import cn.vove7.vtp.log.Vog
import cn.vove7.vtp.net.toJson
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.input.input
import kotlinx.android.synthetic.main.activity_inst_detail.*
import java.util.*

/**
 * # InstDetailActivity
 *
 * @author Administrator
 * 2018/10/3
 */
class InstDetailActivity : BaseActivity() {

    companion object {
        fun start(context: Context, nodeId: Long) {
            val intent = Intent(context, InstDetailActivity::class.java)
            intent.putExtra("nodeId", nodeId)
            context.startActivity(intent)
        }
    }

    override val layoutRes: Int
        get() = R.layout.activity_inst_detail

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        toolbar.inflateMenu(R.menu.menu_inst_detail)
        nodeId = intent.getLongExtra("nodeId", -1)
        Vog.d("nodeId: $nodeId")
        if (nodeId == -1L) {
            finish()
            return
        }

        refresh()
        toolbar.setNavigationOnClickListener { finish() }
        load = true
    }

    fun refresh() {
        val n = DAO.daoSession.actionNodeDao.queryBuilder()
                .where(ActionNodeDao.Properties.Id.eq(nodeId))
                .unique()
        if (n == null) {
            GlobalApp.toastError("该指令不存在 $nodeId")
            finish()
            return
        }
        node = n
        setData()
        initFollows()
    }

    var title: String? = null
        set(value) {
            collapsing_coll.title = value
            field = value
        }

    var nodeId: Long = -1
    lateinit var node: ActionNode
    private var load = false
//    private val instructions_text: TextView by lazy { findViewById<TextView>(R.id.instructions_text) }
//    private val examples_text: TextView by lazy { findViewById<TextView>(R.id.examples_text) }
//    private val regs_text: TextView by lazy { findViewById<TextView>(R.id.regs_text) }
//    private val version_text: TextView by lazy { findViewById<TextView>(R.id.version_text) }
//    private val script_text: CodeView by lazy { findViewById<CodeView>(R.id.script_text) }
//    private val script_type_text: TextView by lazy { findViewById<TextView>(R.id.script_type_text) }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        refresh()
    }

    var first = true
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        if (hasFocus && first) {
            startTutorials()
            first = false
        }
    }

    private fun startTutorials() {
        Handler().postDelayed({
            Tutorials.oneStep(this, list = arrayOf(
                    ItemWrap(Tutorials.t_inst_detail_desc, label_instructions, "指令描述", "可以根据描述了解用途")
                    , ItemWrap(Tutorials.t_inst_detail_exp, label_examples, "指令示例", "根据示例，理解指令的“说法”")
                    , ItemWrap(Tutorials.t_inst_detail_regex, label_regex, "指令正则式", "正则式是该指令能匹配到用户说的命令\n基本格式有：\n" +
                    "1. (用|打开|启动|开启)% ----- 可匹配：'用xxx'、'打开xxx'、'启动xxx'...等命令\n" +
                    "2. 大+点声   -------------------  可匹配 大大大点声 ('大'个数大于0)\n" +
                    "3. (播放)?下一[首|曲]  -------- (播放)?代表‘播放’可加可不加，[首|曲]代表可以是曲也可以是首,就可匹配:播放下一曲，下一曲，下一首...等命令\n")
                    , ItemWrap(Tutorials.t_inst_detail_run, (toolbar.getChildAt(1) as ViewGroup).getChildAt(0), "运行"
                    , "这里可以直接运行指令中的脚本")
            ))
        }, 1000)
    }

    //todo  get user info who shared
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

            view_code.setOnClickListener {
                CodeViewActivity.viewCode(this@InstDetailActivity,
                        title ?: "", node.action?.actionScript ?: "",
                        node.action?.scriptType ?: "")
            }
            view_code.setOnLongClickListener {
                SystemBridge.setClipText(node.action?.actionScript ?: "")
                GlobalApp.toastInfo(R.string.text_copied)
                true
            }
            script_type_text.text = node.action?.scriptType
        }
        if (ActionNode.belongInApp(node.actionScopeType) && node.autoLaunchApp) {
            auto_launch_app_flag.show()
        } else {
            auto_launch_app_flag.gone()
        }
        Handler().post {
            if (node.action != null) {
                val settingsHeader = RegUtils.getRegisterSettingsTextAndName(node.action.actionScript)
                if (settingsHeader != null) {
                    val script = settingsHeader.script
                    val version = settingsHeader.version
                    settingName = settingsHeader.name
                    val instSetting = DaoHelper.getInsetSettingsByName(settingName!!)
                    if (instSetting == null || instSetting.version < version) {//数据库不存在数据
                        Vog.d("执行新建or升级")
//                  //执行
                        val createSettingAction = Action(script, null)
                        createSettingAction.scriptType = when (node.action.scriptType) {
                            Action.SCRIPT_TYPE_LUA -> Action.SCRIPT_TYPE_LUA
                            Action.SCRIPT_TYPE_JS -> Action.SCRIPT_TYPE_JS
                            else -> null
                        }
                        MainService.runActionQue("初始化指令设置",
                                PriorityQueue<Action>().also { it.add(createSettingAction) })
//                        AppBus.post(createSettingAction)
                    }
                    toolbar.menu.findItem(R.id.menu_settings).isVisible = true
                } else {
                    toolbar.menu.findItem(R.id.menu_settings).isVisible = false
                }
            }
        }
    }

    private fun initFollows() {
        if (node.follows != null) {
            follow_list.adapter = object : BaseListAdapter<VHolder, ActionNode>(this, node.follows) {
                override fun layoutId(position: Int): Int = R.layout.item_normal_icon_title

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
    override fun initView() {
        toolbar = findViewById(R.id.toolbar)
        toolbar.setOnMenuItemClickListener { it ->
            when (it.itemId) {
                R.id.menu_edit -> {//修改
                    if (!AppConfig.checkLogin()) {
                        return@setOnMenuItemClickListener true
                    }
                    val editIntent = Intent(this, NewInstActivity::class.java)
                    editIntent.putExtra("nodeId", node.id)
                    editIntent.putExtra("type", node.actionScopeType)
                    val scope = node.actionScope
                    if (scope != null)
                        editIntent.putExtra("pkg", node.actionScope.packageName)
                    editIntent.putExtra("reedit", true)
                    startActivityForResult(editIntent, 1)
                }
                R.id.menu_delete -> {//删除
                    MaterialDialog(this).title(text = getString(R.string.text_confirm_2_del) + ": ${node.actionTitle} ?")
//                            .negativeButton(R.string.text_help) {
//                                SystemBridge.openUrl(ApiUrls.HELP_DEL_INST)// TODO
//                            }
                            .message(R.string.text_msg_delete_action_node)
                            .positiveButton(R.string.text_confirm) {
                                val p = ProgressDialog(this)
                                val nodeId = node.id
                                if (node.tagId != null) {
                                    WrapperNetHelper.postJson<Any>(ApiUrls.DELETE_SHARE_INST, node.tagId) {
                                        success { _, bean ->
                                            if (bean.isOk()) {
                                                GlobalLog.log("云端删除成功$node")
                                                delLocalNode()
                                            } else {
                                                GlobalApp.toastError("云端删除失败$node ${bean.message} 请联系开发者")
                                            }
                                            p.dismiss()
                                        }
                                        fail { _, e ->
                                            GlobalLog.log("云端删除失败$node ${e.message}")
                                            p.dismiss()
                                        }
                                    }
                                } else {
                                    GlobalLog.log("未分享至server$node")
                                    delLocalNode()
                                    DAO.clear()
                                    p.dismiss()
                                }
                                AppBus.post("del_inst")
                                setResult(Activity.RESULT_OK, Intent().apply {
                                    putExtra("action", "del")
                                    putExtra("id", nodeId)
                                })
                                finish()
                            }
                            .negativeButton(R.string.text_cancel)
                            .show()
                }
//                R.id.menu_add_follow -> {// 选择: 从已有(inApp Global)关联parentId，或者新建
//
//                    MaterialDialog(this).listItems(items = listOf(
//                            getString(R.string.text_new),
//                            getString(R.string.text_sel_from_existing)
//                    ), waitForPositiveButton = false) { _, i, s ->
//                        when (i) {
//                            0 -> addFollowFromNew()
//                            1 -> GlobalApp.toastInfo(R.string.text_coming_soon)//todo
//                        }
//                    }.show()
//                }
                R.id.menu_run -> {
                    showRunDialog()
                }
                R.id.menu_set_inst_priority -> {
                    MaterialDialog(this).title(R.string.text_set_priority)
                            .noAutoDismiss()
                            .message(text = "值大优先匹配")
                            .input(inputType = InputType.TYPE_NUMBER_FLAG_SIGNED,
                                    prefill = node.priority.toString()) { d, s ->
                                val p = try {
                                    if (s == "") 0
                                    else s.toString().toInt()
                                } catch (e: Exception) {
                                    GlobalApp.toastError("输入整数值")
                                    return@input
                                }
                                node.priority = p
                                node.update()
                                GlobalApp.toastSuccess("设置完成")
                                ParseEngine.updateNode()
                                DAO.clear()
                                d.dismiss()
                            }
                            .positiveButton()
                            .negativeButton { it.dismiss() }.show()
                }
                R.id.menu_share -> {
                    if (UserInfo.isLogin().not()) {
                        GlobalApp.toastInfo("请登录")
                    } else if (node.parentId != null && node.parent.tagId == null) {//检查parent
                        GlobalApp.toastWarning("请先上传上级操作")
                    } else
                        showShareDialog()
                }
                R.id.menu_inst_share -> {
                    if (SystemBridge.setClipText(node.shareData().toJson())) {
                        GlobalApp.toastInfo(R.string.text_copied)
                    }
                }
                R.id.menu_as_global -> {//copy as global
                    //重要信息
                    MaterialDialog(this).show {
                        title(text = "设为全局指令")
                        message(text = "此操作会复制此指令(包括跟随操作)至全局。")
                        positiveButton(R.string.text_continue) {
                            doCopy2Global(false)
                        }
                    }
                }
                R.id.menu_settings -> {//显示设置
//                    if (!AppConfig.checkLogin() && !BuildConfig.DEBUG) {
//                        return@setOnMenuItemClickListener true
//                    }
                    var d: DialogWithList? = null
                    d = DialogWithList(this, InstSettingListAdapter(this,
                            settingName ?: "") {
                        GlobalApp.toastError("设置加载失败")
                        d?.dismiss()
                    })
                    d.setTitle(title ?: "")
                    d.setWidth(0.9)
                    d.show()
                }
                R.id.menu_add_shortcut -> {
                    val ic by lazy {
                        if (node.belongInApp()) {
                            AppInfo(node.actionScope.packageName).icon
                        } else null
                    }
                    MaterialDialog(this).title(R.string.text_add_shortcut_to_launcher)
                            .message(text = "注意：只能添加没有参数的指令，或者在运行时可以询问参数值指令，否则无法正常执行\n" +
                                    "此操作需要7.1+\n8.0+系统可添加至桌面快捷图标")
                            .positiveButton {
                                ShortcutUtil.addActionShortcut(node, dr = ic)
                            }.show {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    neutralButton(text = "同时添加进图标shortcut") {
                                        ShortcutUtil.addActionShortcut(node, true, ic)
                                    }
                                }
                            }
                }
            }
            return@setOnMenuItemClickListener true
        }
    }

    private fun delLocalNode() {
        launch {
            val b = DaoHelper.deleteActionNodeInTX(node.id)
            if (b) {
                GlobalApp.toastSuccess("删除成功")
            } else
                GlobalApp.toastError("删除失败，可至帮助进行反馈")
        }
    }

    private fun addFollowFromNew() {
        if (!AppConfig.checkLogin()) {
            return
        }
        val editIntent = Intent(this, NewInstActivity::class.java)
        val type = 0 /*when {
            ActionNode.belongGlobal(node.actionScopeType) -> ActionNode.NODE_SCOPE_GLOBAL
            ActionNode.belongInApp(node.actionScopeType) -> ActionNode.NODE_SCOPE_IN_APP
            else -> {
                GlobalLog.log("maybe error")
                GlobalApp.toastInfo("maybe error")
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
        startActivityForResult(editIntent, 10)
    }

    var shareDialog: MaterialDialog? = null

    lateinit var p: ProgressDialog
    private fun doCopy2Global(containSub: Boolean) {
        if (node.parentId != null) {
            GlobalApp.toastWarning("跟随操作不允许此操作")
            return
        }
        p = ProgressDialog(this)
        launch {
            val cloneNode: ActionNode?
            try {
                cloneNode = node.cloneGlobal(containSub)
                if (DaoHelper.insertNewActionNodeInTx(cloneNode)) {
                    GlobalApp.toastSuccess(R.string.text_have_done)
                    DAO.clear()
                    ParseEngine.updateGlobal()
                    InstDetailActivity.start(this@InstDetailActivity, cloneNode.id)
                } else {
                    GlobalApp.toastError(R.string.text_an_err_happened)
                }
            } catch (e: Exception) {
                GlobalLog.err(e.message)
                GlobalApp.toastError("复制失败${e.message}")
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
                            GlobalApp.toastWarning("填写说明")
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
        WrapperNetHelper.postJson<Int>(ApiUrls.UPGRADE_INST, node) {
            success { _, bean ->
                if (bean.isOk()) {
                    GlobalApp.toastSuccess(R.string.text_share_success)
                    //sign tag
                    val s = bean.data ?: -1
                    if (s < 0) {
                        return@success
                    }
                    launch {
                        //更新 tagId
                        node.versionCode = s
                        Vog.d("new ver---> $s")
                        node.update()
                    }
                } else {
                    GlobalApp.toastInfo(bean.message)
                }
            }
            fail { _, e ->
                GlobalLog.err(e)
                GlobalApp.toastError(R.string.text_net_err)
            }
        }
    }

    /**
     * 首次分享返回tag
     */
    private fun firstShare() {
        WrapperNetHelper.postJson<String>(ApiUrls.SHARE_INST, node) {
            success { _, bean ->
                if (bean.isOk()) {
                    GlobalApp.toastSuccess(R.string.text_share_success)
                    //sign tag
                    val s = bean.data
                    launch {
                        //更新 tagId
                        Vog.d("share new tag---> $s")
                        node.from = DataFrom.FROM_SHARED
                        node.tagId = s
                        node.update()
//                        node.follows.forEach { child ->
////                            child.parentTagId = s
//                            child.update()
//                        }
                    }
                } else {
                    GlobalApp.toastInfo(bean.message)
                }
            }
            fail { _, e ->
                e.log()
                GlobalApp.toastError(R.string.text_net_err)
            }
        }
    }

    private fun showRunDialog() {

        // .. -> it.parent -> it
        var p: ActionNode? = node
        val execQueue = mutableListOf<ActionNode>()
        while (p != null) {
            p.action.param = null
            execQueue.add(0, p)
            p = p.parent
        }
        if (execQueue[0].autoLaunchApp && ActionNode.belongInApp(execQueue[0].actionScopeType)) {
            val pkg = execQueue[0].actionScope.packageName
            val appName = AdvanAppHelper.getAppInfo(pkg)?.name
            execQueue.add(0, ActionNode("打开App: $appName",
                    Action("system.openAppByPkg('$pkg',true)", Action.SCRIPT_TYPE_LUA)))

        }
        val d = DialogWithList(this, ExecuteQueueAdapter(this, execQueue))
                .setButton(DialogInterface.BUTTON_POSITIVE, R.string.text_run, View.OnClickListener { v ->
                    val que = PriorityQueue<Action>()
                    execQueue.withIndex().forEach {
                        it.value.action.priority = it.index
                        que.add(it.value.action)
                    }
                    //TODO 输入指令
                    MainService.runActionQue("RUN: ${node.actionTitle}", que)
                    //防止以DEBUG方式运行
//                    AppBus.post(que)
                })
                .setButton(DialogInterface.BUTTON_NEGATIVE, R.string.text_cancel, View.OnClickListener { v -> })
        d.setWidth(0.9)
        d.setTitle("执行队列")
        d.show()
    }
}