package cn.vove7.accessibilityservicedemo.dialog

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.view.View
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import cn.vove7.accessibilityservicedemo.R
import cn.vove7.executorengine.bridge.ChoiceData
import cn.vove7.vtp.easyadapter.BaseListAdapter

/**
 *
 *
 * Created by Vove on 2018/6/21
 */
class MultiChoiceDialog(context: Context, title: String, list: List<ChoiceData>, val listener: OnMultiSelectListener)
    : BaseChoiceDialog<MultiChoiceDialog.Adapter.Holder>(context, title, Adapter(context, list, listener)) {

    override fun show(): Dialog {
        try {
            super.show()
        } catch (E: Exception) {
            listener.onMultiSelect(null, "无悬浮窗权限")
        }
        dialog.setTitle(title)
        dialog.setButton(DialogInterface.BUTTON_POSITIVE, "确定", View.OnClickListener {
            listener.onMultiSelect((adapter as Adapter).selectedData)
        })
        dialog.setButton(DialogInterface.BUTTON_POSITIVE, "取消", View.OnClickListener {
            listener.onMultiSelect(null)
        })
        dialog.show()
        return dialog
    }

    class Adapter(context: Context, choiceDataSet: List<ChoiceData>, private val listener: OnMultiSelectListener) : BaseListAdapter<Adapter.Holder, ChoiceData>(context, choiceDataSet) {
        override fun onCreateViewHolder(view: View): Holder {
            return Holder(view)
        }

        val selectedData = mutableListOf<ChoiceData>()
        /**
         * @return your layout
         */
        override fun layoutId(): Int = R.layout.item_of_dialog_choice_list

        /**
         * Init your holder's view whit holder
         */
        override fun onBindView(holder: Holder, pos: Int, item: ChoiceData) {
            holder.itemView.setOnClickListener {
                when (holder.checkBox.isChecked) {
                    true -> {
                        selectedData.remove(item)
                    }
                    else -> {
                        selectedData.add(item)
                    }
                }
            }
            holder.choiceTitle.text = item.title
            holder.choiceSubtitle.text = item.subtitle ?: ""
            if (item.iconDrawable != null)
                holder.choiceIcon.setImageDrawable(item.iconDrawable)
        }

        class Holder(itemView: View) : BaseListAdapter.ViewHolder(itemView) {
            val checkBox = itemView.findViewById<CheckBox>(R.id.check_box)
            val choiceTitle = itemView.findViewById<TextView>(R.id.title)!!
            val choiceSubtitle = itemView.findViewById<TextView>(R.id.subtitle)!!
            val choiceIcon = itemView.findViewById<ImageView>(R.id.icon)!!

            init {
                checkBox.visibility = View.VISIBLE
            }
        }
    }
}

/**
 * 多选回调
 */
interface OnMultiSelectListener {
    fun onMultiSelect(data: List<ChoiceData>?, msg: String = "")
}