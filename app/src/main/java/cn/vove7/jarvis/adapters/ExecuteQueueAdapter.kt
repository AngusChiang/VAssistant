package cn.vove7.jarvis.adapters

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.TextView
import cn.vove7.common.datamanager.parse.statusmap.ActionNode
import cn.vove7.jarvis.R
import cn.vove7.jarvis.activities.base.CodeEditorActivity
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

    override fun onBindView(holder: VHolder, pos: Int, item: ActionNode) {
        holder.descText.text = item.actionTitle
        holder.paramText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val action = item.action
                action.param = CodeEditorActivity.parseSimpleMap(s.toString())
            }
        })

    }

    override fun onCreateViewHolder(view: View): VHolder {
        return VHolder(view)
    }

    class VHolder(v: View) : BaseListAdapter.ViewHolder(v) {
        val descText: TextView = v.findViewById(R.id.desc_text)
        val paramText: EditText = v.findViewById(R.id.param_text)
    }

}