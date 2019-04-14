package cn.vove7.jarvis.view.dialog

import android.content.Context
import android.view.View
import android.widget.*
import cn.vove7.common.datamanager.parse.statusmap.Reg
import cn.vove7.common.netacc.ApiUrls
import cn.vove7.common.bridges.SystemBridge
import cn.vove7.jarvis.R
import cn.vove7.vtp.easyadapter.BaseListAdapter
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView

/**
 * # InstRegexEditorDialog
 *
 * @author Administrator
 * 2018/10/23
 */
@Deprecated("无用")
class InstRegexEditorDialog(
        val context: Context,
        private val reg: String? = null,//正则
        posArr: Array<Int>? = arrayOf(),//参数位置s
        private val onNewSuccess: (Pair<String, Array<Int>>) -> Unit// 新建/编辑成功 -> reg, 参数位置s [@Reg.PARAM_POS)]

) {
    val dialog = MaterialDialog(context)
            .customView(R.layout.dialog_new_inst_regex, scrollable = true)
            .title(R.string.text_add_regex)
            .positiveButton {
                val re = regEditor.text.toString().trim()
                if (re == "") {
                    it.dismiss()
                    return@positiveButton
                }
                onNewSuccess.invoke(Pair(re, regList))
            }
            .negativeButton()
            .neutralButton(text = "帮助") {
                SystemBridge.openUrl(ApiUrls.INST_REGEX_GUIDE)
            }

    private val regListView: ListView by lazy { dialog.findViewById<ListView>(R.id.reg_list) }
    private val regEditor: EditText by lazy { dialog.findViewById<EditText>(R.id.text_regex) }

    private val regList: Array<Int>
        get() = regListAdapter.getPosList()

    private val regPos = posArr?.toMutableList() ?: mutableListOf()

    init {
        initView()
        dialog.show()
    }

    lateinit var regListAdapter: RegListAdapter
    private fun initView() {
        regListAdapter = RegListAdapter(context, regPos)
        regListView.adapter = regListAdapter
        regEditor.setText(reg)
        Button(context).apply {
            text = "添加参数"
            regListView.addFooterView(this)
            setOnClickListener {
                regPos.add(Reg.PARAM_POS_1)
                regListAdapter.notifyDataSetChanged()
            }
        }
    }

    class RegListAdapter(val context: Context, val ds: MutableList<Int>)
        : BaseListAdapter<V, Int>(context, ds) {
        private val holders = hashMapOf<Int, V>()
        override fun layoutId(position: Int): Int = R.layout.item_of_dialog_new_inst_regex
        fun getPosList(): Array<Int> {
//            val arr = Array(count) { 0 }
//            holders.forEach {
//                arr[it.key] = ds[]
//            }
            return ds.toTypedArray()
        }

        //显示元素，对应zhi
        private val posIndexArr = arrayListOf(Reg.PARAM_POS_1, Reg.PARAM_POS_2,
                Reg.PARAM_POS_3, Reg.PARAM_POS_END)

        override fun onBindView(holder: V, pos: Int, item: Int) {
            holders[pos] = holder
            holder.textView.text = "参数${pos}位置 ："
            //设置样式
            val arrAdapter = ArrayAdapter<String>(context,
                    android.R.layout.simple_spinner_item, posData)
            arrAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            holder.posSpinner.apply {
                adapter=arrAdapter
                setSelection(posIndexArr.indexOf(item))
                onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onNothingSelected(parent: AdapterView<*>?) {}

                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        ds[pos] = posIndexArr[position]
                    }
                }
            }
            holder.delBtn.setOnClickListener {
                ds.removeAt(pos)
                notifyDataSetChanged()
            }
        }

        override fun onCreateViewHolder(view: View): V = V(view)

        private val posData: Array<String> by lazy {
            context.resources.getStringArray(R.array.list_pos_of_regex_param)
        }
    }

    class V(v: View) : BaseListAdapter.ViewHolder(v) {
        val textView = v.findViewById<TextView>(R.id.text)!!
        val posSpinner = v.findViewById<Spinner>(R.id.pos_spinner)!!
        val delBtn = v.findViewById<ImageView>(R.id.del_img)!!
    }
}