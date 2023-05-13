package cn.vove7.jarvis.activities

import android.app.Activity
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import cn.vove7.bottomdialog.BottomDialog
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.app.GlobalLog
import cn.vove7.common.bridges.SystemBridge
import cn.vove7.common.datamanager.DAO
import cn.vove7.common.datamanager.DaoHelper
import cn.vove7.common.datamanager.greendao.ActionNodeDao
import cn.vove7.common.datamanager.parse.DataFrom
import cn.vove7.common.datamanager.parse.model.Action
import cn.vove7.common.datamanager.parse.model.ActionScope
import cn.vove7.common.datamanager.parse.statusmap.ActionNode
import cn.vove7.common.datamanager.parse.statusmap.ActionNode.NODE_SCOPE_GLOBAL
import cn.vove7.common.datamanager.parse.statusmap.ActionNode.NODE_SCOPE_IN_APP
import cn.vove7.common.datamanager.parse.statusmap.Reg
import cn.vove7.common.helper.AdvanAppHelper
import cn.vove7.common.model.UserInfo
import cn.vove7.common.net.ApiUrls
import cn.vove7.common.utils.get
import cn.vove7.common.utils.gone
import cn.vove7.common.utils.show
import cn.vove7.executorengine.model.ActionParseResult
import cn.vove7.executorengine.parse.ParseEngine
import cn.vove7.jarvis.BuildConfig
import cn.vove7.jarvis.R
import cn.vove7.jarvis.activities.base.ReturnableActivity
import cn.vove7.jarvis.databinding.ActivityNewInstBinding
import cn.vove7.jarvis.tools.UriUtils.getPathFromUri
import cn.vove7.jarvis.view.dialog.SelectAppDialog
import cn.vove7.vtp.easyadapter.BaseListAdapter
import cn.vove7.vtp.log.Vog
import cn.vove7.vtp.view.span.ColourTextClickableSpan
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.input.input
import thereisnospon.codeview.CodeViewTheme
import java.io.File
import java.util.*


/**
 * # NewInstActivity
 * login & vip -> in
 * @author Vove
 * 2018/8/19
 */
class NewInstActivity : ReturnableActivity<ActivityNewInstBinding>(), View.OnClickListener {
    var pkg: String? = null

    private var instType: Int = NODE_SCOPE_GLOBAL

    private val editNode: ActionNode by lazy {
        val id = intent.getLongExtra("nodeId", 0L)
        DAO.daoSession.actionNodeDao.queryBuilder().where(ActionNodeDao.Properties.Id.eq(id)).unique()
    }//修改

    private val isReedit by lazy { intent["reedit", false] }

    private var enterTime = 0L

    private lateinit var scriptTextView: EditText
    private var scriptText: String? = null
        set(value) {
            field = value
            viewBinding.codeView.showCode(value ?: "")
        }

    private var scriptType: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        enterTime = System.currentTimeMillis()
        super.onCreate(savedInstanceState)

        if (!UserInfo.isLogin()) {
            finish()
            return
        }

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        initData()

        viewBinding.fab.setOnClickListener(this)
        viewBinding.btnSetScript.setOnClickListener(this)
        viewBinding.addRegex.setOnClickListener(this)
        viewBinding.testRegex.setOnClickListener(this)
        regAdapter = RegListAdapter(this, regs)
        viewBinding.regexStrList.adapter = regAdapter
        if (isDarkTheme) {
            viewBinding.codeView.setBackgroundColor(0)
            viewBinding.codeView.setTheme(CodeViewTheme.DARK)
        }
    }


    private fun initData() {
        val arr = resources!!.getStringArray(R.array.list_pos_of_regex_param)
        posData = arrayListOf()
        posData.addAll(arr)
        if (isReedit) {//重新编辑
            try {//测试 初始化结果
                editNode
            } catch (e: Throwable) {
                GlobalApp.toastError(getString(R.string.text_error_occurred) + " code :n117")
                finish()
                return
            }
            editNode.apply {

                scriptType = action.scriptType
                scriptText = action.actionScript
                viewBinding.activityName.setText(actionScope?.activity)
                viewBinding.descText.setText(actionTitle)
                regsWithoutCache?.forEach {
                    this@NewInstActivity.regs.add(it.regStr)
                }
                viewBinding.checkBoxAutoLaunchApp.isChecked = autoLaunchApp
            }
        } else {// 来自RemoteDebugServer
            if (intent.hasExtra("remote_script")) {
                scriptText = intent.getStringExtra("remote_script")
                scriptType = intent.getStringExtra("remote_script_type")

                Vog.d("initData ---> $scriptType\n$scriptText")
            }
        }
        instType = intent.getIntExtra("type", NODE_SCOPE_GLOBAL)
        when (instType) {
            NODE_SCOPE_GLOBAL -> {//隐藏sel App
                viewBinding.selApp.gone()
            }
            NODE_SCOPE_IN_APP -> {
                viewBinding.btnSelApp.setOnClickListener(this)
                pkg = intent.getStringExtra("pkg")
                if (pkg != null) {
                    val app = AdvanAppHelper.getAppInfo(pkg!!)
                    if (app != null) {
                        viewBinding.btnSelApp.text = app.name
                    }
                }
            }
            0 -> {//新建
                viewBinding.selApp.gone()
                if (!isReedit) {//add follow
                    viewBinding.parentLay.show()
                    viewBinding.parentTitle.text = intent.getStringExtra("parent_title")
                }
            }
            else -> {
                GlobalLog.err("新建类型错误")
                finish()
            }
        }
    }

    override fun onBackPressed() {
        val now = System.currentTimeMillis()
        if (now - enterTime < 5000) {
            super.onBackPressed()
            return
        }
        MaterialDialog(this)
                .title(R.string.text_confirm_to_exit)
                .message(R.string.text_content_wont_be_save)
                .positiveButton {
                    super.onBackPressed()
                }.negativeButton()
                .show()
        //TODO 草稿
    }

    private fun save() {
        val inApp = ActionNode.belongInApp(instType)

        val desc = viewBinding.descText.text.toString().trim()
        if (desc == "") {//descTitle
            viewBinding.descInputLay.error = getString(R.string.text_not_empty)
            return
        }
        if ((scriptText ?: "") == "") {//script
            GlobalApp.toastWarning(getString(R.string.text_tooltips_select_script))
            return
        }
        if ((scriptType ?: "") == "") {//scriptType
            GlobalApp.toastWarning(getString(R.string.text_tooltips_select_script_type))
            return
        }
        if (ActionNode.belongInApp(instType) && pkg == null) {//type
            GlobalApp.toastWarning(getString(R.string.text_tooltips_select_app))
            return
        }
        if (regs.isEmpty()) {//regs
            GlobalApp.toastWarning(getString(R.string.text_at_last_one_regex))
        }

        var activityName: String? = null
        viewBinding.activityName.text.toString().trim().let {
            activityName = if (it == "") {
                null
            } else it
        }
        if (isReedit) {//保存编辑
            if (inApp) {//更新scope
                editNode.setScopeId(DaoHelper.getActionScopeId(ActionScope(pkg, activityName)))
                editNode.autoLaunchApp = viewBinding.checkBoxAutoLaunchApp.isChecked
            }
            val ac = editNode.action!!
            ac.scriptType = scriptType
            ac.actionScript = scriptText
            DAO.daoSession.actionDao.update(ac)
            editNode.actionTitle = desc
            editNode.regs.forEach { DAO.daoSession.regDao.delete(it) }
            wrapRegs().forEach {
                it.nodeId = editNode.id
                DAO.daoSession.regDao.insert(it)
            }
            DAO.daoSession.actionNodeDao.update(editNode)
            ParseEngine.updateNode()
            GlobalApp.toastSuccess(getString(R.string.text_save_success))
            DAO.clear()
            setResult(Activity.RESULT_OK, Intent().apply {
                putExtra("action", "update")
                putExtra("id", editNode.id)
            })
            finish()

        } else {//新发布 构造newNodeIns
            val newNode = ActionNode()
            if (inApp) {
                val scope = ActionScope(pkg, activityName)
                newNode.actionScope = scope
                newNode.autoLaunchApp = viewBinding.checkBoxAutoLaunchApp.isChecked
            }

            val action = Action(scriptText, scriptType)
            newNode.action = action
            newNode.actionTitle = desc

            newNode.from = DataFrom.FROM_USER
            if (UserInfo.isLogin() || !BuildConfig.DEBUG) {
                newNode.publishUserId = UserInfo.getUserId()
            }
            newNode.actionScopeType = instType

            newNode.regs = wrapRegs()

            if (DaoHelper.insertNewActionNode(newNode) != null) {
                GlobalApp.toastSuccess(getString(R.string.text_save_success))
                ParseEngine.updateNode()
                DAO.clear()

                setResult(Activity.RESULT_OK, Intent().apply {
                    putExtra("action", "insert")
                    putExtra("id", newNode.id)
                })
                finish()
            } else {
                GlobalApp.toastError(getString(R.string.text_save_failed))
            }
        }
    }

    private val selAppDialog: BottomDialog by lazy {
        SelectAppDialog.get(this) {
            viewBinding.btnSelApp.text = it.name
            pkg = it.packageName
        }
    }

    override fun onClick(v: View?) {
        when (v) {
            viewBinding.btnSelApp -> {
                selAppDialog.show()
            }
            viewBinding.fab -> {
                //check save insert
                try {
                    save()
                } catch (e: Exception) {
                    GlobalLog.err(e)
                    GlobalApp.toastError(getString(R.string.text_error_occurred) + e.message)
                }
            }
            viewBinding.addRegex -> {
                showInputRegDialog()
            }
            viewBinding.btnSetScript -> {
                showSelScriptDialog()
            }
            viewBinding.testRegex -> {
                //模拟
                if (regs.isEmpty()) {
                    GlobalApp.toastError(getString(R.string.text_enter_at_last_one_regex))
                    return
                }
                showTestParseDialog()
            }
        }
    }

    private var selScriptDialog: MaterialDialog? = null

    private fun showSelScriptDialog() {
        if (selScriptDialog == null) {
            val dView = layoutInflater.inflate(R.layout.dialog_sel_script, null)
            scriptTextView = dView.findViewById(R.id.script_text)
            val typeArr = resources!!.getStringArray(R.array.list_script_type)
            dView.findViewById<Spinner>(R.id.script_type_spinner).also {
                it.setSelection(
                        when (scriptType) {
                            Action.SCRIPT_TYPE_LUA -> 0
                            Action.SCRIPT_TYPE_JS -> 1
                            else -> 0
                        }
                )
            }.onItemSelectedListener = (object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {
                    scriptType = null
                }

                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    scriptType = typeArr[position]
                }
            })
            if (isReedit) {
                scriptText = editNode.action?.actionScript
                scriptType = editNode.action.scriptType
            }
            selScriptDialog = MaterialDialog(this)
                    .title(text = "脚本代码")
                    .noAutoDismiss()
                    .customView(view = dView, scrollable = true)
                    .positiveButton(R.string.text_confirm) { d ->
                        scriptText = scriptTextView.text.toString()
                        d.dismiss()
                    }
                    .negativeButton { it.dismiss() }
                    .neutralButton(R.string.text_from_file) {
                        val selIntent = Intent(Intent.ACTION_GET_CONTENT)
                        selIntent.type = "*/*"
                        selIntent.addCategory(Intent.CATEGORY_OPENABLE)
                        try {
                            startActivityForResult(selIntent, 1)
                        } catch (e: ActivityNotFoundException) {
                            e.printStackTrace()
                            GlobalApp.toastError(R.string.text_cannot_open_file_manager)
                        }
                    }

        }
        scriptTextView.setText(scriptText)
        selScriptDialog?.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {//选择文件回调
            when (requestCode) {
                1 -> {
                    val uri = data?.data
                    if (uri != null) {
                        try {
                            val path = getPathFromUri(this, uri)
                            Vog.d("onActivityResult path: $path")
                            scriptTextView.setText(File(path).readText())
                        } catch (e: Exception) {
                            e.printStackTrace()
                            GlobalApp.toastError(getString(R.string.text_open_failed))
                        }
                    } else {
                        GlobalApp.toastError(getString(R.string.text_open_failed))
                    }
                }
                else -> {
                }
            }
        }
    }

    private var testParseDialog: AlertDialog? = null
    private lateinit var historyTestListView: ListView
    private lateinit var testInputView: EditText
    private lateinit var parseButton: Button

    private lateinit var historyListAdapter: ArrayAdapter<String>
    private lateinit var resultOutput: TextView
    private val hisList = mutableListOf<String>()
    /**
     * Reex测试Dialog
     */
    private fun showTestParseDialog() {
        if (testParseDialog == null) {
            val dView = layoutInflater.inflate(R.layout.dialog_regex_parse_test, null, false)
            testParseDialog = AlertDialog.Builder(this)
                    .setView(dView)
                    .create()
            historyTestListView = dView.findViewById(R.id.test_history_list)
            resultOutput = dView.findViewById(R.id.result_output)
            historyListAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, hisList)
            historyTestListView.adapter = historyListAdapter
            testInputView = dView.findViewById(R.id.test_text)
            testInputView.setOnKeyListener { _, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER) {
                    Vog.d("showTestParseDialog click")
                    if (event.action == KeyEvent.ACTION_UP)
                        parseButton.performClick()
                    return@setOnKeyListener true
                }
                return@setOnKeyListener false
            }

            parseButton = dView.findViewById(R.id.btn_parse)
            parseButton.setOnClickListener {
                val testText = testInputView.text.toString()
                if (testText == "") {
                    GlobalApp.toastWarning(getString(R.string.test_tooltips_of_input_test_text))
                    return@setOnClickListener
                }
                //  联合parentNode
                val result = ParseEngine.testParse(testText, wrapTestNode())
                outputParseResult(result)

                if (!hisList.contains(testText)) {
                    hisList.add(0, testText)
                    historyListAdapter.notifyDataSetChanged()
                }
            }
            historyTestListView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                testInputView.setText(hisList[position])
            }
        }
        testParseDialog?.show()
    }

    private fun outputParseResult(result: ActionParseResult) {
        resultOutput.text = ""
        if (result.isSuccess) {
            val actions = result.actionQueue
            while (actions?.isNotEmpty() == true) {
                val p = actions.poll()
                val args = p.param ?: emptyMap()
                val t = String.format(getString(R.string.text_parse_result_placeholder),
                        p.matchWord, args.toString()) // "匹配词: ${p.matchWord} 参数: ${p.param}\n")
                val text = ColourTextClickableSpan(this, t, android.R.color.white, listener = null)
//                Vog.d("outputParseResult $t")
                resultOutput.append(text.spanStr)
                resultOutput.append("\n")
            }
            val succ = ColourTextClickableSpan(this, getString(R.string.text_test_passed), R.color.light_green_500, listener = null)
            resultOutput.append(succ.spanStr)
        } else {
            val err = ColourTextClickableSpan(this, getString(R.string.text_test_failed), R.color.red_500, listener = null)
            resultOutput.append(err.spanStr)
        }
    }

    private lateinit var regAdapter: RegListAdapter

    //无
    //第一个
    //第二个
    //第三个
    //最后一个

    private val regs = mutableListOf<String>()
    /**
     * 转换测试MapNode List
     * @return regs:List<Pair<String,String>> -> List<ActionNode>
     */
    private fun wrapTestNode(): ActionNode {
        val testId = Int.MAX_VALUE.toLong()

        val testNode = ActionNode(viewBinding.descText.text.toString(), testId, -1L, instType)
        testNode.regs = wrapRegs()
        return testNode
    }

    private fun wrapRegs(): List<Reg> {
        val tregs = mutableListOf<Reg>()
        regs.forEach {
            //            val p = posArr[posData.indexOf(it.second)]
            tregs.add(Reg(it))
        }
        return tregs
    }

    var editIndex = -1
    /**
     * 添加到正则式列表
     * 判断editIndex 是否为编辑
     * @param reg String
     * @param posArr Array<Int>
     */
    private fun add2RegexList(reg: String) {
        Vog.d("add2RegexList $reg")
        if (editIndex > -1) {
            try {
                regs[editIndex] = reg
            } catch (e: Exception) {//index out
                GlobalLog.err(e)
            }
            editIndex = -1
        } else {
            regs.add(reg)
        }
        regAdapter.notifyDataSetChanged()
    }


    lateinit var posData: ArrayList<String>

    private fun showInputRegDialog(reg: String? = null) {

        MaterialDialog(this).show {
            input(hint = "正则式", prefill = reg) { _, t ->
                add2RegexList(t.toString())
            }
            positiveButton(text = "确认")
            negativeButton(text = "取消")
            neutralButton(text = "帮助") { SystemBridge.openUrl(ApiUrls.INST_REGEX_GUIDE) }
        }
    }

    inner class RegListAdapter(context: Context, val dataSet: MutableList<String>?)
        : BaseListAdapter<Holder, String>(context, dataSet) {

        override fun onCreateViewHolder(view: View): Holder {
            return Holder(view)
        }

        override fun layoutId(position: Int): Int = R.layout.item_left_right_text
        override fun onBindView(holder: Holder, pos: Int, item: String) {
            holder.left.text = item
//            holder.right.text = Arrays.toString(item.second)
            holder.itemView.setOnClickListener {
                editIndex = pos
                showInputRegDialog(item)
            }
            holder.itemView.setOnLongClickListener {
                //长按删除
                dataSet?.removeAt(pos)
                notifyDataSetChanged()
                return@setOnLongClickListener true
            }
        }
    }

    class Holder(itemView: View) : BaseListAdapter.ViewHolder(itemView) {
        val left = itemView.findViewById<TextView>(R.id.left_text)
//        val right = itemView.findViewById<TextView>(R.id.right_text)
    }

}
