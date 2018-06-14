package cn.vove7.vtp.toast

import android.annotation.SuppressLint
import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import cn.vove7.vtp.R

/**
 *
 * lateinit var toast: VToast
 * toast = VToast.with(context).top()
 */
class VToast(val context: Context) {
    private lateinit var msgView: TextView
    private val animations = -1
    private val isShow = false
    private lateinit var toast: Toast
    @SuppressLint("ShowToast")

    fun init(): VToast {
        toast = Toast.makeText(context, "", Toast.LENGTH_SHORT)!!
        val inflate = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val v = inflate.inflate(R.layout.vtoast_layout, null)
        msgView = v.findViewById(R.id.message) as TextView
        toast.view = v


        return bottom()
    }

    fun showShort(msg: String) {
        show(msg, Toast.LENGTH_SHORT)
    }

    fun top(xOffset: Int = 0, yOffset: Int = 40): VToast {
        toast.setGravity(Gravity.TOP, xOffset, yOffset)
        return this
    }

    fun bottom(xOffset: Int = 0, yOffset: Int = 0): VToast {
        toast.setGravity(Gravity.BOTTOM, xOffset, yOffset)
        return this
    }

    fun center(xOffset: Int = 0, yOffset: Int = 0): VToast {
        toast.setGravity(Gravity.CENTER, xOffset, yOffset)
        return this
    }


    fun showLong(msg: String) {
        show(msg, Toast.LENGTH_LONG)
    }

    private fun show(msg: String, d: Int = Toast.LENGTH_SHORT) {
        toast.duration = d
        msgView.text = msg

        toast.show()
    }

    companion object {
        fun with(context: Context): VToast {
            return VToast(context).init()
        }
    }

}