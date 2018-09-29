package cn.vove7.jarvis.activities

import android.os.Bundle
import android.support.design.widget.TextInputLayout
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.AdapterView
import cn.vove7.common.app.GlobalLog
import cn.vove7.common.netacc.ApiUrls
import cn.vove7.common.netacc.model.BaseRequestModel
import cn.vove7.common.netacc.model.UserFeedback
import cn.vove7.common.utils.NetHelper
import cn.vove7.common.view.toast.ColorfulToast
import cn.vove7.executorengine.bridges.SystemBridge
import cn.vove7.jarvis.R
import cn.vove7.jarvis.activities.base.ReturnableActivity
import cn.vove7.jarvis.adapters.IconTitleEntity
import cn.vove7.jarvis.adapters.IconTitleListAdapter
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import kotlinx.android.synthetic.main.activity_abc_header.*

/**
 * # HelpActivity
 *
 * @author Administrator
 * 9/23/2018
 */
class HelpActivity : ReturnableActivity(), AdapterView.OnItemClickListener {
    val toast: ColorfulToast by lazy {
        ColorfulToast(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_abc_header)
        header_content.addView(layoutInflater.inflate(R.layout.header_help, null))

        list_view.adapter = IconTitleListAdapter(this, getData())
        list_view.onItemClickListener = this
        list_view.setOnItemLongClickListener { parent, view, position, id ->
            if (position == 2) {
                GlobalLog.export2Sd()
                return@setOnItemLongClickListener true
            }
            return@setOnItemLongClickListener false
        }
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        when (position) {
            0 -> SystemBridge().openUrl(ApiUrls.USER_GUIDE)
            1 -> showFeedbackDialog()
//            2 ->
        }
    }

    private fun showFeedbackDialog() {
        MaterialDialog(this)
                .title(R.string.text_feedback)
                .customView(R.layout.dialog_feedback)
                .cancelOnTouchOutside(false)
                .noAutoDismiss()
                .positiveButton(R.string.text_send) { d ->
                    val bar = d.findViewById<View>(R.id.loading_bar)
                    if (bar.visibility == View.VISIBLE) return@positiveButton

                    val title = getOrSetEmptyErr(d.findViewById(R.id.title_text))
                        ?: return@positiveButton

                    val content = getOrSetEmptyErr(d.findViewById(R.id.desc_text))
                        ?: return@positiveButton

                    bar.visibility = View.VISIBLE
                    val f = UserFeedback(title, content)
                    NetHelper.postJson<Any>(ApiUrls.NEW_USER_FEEDBACK, BaseRequestModel(f)) { _, b ->
                        bar.visibility = View.INVISIBLE
                        if (b?.isOk() == true) {
                            toast.showShort("已收到您的反馈，感谢支持")
                            d.dismiss()
                        } else {
                            toast.showShort(R.string.text_net_err)
                        }
                    }
                }.negativeButton { it.cancel() }
                .show()
    }

    private fun getOrSetEmptyErr(it: TextInputLayout): String? {
        val s = it.editText?.text.toString()
        if (s.trim() == "") {
            it.error = getString(R.string.text_not_empty)
            return null
        }
        it.error = ""
        return s
    }

    private fun getData(): List<IconTitleEntity> {
        return listOf(
                IconTitleEntity(R.drawable.ic_book_24dp, R.string.text_service_manual)
                , IconTitleEntity(R.drawable.ic_feedback_black_24dp, R.string.text_feedback)
                , IconTitleEntity(R.drawable.ic_bug_report_24dp, titleId = R.string.text_explore_log,
                summaryId = R.string.text_long_press_to_export_log)
        )
    }
}