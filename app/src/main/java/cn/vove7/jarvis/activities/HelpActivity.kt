package cn.vove7.jarvis.activities

import android.os.Bundle
import android.os.Handler
import android.support.design.widget.TextInputLayout
import android.view.Gravity
import android.view.View
import android.widget.AdapterView
import android.widget.TextView
import cn.vove7.common.app.GlobalLog
import cn.vove7.common.netacc.ApiUrls
import cn.vove7.common.netacc.NetHelper
import cn.vove7.common.netacc.model.BaseRequestModel
import cn.vove7.common.netacc.model.UserFeedback
import cn.vove7.common.view.editor.MultiSpan
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_abc_header)
        header_content.addView(layoutInflater.inflate(R.layout.header_help, null))

        list_view.adapter = IconTitleListAdapter(this, getData())
        list_view.onItemClickListener = this
        list_view.setOnItemLongClickListener { parent, view, position, id ->
            if (position == 5) {
                GlobalLog.export2Sd()
                return@setOnItemLongClickListener true
            }
            return@setOnItemLongClickListener false
        }
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        when (position) {
            0 -> {
                SystemBridge.openUrl(ApiUrls.USER_GUIDE)
                Handler().postDelayed({
                    toast.showShort("请查阅文章：用户手册")
                }, 4000)
            }
            1 -> {
                MaterialDialog(this).show {
                    title(text = "快捷键")
                    val text = TextView(this@HelpActivity).apply {
                        setPadding(50, 0, 50, 30)
                        append("1. 长按音量上键进行唤醒。\n" +
                                "2. 在聆听时，可通过点按音量上键停止聆听，点按下音量下键取消聆听。\n" +
                                "3. 在执行时，可长按下键，终止执行\n" +
                                "4. 有线耳机适用\n" +
                                "5. 锁屏下可进行唤醒。\n\n")
                        append(MultiSpan(this@HelpActivity,
                                "以上音量快捷键需要无障碍支持", bold = true).spanStr)
                    }
                    customView(view = text, scrollable = true)
                }
            }
            2 -> SystemBridge.openUrl(ApiUrls.USER_FAQ)
            3 -> SystemBridge.openUrl(ApiUrls.QQ_GROUP_1)
            4 -> showFeedbackDialog()
            5 -> {
                val logView = TextView(this)
                logView.setPadding(50, 0, 50, 0)
                logView.gravity = Gravity.BOTTOM
                logView.text = GlobalLog.toString()
                MaterialDialog(this).title(text = "日志")
                        .customView(view = logView, scrollable = true)
                        .positiveButton(text = "复制") {
                            SystemBridge.setClipText(logView.text.toString())
                            toast.showShort(R.string.text_copied)
                        }
                        .negativeButton(text = "清空"){
                            GlobalLog.clear()
                        }
                        .neutralButton(text = "导出至文件"){
                            GlobalLog.export2Sd()
                        }
                        .show()

            }
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
                IconTitleEntity(R.drawable.ic_book_24dp, R.string.text_service_manual),
                IconTitleEntity(R.drawable.ic_accessibility, R.string.text_hot_key_desc)
                , IconTitleEntity(R.drawable.ic_question_answer, R.string.text_faq)
                , IconTitleEntity(R.drawable.ic_qq, R.string.text_add_qq_group)
                , IconTitleEntity(R.drawable.ic_feedback_black_24dp, R.string.text_feedback)
                , IconTitleEntity(R.drawable.ic_bug_report_24dp, titleId = R.string.text_explore_log,
                summaryId = R.string.text_long_press_to_export_log)
        )
    }
}