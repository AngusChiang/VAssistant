package cn.vove7.jarvis.activities

import android.content.ComponentName
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.TextView
import cn.vove7.bottomdialog.BottomDialog
import cn.vove7.bottomdialog.builder.buttons
import cn.vove7.bottomdialog.extension.awesomeHeader
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.app.GlobalLog
import cn.vove7.common.bridges.SystemBridge
import cn.vove7.common.net.ApiUrls
import cn.vove7.common.net.WrapperNetHelper
import cn.vove7.common.net.model.UserFeedback
import cn.vove7.common.utils.span
import cn.vove7.jarvis.BuildConfig
import cn.vove7.jarvis.R
import cn.vove7.jarvis.activities.base.ReturnableActivity
import cn.vove7.jarvis.adapters.IconTitleEntity
import cn.vove7.jarvis.adapters.IconTitleListAdapter
import cn.vove7.jarvis.tools.ItemWrap
import cn.vove7.jarvis.tools.Tutorials
import cn.vove7.jarvis.view.dialog.contentbuilder.MarkdownContentBuilder
import cn.vove7.jarvis.view.dialog.contentbuilder.SmoothTextBuilder
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.main.activity_abc_header.*
import java.io.File

/**
 * # HelpActivity
 *
 * @author Administrator
 * 9/23/2018
 */
class HelpActivity : ReturnableActivity() {
    lateinit var adapter: IconTitleListAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_abc_header)
        header_content.addView(layoutInflater.inflate(R.layout.header_help, null))

        list_view.adapter = IconTitleListAdapter(this, getData()).also { adapter = it }
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
                    ItemWrap(Tutorials.h_t_1, adapter.holders[0]?.titleView, getString(R.string.text_user_manual), "自定义指令、脚本Api、远程调试都在这里")
                    , ItemWrap(Tutorials.h_t_2, adapter.holders[1]?.titleView, getString(R.string.text_hot_key_desc), "快速了解快捷键操作")
                    , ItemWrap(Tutorials.h_t_5, adapter.holders[2]?.titleView, getString(R.string.text_faq), "遇到问题可先查看常见问题再进行反馈")
                    , ItemWrap(Tutorials.h_t_3, adapter.holders[4]?.titleView, getString(R.string.text_feedback), "在这里进行反馈与建议，或者加入QQ群")
                    , ItemWrap(Tutorials.h_t_4, adapter.holders[5]?.titleView, getString(R.string.text_browse_log), "当出现问题时，或非预想结果时可提供日志给作者")
            ))
        }
    }


    //TODO
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
                    WrapperNetHelper.postJson<Any>(ApiUrls.NEW_USER_FEEDBACK, f) {
                        success { _, b ->
                            bar.visibility = View.INVISIBLE
                            if (b.isOk()) {
                                GlobalApp.toastInfo("已收到您的反馈，感谢支持")
                                d.dismiss()
                            } else {
                                GlobalApp.toastError(R.string.text_net_err)
                            }
                        }
                        fail { _, e ->
                            GlobalApp.toastError(e.message ?: "err")
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

    private fun getData(): List<IconTitleEntity> = listOf(
            IconTitleEntity(R.drawable.ic_help, R.string.using_help) {
                BottomDialog.builder(this) {
                    awesomeHeader(getString(R.string.using_help))
                    content(MarkdownContentBuilder()) {
                        loadMarkdownFromAsset("files/introduction.md")
                    }
                }
            },
            IconTitleEntity(R.drawable.ic_book_24dp, R.string.text_user_manual) {
                SystemBridge.openUrl(ApiUrls.USER_GUIDE)
            },
            IconTitleEntity(R.drawable.ic_accessibility, R.string.text_hot_key_desc) {

                MaterialDialog(this).show {
                    title(text = "快捷键")
                    val text = TextView(this@HelpActivity).apply {
                        setPadding(50, 0, 50, 30)
                        append("1. 长按音量上键进行唤醒。\n" +
                                "2. 按下松开音量上键，再快速长按可持续加大音量。\n" +
                                "3. 在聆听时，可通过点按音量上键停止聆听，点按下音量下键取消聆听。\n" +
                                "4. 在执行时，可长按下键，终止执行\n" +
                                "5. 在长语音聆听时，可长按下键，结束长语音\n" +
                                "6. 有线耳机适用,并且支持长按中键唤醒\n" +
                                "7. 锁屏下可进行唤醒。\n\n")
                        append("以上音量快捷键需要无障碍支持".span(typeface = Typeface.BOLD))
                    }
                    customView(view = text, scrollable = true)
                }
            },
            IconTitleEntity(R.drawable.ic_question_answer, R.string.text_faq) {
                BottomDialog.builder(this) {
                    awesomeHeader("常见问题")
                    content(MarkdownContentBuilder()) {
                        loadMarkdownFromAsset("files/faqs.md")
                    }
                }
            },
            IconTitleEntity(R.drawable.ic_qq, R.string.text_add_qq_group) {
                val qqPkg = "com.tencent.mobileqq"
                if (SystemBridge.hasInstall(qqPkg)) {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = Uri.parse(ApiUrls.QQ_GROUP_1)
                    intent.component = ComponentName(qqPkg, "com.tencent.mobileqq.activity.JumpActivity")
                    GlobalApp.APP.startActivity(intent)
                } else {
                    SystemBridge.openUrl(ApiUrls.QQ_GROUP_1)
                }
            },
            IconTitleEntity(R.drawable.ic_feedback_black_24dp, R.string.text_feedback) {
                showFeedbackDialog()
            },
            IconTitleEntity(R.drawable.ic_bug_report_24dp, titleId = R.string.text_browse_log) {
                var text = GlobalLog.colorHtml()
                if (BuildConfig.DEBUG && text.isEmpty()) {
                    try {
                        text = File(Environment.getExternalStorageDirectory().absolutePath + "/crash.log").readText()
                    } catch (e: Exception) {
                    }
                }

                BottomDialog.builder(this) {
                    awesomeHeader("日志")
                    cancelable(false)

                    content(SmoothTextBuilder().apply {
                        html = text
                    })

                    buttons {
                        positiveButton(text = "复制") {
                            SystemBridge.setClipText(GlobalLog.toString())
                            GlobalApp.toastInfo(R.string.text_copied)
                        }
                        negativeButton(text = "清空") {
                            GlobalLog.clear()
                            it.dismiss()
                        }
                        neutralButton(text = "导出至文件") {
                            GlobalLog.export2Sd()
                        }
                    }
                }
            }
    )

}