package cn.vove7.jarvis.activities

import android.graphics.Typeface
import android.os.Bundle
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
import cn.vove7.jarvis.tools.ItemWrap
import cn.vove7.jarvis.tools.Tutorials
import cn.vove7.jarvis.view.dialog.ProgressTextDialog
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
    lateinit var adapter: IconTitleListAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_abc_header)
        header_content.addView(layoutInflater.inflate(R.layout.header_help, null))

        list_view.adapter = IconTitleListAdapter(this, getData()).also { adapter = it }
        list_view.onItemClickListener = this
        list_view.setOnItemLongClickListener { parent, view, position, id ->
            if (position == 5) {
                GlobalLog.export2Sd()
                return@setOnItemLongClickListener true
            }
            return@setOnItemLongClickListener false
        }
        startTutorials()
    }

    private fun startTutorials() {
        list_view.post {
            Tutorials.oneStep(this, list = arrayOf(
                    ItemWrap(Tutorials.h_t_1, adapter.holders[0]?.titleView, getString(R.string.text_advanced_user_guide), "自定义指令、脚本Api、远程调试都在这里")
                    , ItemWrap(Tutorials.h_t_2, adapter.holders[1]?.titleView, getString(R.string.text_hot_key_desc), "快速了解快捷键操作")
                    , ItemWrap(Tutorials.h_t_5, adapter.holders[2]?.titleView, getString(R.string.text_faq), "遇到问题可先查看常见问题再进行反馈")
                    , ItemWrap(Tutorials.h_t_3, adapter.holders[4]?.titleView, getString(R.string.text_feedback), "在这里进行反馈与建议，或者加入QQ群")
                    , ItemWrap(Tutorials.h_t_4, adapter.holders[5]?.titleView, getString(R.string.text_explore_log), "当出现问题时，或非预想结果时可提供日志给作者")
            ))
        }
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        when (position) {
            0 -> {
                SystemBridge.openUrl(ApiUrls.USER_GUIDE)
            }
            1 -> {
                MaterialDialog(this).show {
                    title(text = "快捷键")
                    val text = TextView(this@HelpActivity).apply {
                        setPadding(50, 0, 50, 30)
                        append("1. 长按音量上键进行唤醒。\n" +
                                "2. 在聆听时，可通过点按音量上键停止聆听，点按下音量下键取消聆听。\n" +
                                "3. 在执行时，可长按下键，终止执行\n" +
                                "4. 有线耳机适用,并且支持长按中键唤醒\n" +
                                "5. 锁屏下可进行唤醒。\n\n")
                        append(MultiSpan(this@HelpActivity,
                                "以上音量快捷键需要无障碍支持", typeface = Typeface.BOLD).spanStr)
                    }
                    customView(view = text, scrollable = true)
                }
            }
            2 -> {
                ProgressTextDialog(this, "常见问题",autoLink = true).apply {
                    faqs.forEach {
                        appendlnBold("· " + it.first)
                        appendln(it.second)
                        appendln()
                    }
                }
            }
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
                        .negativeButton(text = "清空") {
                            GlobalLog.clear()
                        }
                        .neutralButton(text = "导出至文件") {
                            GlobalLog.export2Sd()
                        }
                        .show()

            }
        }
    }

    private val faqs
        get() = listOf(
                Pair("突然按键失灵了?", "按键失灵一般是由于程序后台被杀导致的，目前也属于安卓系统的bug，解决方法就是进入App详情，将此应用强行停止。参考 如何为APP上锁")
                , Pair("如何为App上锁?", "在App内进入最近任务，上锁；若此时最近任务没有出现，可尝试进入[例如插件管理、指令管理]页面再进入最近任务上锁。或者自带管家白名单")
                , Pair("通知栏显示服务正在运行", "可尝试在应用设置中关闭此通知")
                , Pair("屏幕助手如何设置", "1. 先在App设置中 设为默认辅助应用(App重启需要重新设置)\n" +
                "2. App里开启 实验室/屏幕助手\n" +
                "3. 查看手机系统 唤出助手的方式\n" +
                "  - 一般为长按Home键" +
                "  - 一加氢OS安卓P版本可在[设置/按键和手势/快捷开启助手应用 开启]，" +
                "氢OS8.0版本设置方式参考https://www.coolapk.com/feed/8889370\n" +
                "4. 可能部分系统无法设置")
        )

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
                IconTitleEntity(R.drawable.ic_book_24dp, R.string.text_advanced_user_guide),
                IconTitleEntity(R.drawable.ic_accessibility, R.string.text_hot_key_desc)
                , IconTitleEntity(R.drawable.ic_question_answer, R.string.text_faq)
                , IconTitleEntity(R.drawable.ic_qq, R.string.text_add_qq_group)
                , IconTitleEntity(R.drawable.ic_feedback_black_24dp, R.string.text_feedback)
                , IconTitleEntity(R.drawable.ic_bug_report_24dp, titleId = R.string.text_explore_log,
                summaryId = R.string.text_long_press_to_export_log)
        )
    }
}