package cn.vove7.jarvis.adapters

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.TextView
import cn.vove7.common.datamanager.parse.statusmap.ActionNode
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

    private val ActionNode.regParamSet: Set<String>
        get() {
            val set = mutableSetOf<String>()
            @Suppress("RegExpRedundantEscape")//运行时解析错误
            val reg = "@\\{#?([^}.]+)\\}".toRegex()
            regs?.map { it.regStr }?.forEach { s ->
                reg.findAll(s).map { it.groupValues[1] }.forEach { set.add(it) }
            }
            return set
        }

    override fun onBindView(holder: VHolder, pos: Int, item: ActionNode) {
        holder.descText.text = item.actionTitle
        if (item.actionTitle.startsWith("打开App:")) {
            holder.paramText.isEnabled = false
        } else {
            val pset = item.regParamSet
            if (pset.isEmpty()) {
                holder.paramText.isEnabled = false
            } else {
                holder.paramText.setText(pset.joinToString(":\n", postfix = if (pset.isEmpty()) "" else ":"))
                holder.paramText.addTextChangedListener(object : TextWatcher {
                    override fun afterTextChanged(s: Editable?) {
                    }

                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                    }

                    //TODO 输入后解析
                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                        val action = item.action
                        action.param = try {
                            parseLineParam(s.toString())
                        } catch (e: Exception) {
                            null
                        }
                    }

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