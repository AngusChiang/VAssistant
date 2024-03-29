package cn.vove7.jarvis.activities

import android.content.ComponentName
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ListView
import android.widget.TextView
import cn.vove7.bottomdialog.BottomDialog
import cn.vove7.bottomdialog.builder.buttons
import cn.vove7.bottomdialog.extension.awesomeHeader
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.app.GlobalLog
import cn.vove7.common.bridges.SystemBridge
import cn.vove7.common.net.ApiUrls
import cn.vove7.common.utils.span
import cn.vove7.jarvis.BuildConfig
import cn.vove7.jarvis.R
import cn.vove7.jarvis.activities.base.ReturnableActivity
import cn.vove7.jarvis.adapters.IconTitleEntity
import cn.vove7.jarvis.adapters.IconTitleListAdapter
import cn.vove7.jarvis.databinding.ActivityAbcHeaderBinding
import cn.vove7.jarvis.tools.ItemWrap
import cn.vove7.jarvis.tools.Tutorials
import cn.vove7.jarvis.view.dialog.contentbuilder.SmoothTextBuilder
import cn.vove7.jarvis.view.dialog.contentbuilder.markdownContent
import cn.vove7.jarvis.view.positiveButtonWithColor
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.google.android.material.textfield.TextInputLayout
import java.io.File

/**
 * # HelpActivity
 *
 * @author Administrator
 * 9/23/2018
 */
class HelpActivity : ReturnableActivity<ActivityAbcHeaderBinding>() {

    override val darkTheme: Int
        get() = R.style.DarkTheme
    lateinit var adapter: IconTitleListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding.headerContent.addView(layoutInflater.inflate(R.layout.header_help, null))

        viewBinding.listView.adapter = IconTitleListAdapter(this, getData()).also { adapter = it }
        viewBinding.listView.setOnItemLongClickListener { _, _, position, _ ->
            if (position == 5) {
                GlobalLog.export2Sd()
                return@setOnItemLongClickListener true
            }
            return@setOnItemLongClickListener false
        }
        startTutorials()
    }

    private fun startTutorials() {
        viewBinding.listView.post {
            Tutorials.oneStep(this, list = arrayOf(
                    ItemWrap(Tutorials.h_t_1, adapter.holders[0]?.titleView, getString(R.string.text_user_manual), "自定义指令、脚本Api、远程调试都在这里"),
                    ItemWrap(Tutorials.h_t_2, adapter.holders[1]?.titleView, getString(R.string.text_hot_key_desc), "快速了解快捷键操作"),
                    ItemWrap(Tutorials.h_t_5, adapter.holders[2]?.titleView, getString(R.string.text_faq), "遇到问题可先查看常见问题再进行反馈"),
                    ItemWrap(Tutorials.h_t_3, adapter.holders[4]?.titleView, getString(R.string.text_feedback), "在这里进行反馈与建议，或者加入QQ群"),
                    ItemWrap(Tutorials.h_t_4, adapter.holders[5]?.titleView, getString(R.string.text_browse_log), "当出现问题时，或非预想结果时可提供日志给作者"),
            ))
        }
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
                    awesomeHeader("新用户必读")
                    markdownContent {
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
                    markdownContent {
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
                        positiveButtonWithColor("复制") {
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