package cn.vove7.jarvis.view.floatwindows

import android.graphics.drawable.AnimationDrawable
import android.view.View
import android.view.ViewAnimationUtils
import android.view.WindowManager
import android.view.animation.AccelerateInterpolator
import android.view.animation.AnimationUtils
import cn.vove7.common.utils.gone
import cn.vove7.common.utils.listener
import cn.vove7.common.utils.runInCatch
import cn.vove7.common.utils.show
import cn.vove7.jarvis.R
import cn.vove7.jarvis.view.*
import group.infotech.drawable.dsl.corners
import group.infotech.drawable.dsl.shapeDrawable
import group.infotech.drawable.dsl.solidColor
import kotlinx.android.synthetic.main.float_panel_default.view.*

/**
 * # DefaultPanel
 *
 * @author Vove
 * 2019/10/22
 */
class DefaultPanel : FloatyPanel(
        WindowManager.LayoutParams.MATCH_PARENT,
        WindowManager.LayoutParams.WRAP_CONTENT
) {
    override fun layoutResId(): Int = R.layout.float_panel_default
    override fun onCreateView(view: View) {
        super.onCreateView(view)
        animationBody.background = buildBackground()
    }

    /**
     * 加载配置
     */
    private fun buildBackground(
            radius: Int = FloatPanelConfig.defaultPanelRadius,
            bgColor: Int = FloatPanelConfig.defaultPanelColor,
            textColor: Int = FloatPanelConfig.defaultTextColor
    ) = shapeDrawable {
        solidColor = bgColor
        corners {
            bottomLeft = radius.dp.pxf
            bottomRight = radius.dp.pxf
        }
        animationBody.voice_text.setTextColor(textColor)
        animationBody.listening_ani.setColor(textColor)
    }

    override fun showListeningAni() {
        if (contentView?.listening_ani?.isShown == true) return
        contentView?.parse_ani?.gone()
        contentView?.listening_ani?.show()
    }

    override fun showParseAni() {
        if (contentView?.parse_ani?.isShown == true) return
        contentView?.parse_ani?.apply {
            (drawable as? AnimationDrawable)?.start()
            show()
        }
        contentView?.listening_ani?.gone()
    }

    override fun showEnterAnimation() = runInCatch {
        val width = contentView?.width ?: screenWidth
        when (FloatPanelConfig.defaultPanelAnimation) {
            1 -> {//揭露动画
                ViewAnimationUtils
                        .createCircularReveal(animationBody, width / 2, 0, 0f, width.toFloat())
                        .apply {
                            interpolator = AccelerateInterpolator()
                            duration = 300
                            start()
                        }
            }
            else -> {//默认
                animationBody.startAnimation(AnimationUtils.loadAnimation(context, R.anim.pop_fade_in))
            }
        }
    }

    override fun showExitAnimation() = buildExitAnimation()

    private fun buildExitAnimation() {
        when (FloatPanelConfig.defaultPanelAnimation) {
            1 -> {//揭露动画
                startExitCircularAnimation()
            }
            else -> AnimationUtils.loadAnimation(context, R.anim.pop_fade_out).apply {
                listener {
                    onEnd {
                        if (isHiding) {
                            superRemove()
                        }
                    }
                }
                runInCatch {
                    animationBody.startAnimation(this)
                }
            }
        }
    }

    private fun startExitCircularAnimation() {
        try {
            ViewAnimationUtils.createCircularReveal(animationBody, screenWidth / 2, 0,
                    screenWidth.toFloat(), 0f)
                    .apply {
                        duration = 300
                        interpolator = AccelerateInterpolator()
                        listener {
                            onEnd {
                                if (isHiding) {
                                    superRemove()
                                }
                            }
                        }
                        start()
                    }
        } catch (e: Exception) {
            e.printStackTrace()
            superRemove()
        }
    }

    override val settingItems: Array<SettingChildItem>
        get() = arrayOf(
                SingleChoiceItem(
                        title = "动画", defaultValue = 0,
                        entityArrId = R.array.list_fp_animation,
                        keyId = R.string.key_default_fp_animation
                ),
                NumberPickerItem(
                        title = "圆角",
                        keyId = R.string.key_default_fp_radius,
                        defaultValue = { FloatPanelConfig.defaultPanelRadius },
                        range = 0..50,
                        onDialogDismiss = { hide() },
                        onChange = {
                            if (!isShowing) {
                                show("设置圆角")
                            } else runInCatch {
                                animationBody.background = buildBackground(radius = it)
                            }
                        }
                ),
                ColorPickerItem(
                        title = "背景颜色",
                        keyId = R.string.key_default_fp_color,
                        defaultValue = FloatPanelConfig.defaultPanelColor,
                        onDialogDismiss = { hide() },
                        onChange = {
                            if (!isShowing) {
                                show("设置圆角")
                            } else runInCatch {
                                animationBody.background = buildBackground(bgColor = it)
                            }
                        }
                ),
                ColorPickerItem(
                        title = "文字颜色",
                        keyId = R.string.key_default_fp_text_color,
                        defaultValue = FloatPanelConfig.defaultTextColor,
                        onDialogDismiss = { hide() },
                        onChange = {
                            if (!isShowing) {
                                show("设置圆角")
                            } else runInCatch {
                                animationBody.background = buildBackground(textColor = it)
                            }
                        }
                )
        )

}