package cn.vove7.common.view.dialog

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import cn.vove7.vtp.R
import cn.vove7.common.R as RR

/**
 * # BaseDialog
 * > A dialog which include title and button on bottom.
 * - 标题栏:  [icon  title                ]
 * - 底部按钮: [NEUTRAL   POSITIVE NEGATIVE] 设置则显示
 *
 * - ensure include following layout in your dialog layout
 * - @layout [R.layout.dialog_header]
 * - @layout [R.layout.dialog_footer]
 *
 * Created by Vove on 2018/6/21
 */
open class BaseDialog(context: Context) : Dialog(context), DialogInterface {

    private lateinit var titleView: TextView
    private lateinit var iconView: ImageView
    private lateinit var buttonPositive: TextView
    private lateinit var buttonNegative: TextView
    private lateinit var buttonNeutral: TextView

    /**
     * 宽占屏比
     */
    var widthP: Double = 0.8
    /**
     * 高占屏比
     */
    var heightP: Double = -1.0

    var gravity: Int = Gravity.CENTER

    var title: String = ""
    var iconDrawable: Drawable? = null
    var bodyView: View? = null

    /**
     * 底部按钮
     */
    val buttonList = mutableListOf<ButtonModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        super.setContentView(RR.layout.dialog_base)
        initView()
        findViewById<FrameLayout>(RR.id.body_container).addView(bodyView)
    }

    override fun setContentView(layoutResID: Int) {
        bodyView = layoutInflater.inflate(layoutResID, null)
    }

    override fun setContentView(view: View?, params: ViewGroup.LayoutParams?) {
        bodyView = view
    }


    override fun setContentView(view: View?) {
        bodyView = view
    }

    override fun setTitle(stringId: Int) {
        setTitle(context.getString(stringId))
    }

    override fun setTitle(title: CharSequence) {
        this.title = title.toString()
    }

    private fun initView() {
        titleView = findViewById(R.id.dialog_title)
        iconView = findViewById(R.id.dialog_icon)
        buttonPositive = findViewById(R.id.dialog_button_positive)
        buttonNegative = findViewById(R.id.dialog_button_negative)
        buttonNeutral = findViewById(R.id.dialog_button_neutral)
    }

    /**
     * 设置Title左边的Icon
     */
    fun setIcon(@DrawableRes drawableId: Int): BaseDialog {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setIcon(context.resources.getDrawable(drawableId, null))
        } else
            setIcon(context.resources.getDrawable(drawableId))
        return this
    }

    fun setIcon(drawable: Drawable): BaseDialog {
        iconDrawable = drawable
        return this
    }

    fun setButton(whichButton: Int, @StringRes resId: Int, lis: View.OnClickListener): BaseDialog {
        return setButton(whichButton, context.getString(resId), lis)
    }

    fun setButton(whichButton: Int, text: String, lis: View.OnClickListener): BaseDialog {
        buttonList.add(ButtonModel(whichButton, text, lis))
        return this
    }

    private fun onSetButton() {
        buttonList.forEach {

            when (it.whichButton) {
                BUTTON_POSITIVE -> {
                    buttonPositive.text = it.text
                    buttonPositive.visibility = View.VISIBLE
                    buttonPositive.setOnClickListener { v ->
                        it.lis.onClick(v)
                        dismiss()
                    }
                }
                DialogInterface.BUTTON_NEGATIVE -> {
                    buttonNegative.text = it.text
                    buttonNegative.visibility = View.VISIBLE
                    buttonNegative.setOnClickListener { v ->
                        it.lis.onClick(v)
                        dismiss()
                    }
                }
                DialogInterface.BUTTON_NEUTRAL -> {
                    buttonNeutral.text = it.text
                    buttonNeutral.visibility = View.VISIBLE
                    buttonNeutral.setOnClickListener(it.lis)
                }
            }
        }

    }

    override fun show() {
        super.show()
        onSetButton()
        titleView.text = title
        if (iconDrawable != null) {
            iconView.visibility = View.VISIBLE
            iconView.setImageDrawable(iconDrawable)
        }
        onSetHeight()
        onSetWidth()
        findViewById<TextView>(android.R.id.title)?.visibility = View.GONE
        window!!.setGravity(gravity)
    }

    private fun onSetWidth() {
        val m = window!!.windowManager
        val d = m.defaultDisplay
        val p = window!!.attributes
        val metrics = DisplayMetrics()
        d.getMetrics(metrics)
        p.width = (metrics.widthPixels * widthP).toInt() //设置dialog的宽度为当前手机屏幕的宽度
        window!!.attributes = p
    }

    private fun onSetHeight() {
        if (heightP < 0)
            return
        val m = window!!.windowManager
        val d = m.defaultDisplay
        val p = window!!.attributes
        val metrics = DisplayMetrics()
        d.getMetrics(metrics)
        val h = metrics.heightPixels
        p.height = (h * heightP).toInt()
        window!!.attributes = p
    }

    fun setWidth(f: Double) {
        widthP = f
    }

    fun setHeight(f: Double) {
        heightP = f
    }

    fun fullScreen() {
        setWidth(1.0)
        setHeight(0.94)
        bottom()
    }

    fun bottom() {
        gravity = Gravity.BOTTOM
    }

    fun top() {
        gravity = Gravity.TOP
    }


}

class ButtonModel(var whichButton: Int, var text: String, var lis: View.OnClickListener)