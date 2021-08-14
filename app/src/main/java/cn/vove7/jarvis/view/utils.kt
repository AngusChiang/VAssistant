package cn.vove7.jarvis.view

import com.google.android.material.textfield.TextInputLayout
import cn.vove7.common.app.GlobalApp
import cn.vove7.jarvis.R
import cn.vove7.vtp.view.DisplayUtils.*

/**
 * # Utils
 *
 * @author Administrator
 * 2018/12/30
 */

/**
 * 检查TextInputLayout编辑框内容是否空
 * @receiver TextInputLayout
 * @param errMsgWhenEmpty String
 * @return String?
 */
fun TextInputLayout.checkEmpty(
        errMsgWhenEmpty: String = context.getString(R.string.text_not_empty), trim: Boolean = true): String? {
    val s = editText?.text.toString()
    if ((if (trim) s.trim() else s).isEmpty()) {
        error = errMsgWhenEmpty
        return null
    }
    error = ""
    return s

}



/**
 * # units
 * px dp sp 单位相互转换
 * ```kotlin
 * var p = 16.dp.px  //16dp -> px
 *
 * //NoClassDefFoundError ???
 * assertEquals(p == 16.dp to PX)
 * ```
 *
 * @author Vove
 * 2019/7/31
 */


private val context get() = GlobalApp.APP

//object PX
//object SP
//object DP

class Dp(val value: Float) {
    val px: Int = dp2px(context, value)
    val pxf: Float = dp2px(context, value).toFloat()

//    infix fun to(ignore: PX): Int = px
}

class Px(val value: Int) {
    val dp: Int get() = px2dp(context, value.toFloat())
    val sp: Float get() = px2sp(context, value.toFloat()).toFloat()

//    infix fun to(ignore: SP): Float = sp
//    infix fun to(ignore: DP): Int = dp
}

class Sp(val value: Float) {
    val px: Int = sp2px(context, value)
//    infix fun to(ignore: PX): Int = px
}

val Int.dp get() = Dp(this.toFloat())
val Float.dp get() = Dp(this)
val Int.px get() = Px(this)
val Number.sp get() = Sp(this.toFloat())

