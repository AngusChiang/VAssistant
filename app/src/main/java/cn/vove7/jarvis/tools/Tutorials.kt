package cn.vove7.jarvis.tools

import android.app.Activity
import android.content.Context
import android.graphics.Typeface
import android.view.View
import android.widget.TextView
import androidx.annotation.DrawableRes
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.utils.boundsInScreen
import cn.vove7.jarvis.R
import cn.vove7.vtp.log.Vog
import cn.vove7.vtp.sharedpreference.SpHelper
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetSequence
import com.getkeepsafe.taptargetview.TapTargetView


/**
 * # Tutorials
 *
 * @author Administrator
 * 2018/11/2
 */
object Tutorials {
    var debug = false // 必现引导

    //首页登陆
    const val T_LOGIN = "t_login"

    //帮助页
    const val h_t_1 = "h_t_1"
    const val h_t_2 = "h_t_2"
    const val h_t_3 = "h_t_3"
    const val h_t_4 = "h_t_4"
    const val h_t_5 = "h_t_5"

    //高级页
    const val t_inst_man = "t_inst_man"
    const val t_mark_man = "t_mark_man"

    //设置页
    const val t_settings_set_assist = "t_settings_set_assist"
    private val _sp: SpHelper get() = SpHelper(GlobalApp.APP, "tutorials")

    //指令详情页
    const val t_inst_detail_desc = "t_inst_detail_desc"
    const val t_inst_detail_exp = "t_inst_detail_exp"
    const val t_inst_detail_regex = "t_inst_detail_regex"
    const val t_inst_detail_run = "t_inst_detail_run"
    const val screen_translate_tips = "screen_translate_tips"
    const val screen_assistant_qrcode = "screen_assistant_qrcode"
    const val screen_assistant_spot = "screen_assistant_spot"
    const val screen_assistant_ocr = "screen_assistant_ocr2"
    const val hide_on_recent = "hide_on_recent"

    val tipsHideRecent: Boolean
        get() {
            val sp = _sp
            val b = sp.getBoolean(hide_on_recent, true)
            sp.set(hide_on_recent, false)
            return b
        }

    val context: Context
        get() = GlobalApp.APP

    fun oneStep(activity: Activity, list: Array<ItemWrap>, finish: (() -> Unit)? = null) {
        TapTargetSequence(activity).targets(
                *buildAfterShow(list)
        ).listener(object : TapTargetSequence.Listener {
            override fun onSequenceCanceled(lastTarget: TapTarget?) {
            }

            override fun onSequenceFinish() {
                finish?.invoke()
            }

            override fun onSequenceStep(lastTarget: TapTarget?, targetClicked: Boolean) {
            }
        }).start()
    }

    private fun buildAfterShow(list: Array<ItemWrap>): Array<TapTarget?> {
        val l = mutableListOf<TapTarget>()
        val sp = _sp
        list.withIndex().forEach { kv ->
            val it = kv.value
            if (!debug && sp.getBoolean(it.key)) return@forEach
            if (it.v == null) {
                Vog.e("buildAfterShow ---> view is null")
                return@forEach
            }
            sp.set(it.key, true)
            l.add(buildForView(it.v, it.title, it.desc, null))
        }
        return l.toTypedArray()
    }

    fun showForText(context: Activity, key: String, view: TextView, description: String? = null,
                    @DrawableRes iconId: Int? = null, onClick: (() -> Unit)? = null) {
        val sp = _sp
        if (!debug && sp.getBoolean(key)) return
        else sp.set(key, true)
        TapTargetView.showFor(context, // `this` is an Activity
                buildForText(view, description, iconId),
                object : TapTargetView.Listener() {
                    // The listener can listen for regular clicks, long clicks or cancels
                    override fun onTargetClick(view: TapTargetView) {
                        super.onTargetClick(view)      // This call is optional
                        onClick?.invoke()
                    }

                })
    }

    fun showForView(context: Activity, key: String, view: View, title: String, description: String? = null,
                    @DrawableRes iconId: Int? = null, onClick: (() -> Unit)? = null) {
        val sp = _sp
        if (!debug && sp.getBoolean(key)) return
        else sp.set(key, true)
        TapTargetView.showFor(context, // `this` is an Activity
                buildForView(view, title, description, iconId),
                object : TapTargetView.Listener() {
                    // The listener can listen for regular clicks, long clicks or cancels
                    override fun onTargetClick(view: TapTargetView) {
                        super.onTargetClick(view)      // This call is optional
                        onClick?.invoke()
                    }
                })
    }

    private fun buildForView(view: View, title: String, description: String?, iconId: Int?): TapTarget {
        return TapTarget.forView(view, title, description).apply {
            // All options below are optional
            outerCircleColor(R.color.google_green)      // Specify a color for the outer circle
            outerCircleAlpha(0.99f)            // Specify the alpha amount for the outer circle
//            targetCircleColor(R.color.transparent)   // Specify a color for the target circle
            titleTextSize(20)                  // Specify the size (in sp) of the title text
            titleTextColor(R.color.fff)      // Specify the color of the title text
            descriptionTextSize(12)            // Specify the size (in sp) of the description text
            descriptionTextColor(R.color.fff)  // Specify the color of the description text
            textColor(R.color.fff)            // Specify a color for both the title and description text
            textTypeface(Typeface.SANS_SERIF)  // Specify a typeface for the text
            dimColor(R.color.google_green)            // If set, will dim behind the view with 30% opacity of the given color
            drawShadow(true)                   // Whether to draw a drop shadow or not
            cancelable(false)                  // Whether tapping outside the outer circle dismisses the view
            tintTarget(true)                   // Whether to tint the target view's color
            transparentTarget(true)           // Specify whether the target is transparent (displays the content underneath)
            if (iconId != null) icon(context.resources.getDrawable(iconId))                     // Specify a custom drawable to draw as the target
            targetRadius(60) // Specify the target radius (in dp)
        }
    }

    private fun buildForText(view: TextView, description: String?, iconId: Int?): TapTarget {
        return TapTarget.forBounds(view.boundsInScreen(), view.text, description).apply {
            // All options below are optional
            outerCircleColor(R.color.google_green)      // Specify a color for the outer circle
            outerCircleAlpha(0.99f)            // Specify the alpha amount for the outer circle
            targetCircleColor(R.color.transparent)   // Specify a color for the target circle
            titleTextSize(20)                  // Specify the size (in sp) of the title text
            titleTextColor(R.color.google_green)      // Specify the color of the title text
            descriptionTextSize(12)            // Specify the size (in sp) of the description text
            descriptionTextColor(R.color.fff)  // Specify the color of the description text
            textColor(R.color.fff)            // Specify a color for both the title and description text
            textTypeface(Typeface.SANS_SERIF)  // Specify a typeface for the text
            dimColor(R.color.google_green)            // If set, will dim behind the view with 30% opacity of the given color
            drawShadow(true)                   // Whether to draw a drop shadow or not
            cancelable(false)                  // Whether tapping outside the outer circle dismisses the view
            tintTarget(true)                   // Whether to tint the target view's color
            transparentTarget(true)           // Specify whether the target is transparent (displays the content underneath)
            if (iconId != null) icon(context.resources.getDrawable(iconId))                     // Specify a custom drawable to draw as the target
            targetRadius(60) // Specify the target radius (in dp)
        }
    }

    /**
     * 重置引导
     */
    fun resetTutorials() {
        val sp = _sp
        arrayOf(T_LOGIN, h_t_1, h_t_2, h_t_3, h_t_4,
                t_inst_man, t_mark_man, t_settings_set_assist
                , t_inst_detail_desc
                , t_inst_detail_exp
                , t_inst_detail_regex
                , t_inst_detail_run
                , screen_translate_tips
                , screen_assistant_qrcode
                , screen_assistant_spot
                , screen_assistant_ocr
                , hide_on_recent
        ).forEach {
            sp.removeKey(it)
        }
    }
}

class ItemWrap(
        val key: String,
        val v: View?,
        val title: String,
        val desc: String? = null
)