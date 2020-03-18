package cn.vove7.jarvis.adapters

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.TextView
import cn.vove7.common.datamanager.parse.statusmap.ActionNode
import cn.vove7.common.utils.regParamSet
import cn.vove7.jarvis.R
import cn.vove7.smartkey.tool.Vog
import cn.vove7.vtp.easyadapter.BaseListAdapter

/**
 * # ExecuteQueueAdapter
 *
 * @author 17719247306
 * 2018/9/8
 */
class ExecuteQueueAdapter(context: Context, execQueue: MutableList<ActionNode>)
    : BaseListAdapter<ExecuteQueueAdapter.VHolder, ActionNode>(context, execQueue) {
    override fun layoutId(position: Int): Int = R.layout.item_of_exec_queue

    private val ets = Array(execQueue.size) { "" }

    val allParams: List<Map<String, String>>
        get() = ets.map { parseLineParam(it) }

    override fun onBindView(holder: VHolder, pos: Int, item: ActionNode) {
        holder.descText.text = item.actionTitle
        if (item.actionTitle.startsWith("打开App:")) {
            holder.paramText.isEnabled = false
            holder.paramText.hint = "无需参数"
            holder.paramText.text = null
        } else {
            val pset = item.regParamSet
            if (pset.isEmpty()) {
                holder.paramText.isEnabled = false
            } else {
                holder.paramText.setText(pset.joinToString(":\n",
                        postfix = if (pset.isEmpty()) "" else ":"))
                holder.paramText.addTextChangedListener(object : TextWatcher {
                    override fun afterTextChanged(s: Editable?) {
                        ets[pos] = s.toString()
                    }

                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                })
            }
        }
    }

    private fun parseLineParam(text: String): Map<String, String> {
        val map = mutableMapOf<String, String>()
        text.lines().filter(String::isNotBlank).forEach { line ->
            val mi = line.indexOf(':')
            val key = line.substring(0, mi).trim()
            val value = line.substring(mi + 1).trim()
            if (key.isNotBlank() && value.isNotBlank())
                map[key] = value
        }
        Vog.d("parseLineParam $map")
        return map
    }

    override fun onCreateViewHolder(view: View): VHolder {
        return VHolder(view)
    }

    class VHolder(v: View) : BaseListAdapter.ViewHolder(v) {
        val descText: TextView = v.findViewById(R.id.desc_text)
        val paramText: EditText = v.findViewById(R.id.param_text)
    }

}