package cn.vove7.vtp.toast

import android.annotation.SuppressLint
import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import cn.vove7.vtp.R
import cn.vove7.vtp.log.Vog

/**
 *
 * lateinit var toast: Voast
 * toast = Voast.with(context).top()
 */
class Voast(
        private val context: Context,
        private val outDebug: Boolean
) {
    private lateinit var msgView: TextView
    private val animations = -1
    private val isShow = false
    private lateinit var toast: Toast
    @SuppressLint("ShowToast")

    fun init(): Voast {
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

    fun top(xOffset: Int = 0, yOffset: Int = 40): Voast {
        toast.setGravity(Gravity.TOP, xOffset, yOffset)
        return this
    }

    fun bottom(xOffset: Int = 0, yOffset: Int = 0): Voast {
        toast.setGravity(Gravity.BOTTOM, xOffset, yOffset)
        return this
    }

    fun center(xOffset: Int = 0, yOffset: Int = 0): Voast {
        toast.setGravity(Gravity.CENTER, xOffset, yOffset)
        return this
    }


    fun showLong(msg: String) {
        show(msg, Toast.LENGTH_LONG)
    }

    private fun show(msg: String, d: Int = Toast.LENGTH_SHORT) {
        if (outDebug)
            Vog.d(this, msg)
        toast.duration = d
        try {
            msgView.text = msg
        } catch (e: Exception) {
            Vog.wtf(this, e.message?:"")
        }
        toast.show()
    }

    companion object {
        fun with(context: Context, outDebug: Boolean = false): Voast {
            return Voast(context, outDebug).init()
        }
    }

}