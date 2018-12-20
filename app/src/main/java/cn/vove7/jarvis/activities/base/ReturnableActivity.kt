package cn.vove7.jarvis.activities.base

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import cn.vove7.common.view.toast.ColorfulToast

/**
 * # ReturnableActivity
 *
 * @author Administrator
 * 9/24/2018
 */
@SuppressLint("Registered")
open class ReturnableActivity : AppCompatActivity() {
    val toast: ColorfulToast by lazy { ColorfulToast(this).blue() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

}