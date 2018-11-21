package cn.vove7.jarvis.view.custom

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import cn.vove7.common.app.GlobalApp
import cn.vove7.jarvis.R
import cn.vove7.jarvis.view.SettingChildItem

class SettingGroupItem(
        val iconId: Int,
        private val titleS: String? = null,
        private val titleId: Int? = null,
        val childItems: List<SettingChildItem>
) {
    var title: String = ""
        get() {
            return when {
                titleS != null -> titleS
                titleId != null -> GlobalApp.getString(titleId)
                else -> "null"
            }
        }
}

class GroupItemHolder(v: View) {
    val itemView = v
    val lineView: View = v.findViewById(R.id.line)
    val titleView: TextView = v.findViewById(R.id.title)
    val downIcon = v.findViewById<ImageView>(R.id.down_icon)
}