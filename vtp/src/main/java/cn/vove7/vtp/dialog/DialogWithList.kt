package cn.vove7.vtp.dialog

import android.content.Context
import android.os.Bundle
import android.widget.BaseAdapter
import android.widget.ListView
import cn.vove7.vtp.R

class DialogWithList(context: Context, val adapter: BaseAdapter) : BaseDialog(context) {
    lateinit var listView: ListView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_list_layout)
        listView = findViewById(R.id.dialog_list)

        listView.adapter = adapter
        notifyDataChanged()
    }

    fun notifyDataChanged() {
        adapter.notifyDataSetChanged()
    }
}