package cn.vove7.jarvis.view.dialog

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import cn.vove7.jarvis.R
import cn.vove7.executorengine.bridges.ChoiceData
import cn.vove7.vtp.easyadapter.BaseListAdapter

/**
 *
 *
 * Created by Vove on 2018/6/21
 */
class SingleChoiceDialog(context: Context, title: String, list: List<ChoiceData>, val listener: OnSelectListener)
    : BaseChoiceDialog<SingleChoiceDialog.Adapter.Holder>(context, title, SingleChoiceDialog.Adapter(context, list, listener)) {

    override fun show(): Dialog {
        try {
            super.show()
        } catch (e: Exception) {
            listener.onSingleSelect(0, null, "无悬浮窗权限")
        }
        dialog.setOnDismissListener{
            listener.onSingleSelect(0, null)
        }
        dialog.setButton(DialogInterface.BUTTON_POSITIVE, "取消", View.OnClickListener {
            listener.onSingleSelect(0, null)
            dialog.dismiss()
        })
        dialog.setWidth(0.9)
        dialog.show()
        return dialog
    }

    class Adapter(context: Context, choiceDataSet: List<ChoiceData>, private val listener: OnSelectListener) : BaseListAdapter<Adapter.Holder, ChoiceData>(context, choiceDataSet) {
        override fun onCreateViewHolder(view: View): Holder {
            return Holder(view)
        }

        /**
         * @return your layout
         */
        override fun layoutId(): Int = R.layout.item_of_dialog_choice_list

        /**
         * Init your holder's contentView whit holder
         */
        override fun onBindView(holder: Holder, pos: Int, item: ChoiceData) {
            holder.itemView.setOnClickListener {
                listener.onSingleSelect(pos, item)
            }
            holder.choiceTitle.text = item.title
            holder.choiceSubtitle.text = item.subtitle ?: ""
            if (item.iconDrawable != null)
                holder.choiceIcon.setImageDrawable(item.iconDrawable)
        }

        inner class Holder(itemView: View) : BaseListAdapter.ViewHolder(itemView) {
            val choiceTitle = itemView.findViewById<TextView>(R.id.title)!!
            val choiceSubtitle = itemView.findViewById<TextView>(R.id.subtitle)!!
            val choiceIcon = itemView.findViewById<ImageView>(R.id.icon)!!
        }
    }
}

/**
 * 单选回调
 */
interface OnSelectListener {
    fun onSingleSelect(pos: Int, data: ChoiceData?, msg: String = "")
}