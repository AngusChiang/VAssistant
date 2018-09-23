package cn.vove7.jarvis.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.AdapterView
import cn.vove7.common.netacc.ApiUrls
import cn.vove7.executorengine.bridges.SystemBridge
import cn.vove7.jarvis.R
import cn.vove7.jarvis.adapters.IconTitleEntity
import cn.vove7.jarvis.adapters.IconTitleListAdapter
import kotlinx.android.synthetic.main.activity_abc_header.*

/**
 * # HelpActivity
 *
 * @author Administrator
 * 9/23/2018
 */
class HelpActivity : AppCompatActivity(), AdapterView.OnItemClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_abc_header)
        header_content.addView(layoutInflater.inflate(R.layout.header_help, null))

        list_view.adapter = IconTitleListAdapter(this, getData())
        list_view.onItemClickListener = this
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        when (position) {
            0 -> {
                SystemBridge().openUrl(ApiUrls.USER_GUIDE)
            }
            1 -> {
            }
        }
    }

    private fun getData(): List<IconTitleEntity> {
        return listOf(
                IconTitleEntity(R.drawable.ic_book_24dp, R.string.text_advanced_user_guide)
                , IconTitleEntity(R.drawable.ic_feedback_black_24dp, R.string.text_feedback)
        )
    }
}