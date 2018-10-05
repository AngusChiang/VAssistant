package cn.vove7.jarvis.activities

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v4.view.MenuItemCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.SearchView
import android.support.v7.widget.Toolbar
import android.view.KeyEvent
import android.view.MenuItem
import android.view.View
import android.widget.*
import cn.vove7.common.app.GlobalLog
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
import cn.vove7.common.model.UserInfo
import cn.vove7.common.view.toast.ColorfulToast
import cn.vove7.executorengine.helper.AdvanAppHelper
import cn.vove7.jarvis.BuildConfig
import cn.vove7.jarvis.R
import cn.vove7.jarvis.adapters.SimpleListAdapter
import cn.vove7.jarvis.adapters.ViewModel
import cn.vove7.jarvis.utils.UriUtils.getPathFromUri
import cn.vove7.jarvis.view.BottomSheetController
import cn.vove7.parseengine.engine.ParseEngine
import cn.vove7.parseengine.model.ParseResult
import cn.vove7.vtp.app.AppInfo
import cn.vove7.vtp.easyadapter.BaseListAdapter
import cn.vove7.vtp.log.Vog
import cn.vove7.vtp.view.span.ColourTextClickableSpan
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import io.github.kbiakov.codeview.adapters.Options
import kotlinx.android.synthetic.main.activity_new_inst.*
import java.io.File
import kotlin.concurrent.thread


/**
 * # NewInstActivity
 * login & vip -> in
 * @author 17719247306
 * 2018/8/19
 */
class NewInstActivity : AppCompatActivity(), View.OnClickListener {
    var bsController: BottomSheetController? = null
    var pkg: String? = null
    var parentId: Long? = null//上级命令MapNodeId
    private var instType: Int = NODE_SCOPE_GLOBAL

    private var editNode: ActionNode? = null//修改
    private var parentNode: ActionNode? = null
    private var isReedit = false

    private lateinit var searchView: SearchView
    private var enterTime = 0L

    lateinit var toolbar: Toolbar
    override fun onCreate(savedInstanceState: Bundle?) {
        enterTime = System.currentTimeMillis()
        super.onCreate(savedInstanceState)

        if (!UserInfo.isLogin()) {
            finish()
            return
        }

        voast = ColorfulToast(this)
        setContentView(R.layout.activity_new_inst)
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        initData()

        fab.setOnClickListener(this)
        btn_set_script.setOnClickListener(this)
        add_regex.setOnClickListener(this)
        test_regex.setOnClickListener(this)
        regAdapter = RegListAdapter(this, regs)
        regex_str_list.adapter = regAdapter
    }

    private fun setTitle(title: String) {
        this.title = title
    }

    private fun initData() {
        initBottom()
        isReedit = intent.getBooleanExtra("reedit", false)

        val arr = resources.getStringArray(R.array.list_pos_of_regex_param)
        posData = arrayListOf()
        posData.addAll(arr)
        if (isReedit) {//重新编辑
            val id = intent.getLongExtra("nodeId", 0L)
            editNode = DAO.daoSession.actionNodeDao.queryBuilder().where(ActionNodeDao.Properties.Id.eq(id)).unique()
//            editNode?.assembly()
            scriptType = editNode!!.action.scriptType
            scriptText = editNode?.action?.actionScript
            if (editNode == null) {
                voast.showShort(getString(R.string.text_error_occurred) + " code :n117")
            } else {//展示信息
                activity_name.setText(editNode?.actionScope?.activity)
                desc_text.setText(editNode?.actionTitle)
                editNode?.newRegs?.forEach {
                    regs.add(Pair(it.regStr, posData[posArr.indexOf(it.paramPos)]))
                }
            }
        }
        instType = intent.getIntExtra("type", NODE_SCOPE_GLOBAL)
        when (instType) {
            NODE_SCOPE_GLOBAL -> {//隐藏sel App
                sel_app.visibility = View.GONE
            }
            NODE_SCOPE_IN_APP -> {
                btn_sel_app.setOnClickListener(this)
                pkg = intent.getStringExtra("pkg")
                if (pkg != null) {
                    val app = AdvanAppHelper.getAppInfo(pkg!!)
                    if (app != null) {
                        btn_sel_app.text = app.name
                    }
                }
                getAppList()
            }
            0 -> {//新建
                sel_app.visibility = View.GONE
                if (!isReedit) {//add follow
                    parentId = intent.getLongExtra("nodeId", 0)
                    parentNode = DAO.daoSession.actionNodeDao.queryBuilder()
                            .where(ActionNodeDao.Properties.Id.eq(parentId)).unique()

                    parent_lay.visibility = View.VISIBLE
                    parent_title.text = intent.getStringExtra("parent_title")
                }
            }
            else -> {
                GlobalLog.err("新建类型错误")
                finish()
            }
        }
    }

    // TODO 分页
    private fun getAppList() {
        thread {
            apps.addAll(getInstalledApp())
            appHandler.sendEmptyMessage(0)
        }
        bsController?.onLoading()
    }

    private fun initBottom() {
        val bottomView = findViewById<LinearLayout>(R.id.bottom_sheet_view)
        bsController = BottomSheetController(this, bottomView, getString(R.string.text_select_application), R.menu.menu_toolbar_with_search_refresh)
        val searchItem = bsController!!.bottomToolbar.menu.findItem(R.id.menu_search)
        bsController?.bottomToolbar?.setOnMenuItemClickListener(this::onOptionsItemSelected)
        searchView = MenuItemCompat.getActionView(searchItem) as SearchView
        searchView.queryHint = getString(R.string.text_search)

        var sR: Runnable? = null
        val handler = Handler()
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                Vog.d(this, "onQueryTextChange ----> $newText")
                handler.removeCallbacks(sR)
                val nt = newText.trim()
                sR = Runnable {
                    if (nt == "") {
                        bsController?.setBottomListData(apps, onSelAppItemClick)
                    }
                    val tmp = apps.filter {
                        it.title?.contains(nt, ignoreCase = true) == true
                    }
                    bsController?.setBottomListData(tmp as MutableList<ViewModel>, onSelAppItemClick)
                }
                handler.postDelayed(sR, 500)
                return true
            }
        })
        bsController?.hideBottom()
        bsController?.setBottomListData(apps, onSelAppItemClick)
    }

    private val appHandler = Handler {
        bsController?.notifyDataSetChanged()
        bsController?.onSuccess()
        return@Handler true
    }

    private val apps = mutableListOf<ViewModel>()

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> onBackPressed()
            R.id.menu_search -> {
                bsController?.expandSheet()
            }
            R.id.menu_refresh -> {
                apps.clear()
                bsController?.notifyDataSetChanged()
                getAppList()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private lateinit var voast: ColorfulToast

    private fun getInstalledApp(): MutableList<ViewModel> {
        val list = mutableListOf<ViewModel>()
        AdvanAppHelper.APP_LIST.values.forEach {
            list.add(ViewModel(it.name, icon = it.icon, extra = it))
        }
        return list
    }

    override fun onBackPressed() {
        if (bsController?.bottomToolbar?.hasExpandedActionView() == true) {
            bsController?.bottomToolbar?.collapseActionView()
            return
        }

        if (bsController?.isBottomSheetShowing == true) {
            bsController?.hideBottom()
            return
        }

        val now = System.currentTimeMillis()
        if (now - enterTime < 5000) {
            super.onBackPressed()
            return
        }
        MaterialDialog(this)
                .title(R.string.text_confirm_to_exit)
                .message(R.string.text_content_wont_be_save)
                .positiveButton { _ ->
                    super.onBackPressed()
                }.negativeButton()
                .show()
        //TODO 草稿
    }

    private val onSelAppItemClick = object : SimpleListAdapter.OnItemClickListener {
        override fun onClick(holder: SimpleListAdapter.VHolder?, pos: Int, item: ViewModel) {
            val app = item.extra as AppInfo
            btn_sel_app.text = app.name
            pkg = app.packageName
            bsController?.hideBottom()
        }
    }

    private fun save() {
        val inApp = ActionNode.belongInApp(instType)

        val desc = desc_text.text.toString().trim()
        if (desc == "") {//descTitle
            desc_input_lay.error = getString(R.string.text_not_empty)
            return
        }
        if ((scriptText ?: "") == "") {//script
            voast.showShort(getString(R.string.text_tooltips_select_script))
            return
        }
        if ((scriptType ?: "") == "") {//scriptType
            voast.showShort(getString(R.string.text_tooltips_select_script_type))
            return
        }
        if (ActionNode.belongInApp(instType) && pkg == null) {//type
            voast.showShort(getString(R.string.text_tooltips_select_app))
            return
        }
        if (regs.isEmpty()) {//regs
            voast.showShort(getString(R.string.text_at_last_one_regex))
        }

        var activityName: String? = null
        activity_name.text.toString().trim().let {
            activityName = if (it == "") {
                null
            } else it
        }
        if (isReedit) {//保存编辑
            if (editNode == null) {
                voast.showShort(getString(R.string.text_error_occurred))
                GlobalLog.err(getString(R.string.text_error_occurred) + "code: na264")
                return
            }

            if (inApp) {//更新scope
                editNode?.setScopeId(DaoHelper.getActionScopeId(ActionScope(pkg, activityName)))
            }
            val ac = editNode?.action!!
            ac.scriptType = scriptType
            ac.actionScript = scriptText
            DAO.daoSession.actionDao.update(ac)
            editNode?.actionTitle = desc

            editNode!!.regs.forEach { DAO.daoSession.regDao.delete(it) }
            wrapRegs().forEach {
                it.nodeId = editNode!!.id
                DAO.daoSession.regDao.insert(it)
            }
            DAO.daoSession.actionNodeDao.update(editNode)
            ParseEngine.updateNode()
            voast.showShort(getString(R.string.text_save_success))
            DAO.clear()
            finish()

        } else {//新发布 构造newNode
            val newNode = ActionNode()
            if (parentNode != null) {
                newNode.parentId = parentId
//                newNode.parentTagId = parentNode?.tagId
            }
            if (inApp) {
                val scope = ActionScope(pkg, activityName)
                newNode.actionScope = scope
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
                voast.showShort(getString(R.string.text_save_success))
                ParseEngine.updateNode()
                DAO.clear()
                finish()
            } else {
                voast.showShort(getString(R.string.text_save_failed))
            }
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_sel_app -> {
                bsController?.showBottom()
            }
            fab.id -> {
                //check save insert
                try {
                    save()
                } catch (e: Exception) {
                    e.printStackTrace()
                    voast.showShort(getString(R.string.text_error_occurred) + e.message)
                }
            }
            add_regex.id -> {
                showInputRegDialog()
            }
            btn_set_script.id -> {
                showSelScriptDialog()
            }
            test_regex.id -> {
                //模拟
                if (regs.isEmpty()) {
                    voast.showShort(getString(R.string.text_enter_at_last_one_regex))
                    return
                }
                showTestParseDialog()
            }
        }
    }

    private var selScriptDialog: MaterialDialog? = null
    lateinit var scriptTextView: EditText
    private var scriptText: String? = null
        set(value) {
            field = value
            if (code_view.getOptions() == null)
                code_view.setOptions(Options.get(this)
                        .withLanguage(scriptType ?: "lua")
                        .withCode(value ?: ""))
            else {
                code_view.setCode(value ?: "")
            }
        }
    private var scriptType: String? = null

    private fun showSelScriptDialog() {
        if (selScriptDialog == null) {
            val dView = layoutInflater.inflate(R.layout.dialog_sel_script, null)
            scriptTextView = dView.findViewById(R.id.script_text)
            val typeArr = resources.getStringArray(R.array.list_script_type)
            dView.findViewById<Spinner>(R.id.script_type_spinner).also {
                it.setSelection(
                        when (scriptType) {
                            null -> 0
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
                scriptText = editNode?.action?.actionScript
                scriptType = editNode!!.action.scriptType
            }
            selScriptDialog = MaterialDialog(this).noAutoDismiss()
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
                            voast.showShort(getString(R.string.text_cannot_open_file_manager))
                        }
                    }

        }
        scriptTextView.setText(scriptText)
        selScriptDialog?.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {//选择文件回调
            when (requestCode) {
                1 -> {
                    val uri = data?.data
                    if (uri != null) {
                        try {
                            val path = getPathFromUri(this, uri)
                            Vog.d(this, "onActivityResult path: $path")
                            scriptTextView.setText(File(path).readText())
                        } catch (e: Exception) {
                            e.printStackTrace()
                            voast.showShort(getString(R.string.text_open_failed))
                        }
                    } else {
                        voast.showShort(getString(R.string.text_open_failed))
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
                    Vog.d(this, "showTestParseDialog click")
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
                    voast.showLong(getString(R.string.test_tooltips_of_input_test_text))
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

    private fun outputParseResult(result: ParseResult) {
        resultOutput.text = ""
        if (result.isSuccess) {
            val actions = result.actionQueue
            while (actions.isNotEmpty()) {
                val p = actions.poll()
                var arg = p.param.value
                if (arg == null) arg = getString(R.string.text_none)
                val t = String.format(getString(R.string.text_parse_result_placeholder),
                        p.matchWord, arg) // "匹配词: ${p.matchWord} 参数: ${p.param}\n")
                val text = ColourTextClickableSpan(this, t, android.R.color.white, listener = null)
//                Vog.d(this, "outputParseResult $t")
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

    private val posArr = arrayListOf(Reg.PARAM_NO, /*Reg.PARAM_POS_0,*/ Reg.PARAM_POS_1, Reg.PARAM_POS_2, Reg.PARAM_POS_3, Reg.PARAM_POS_END)
    private val regs = mutableListOf<Pair<String, String>>()
    /**
     * 转换测试MapNode List
     * @return regs:List<Pair<String,String>> -> List<ActionNode>
     */
    private fun wrapTestNode(): ActionNode {
        val testId = Int.MAX_VALUE.toLong()

        val testNode = ActionNode(desc_text.text.toString(), testId, -1L, instType, parentId
            ?: 0)
        testNode.follows = emptyList()
        testNode.regs = wrapRegs()
        if (parentNode != null) {
            val fs = parentNode!!.follows
            fs.add(testNode)
            return parentNode!!
        }

        return testNode
    }

    private fun wrapRegs(): List<Reg> {
        val tregs = mutableListOf<Reg>()
        regs.forEach {
            val p = posArr[posData.indexOf(it.second)]
            tregs.add(Reg(it.first, p))
        }
        return tregs
    }

    var isModify = -1
    private fun add2RegexList(reg: String, pos: String) {
        Vog.d(this, "add2RegexList $reg $pos")
        if (isModify > -1) {
            regs[isModify] = Pair(reg, pos)
            isModify = -1
        } else {
            regs.add(Pair(reg, pos))
        }
        regAdapter.notifyDataSetChanged()
    }


    lateinit var regEditText: EditText
    lateinit var spinner: Spinner
    lateinit var posData: ArrayList<String>

    private var inputDialog: Dialog? = null
    private fun showInputRegDialog(reg: String? = null, pos: String? = null) {
        if (inputDialog == null) {
            val dialogView = layoutInflater.inflate(R.layout.dialog_add_new_regex, null)

            regEditText = dialogView.findViewById(R.id.text_regex)
            spinner = dialogView.findViewById(R.id.pos_of_param_spinner)

            //适配器
            val arrAdapter = ArrayAdapter<String>(this,
                    android.R.layout.simple_spinner_item, posData)
            //设置样式
            arrAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            //加载适配器
            spinner.adapter = arrAdapter
            regEditText.setText(reg)
            val p = if (pos != null) posData.indexOf(pos) else 0
            spinner.setSelection(p)
            inputDialog = AlertDialog.Builder(this)
                    .setTitle(R.string.text_add_regex)
                    .setView(dialogView)
                    .setPositiveButton("确认") { _, _ ->
                        val r = regEditText.text.trim().toString()
                        if (r == "") {
                            inputDialog?.hide()
                        } else
                            add2RegexList(r, posData[spinner.selectedItemPosition])
                    }.setNegativeButton("取消") { i, _ ->
                        isModify = -1
                        inputDialog?.hide()
                    }.setOnDismissListener {
                        regEditText.text.clear()
                        isModify = -1
                    }.show()
        } else {
            regEditText.setText(reg)
            val p = if (pos != null) posData.indexOf(pos) else 0
            spinner.setSelection(p)
            inputDialog?.show()
        }
    }

    inner class RegListAdapter(context: Context, val dataSet: MutableList<Pair<String, String>>?) : BaseListAdapter<Holder, Pair<String, String>>(context, dataSet) {

        override fun onCreateViewHolder(view: View): Holder {
            return Holder(view)
        }

        override fun layoutId(): Int = R.layout.item_left_right_text
        override fun onBindView(holder: Holder, pos: Int, item: Pair<String, String>) {
            holder.left.text = item.first
            holder.right.text = item.second
            holder.itemView.setOnClickListener {
                isModify = pos
                showInputRegDialog(item.first, item.second)
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
        val right = itemView.findViewById<TextView>(R.id.right_text)
    }

}
