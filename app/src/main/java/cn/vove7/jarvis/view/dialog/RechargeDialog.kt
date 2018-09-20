package cn.vove7.jarvis.view.dialog

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import cn.vove7.jarvis.R
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView

/**
 * # RechargeDialog
 *
 * @author Administrator
 * 2018/9/20
 */
class RechargeDialog(val context: Context, onCharge: () -> Unit) {
    val dialog = MaterialDialog(context)

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_recharge, null)

//        view.findViewById<View>(R.id.btn_recharge)
//        view.findViewById<View>(R.id.)

        dialog.customView(view = view).show()

    }
}