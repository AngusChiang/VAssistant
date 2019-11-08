package cn.vove7.jarvis.view.dialog

import android.annotation.SuppressLint
import android.content.Context
import android.view.ViewGroup
import android.widget.TextView
import cn.vove7.jarvis.R
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItems

/**
 * # SearchableListDialog
 *
 * @author Administrator
 * 2018/12/18
 */
class SearchableListDialog(val context: Context, val items: List<String>, val onSel: (Pair<Int, String>, showItems: List<String>) -> Unit) {
    var tmp: MutableList<String> = items.toMutableList()
    val dialog: MaterialDialog by lazy {
        MaterialDialog(context).listItems(items = items, waitForPositiveButton = false) { _, index, text ->
            onSel.invoke(Pair(index, text.toString()), tmp)
        }.apply {
            searchable()
        }
    }

    inline fun show(func: MaterialDialog.() -> Unit): SearchableListDialog {
        dialog.func()
        dialog.show()
        return this
    }

    @SuppressLint("CheckResult")
    @Synchronized
    private fun onSearch(s: String) {
        if (s.trim() == "") {
            tmp = items.toMutableList()
        } else {
            tmp.clear()
            items.forEach {
                if (it.contains(s))
                    tmp.add(it)
            }
        }
        dialog.listItems(items = tmp)
    }

}

fun MaterialDialog.searchable() {
    findViewById<ViewGroup>(R.id.md_title_layout)?.apply {
        val searchIcon = TextView(context)
        searchIcon.text = "1234"
        searchIcon.width = 50
        searchIcon.height = 50
        searchIcon.background = context.getDrawable(R.drawable.ic_search_black_24dp)
        addView(searchIcon)
    }

}