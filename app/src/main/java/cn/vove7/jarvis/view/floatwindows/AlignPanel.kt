package cn.vove7.jarvis.view.floatwindows

import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.AnimationUtils
import cn.vove7.common.bridges.SystemBridge
import cn.vove7.common.utils.listener
import cn.vove7.common.utils.runInCatch
import cn.vove7.jarvis.R
import cn.vove7.jarvis.view.*
import cn.vove7.jarvis.view.tools.SettingItemHelper
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.callbacks.onDismiss
import com.afollestad.materialdialogs.customview.customView
import group.infotech.drawable.dsl.corners
import group.infotech.drawable.dsl.shapeDrawable
import group.infotech.drawable.dsl.solidColor
import kotlinx.android.synthetic.main.float_panel_align.view.*
import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar

/**
 * # AlignPanel
 * 侧边样式
 * @author Vove
 * 2020/4/20
 */
class AlignPanel : FloatyPanel(
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.WRAP_CONTENT
) {
    override val posY: Int
        get() = (FloatPanelConfig.alignVertical * SystemBridge.screenHeight).toInt()

    override val posX: Int
        get() = if (FloatPanelConfig.alignOrientation == 0) 0 else SystemBridge.screenWidth

    override fun layoutResId(): Int = R.layout.float_panel_align

    override fun onCreateView(view: View) {
        val lp = (animationBody.layoutParams as ViewGroup.MarginLayoutParams)
        if (FloatPanelConfig.alignOrientation == 0) {
            animationBody.setPadding(FloatPanelConfig.alignLeftPadding.dp.px, 0, 0, 0)
            lp.marginEnd = 10.dp.px
        } else {
            animationBody.setPadding(0, 0, FloatPanelConfig.alignLeftPadding.dp.px, 0)
            lp.marginStart = 10.dp.px
        }
        animationBody.layoutParams = lp
        animationBody.addOnLayoutChangeListener { _, _, top, _, bottom, _, oldTop, oldRight, oldBottom ->
            animationBody.background = buildBackground()
        }
    }

    private fun buildBackground(
            bgColor: Int = FloatPanelConfig.alignBgColor,
            alignOri: Int = FloatPanelConfig.alignOrientation,
            textColor: Int = FloatPanelConfig.alignTextColor

    ): Drawable = shapeDrawable {
        animationBody.voice_text.setTextColor(textColor)
        animationBody.listening_ani.setColor(textColor)

        solidColor = bgColor
        corners {
            val r = animationBody.height.toFloat() / 2
            if (alignOri == 0) {
                bottomRight = r
                topRight = r
            } else {
                topLeft = r
                bottomLeft = r
            }
        }
    }

    override fun showEnterAnimation() {
        animationBody.startAnimation(AnimationUtils.loadAnimation(context,
                if (FloatPanelConfig.alignOrientation == 0) R.anim.pop_fade_in_left_to_right
                else R.anim.pop_fade_in_right_to_left
        ).also {
            it.fillBefore = true
            it.isFillEnabled = true
        })
    }

    override fun showExitAnimation() {
        val ani = AnimationUtils.loadAnimation(context,
                if (FloatPanelConfig.alignOrientation == 0) R.anim.pop_fade_out_right_to_left
                else R.anim.pop_fade_out_left_to_right
        ).also {
            it.fillAfter = true
            it.isFillEnabled = true
        }.listener {
            onEnd {
                if (isHiding) {
                    superRemove()
                }
            }
        }
        runInCatch {
            animationBody.startAnimation(ani)
        }
    }

    override val settingItems: Array<SettingChildItem>
        get() = arrayOf(
                SingleChoiceItem(
                        title = "依靠方向", defaultValue = 0,
                        items = listOf("左", "右"),
                        keyId = R.string.key_align_fp_orientation
                ),
                IntentItem(title = "垂直距离", summary = "距离顶部${(FloatPanelConfig.alignVertical * 100).toInt()}%") { io ->
                    show("设置垂直距离")
                    var alignVertical = FloatPanelConfig.alignVertical
                    MaterialDialog(io.itemHelper.context).show {
                        title(text = "设置垂直距离")
                        positiveButton(text = "确定") {
                            FloatPanelConfig.alignVertical = alignVertical
                        }
                        negativeButton(text = "取消")
                        onDismiss {
                            this@AlignPanel.hide()
                        }
                        val vv = SettingItemHelper.buildNumberPickerView(context,
                                0 to 10000, (FloatPanelConfig.alignVertical * 10000).toInt())
                        customView(view = vv.first)

                        vv.second.setOnProgressChangeListener(object : DiscreteSeekBar.OnProgressChangeListener {
                            override fun onProgressChanged(seekBar: DiscreteSeekBar?, value: Int, fromUser: Boolean) {
                                if (fromUser) {
                                    alignVertical = value.toFloat() / 10000
                                    val y = (alignVertical * SystemBridge.screenHeight).toInt()
                                    updatePoint(posX, y)
                                }
                            }

                            override fun onStartTrackingTouch(seekBar: DiscreteSeekBar?) {}
                            override fun onStopTrackingTouch(seekBar: DiscreteSeekBar?) {}
                        })
                    }
                },
                ColorPickerItem(
                        title = "背景颜色",
                        keyId = R.string.key_align_fp_bg_color,
                        defaultValue = FloatPanelConfig.alignBgColor,
                        onDialogShow = {
                            show("设置背景色")
                        },
                        onDialogDismiss = { hide() },
                        onChange = {
                            runInCatch {
                                animationBody.background = buildBackground(bgColor = it)
                            }
                        }
                ),
                ColorPickerItem(
                        title = "文字颜色",
                        keyId = R.string.key_align_fp_text_color,
                        defaultValue = FloatPanelConfig.alignTextColor,
                        onDialogShow = {
                            show("设置文字颜色")
                        },
                        onDialogDismiss = { hide() },
                        onChange = {
                            runInCatch {
                                animationBody.background = buildBackground(textColor = it)
                            }
                        }
                ),
                NumberPickerItem(
                        title = "边距",
                        keyId = R.string.key_align_fp_left_padding,
                        range = 5 to 20,
                        defaultValue = { FloatPanelConfig.alignLeftPadding },
                        onDialogShow = { show("设置边距") },
                        onDialogDismiss = { hide() },
                        onChange = {
                            runInCatch {
                                val lp = (animationBody.layoutParams as ViewGroup.MarginLayoutParams)
                                if (FloatPanelConfig.alignOrientation == 0) {
                                    animationBody.setPadding(it.dp.px, 0, 0, 0)
                                    lp.marginEnd = 10.dp.px
                                } else {
                                    animationBody.setPadding(0, 0, it.dp.px, 0)
                                    lp.marginStart = 10.dp.px
                                }
                                animationBody.layoutParams = lp
                            }
                        }
                )
        )

}