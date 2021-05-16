package cn.vove7.jarvis.activities.base

import android.os.Bundle
import android.view.MenuItem
import androidx.viewbinding.ViewBinding


/**
 * # ReturnableActivity
 *
 * @author Administrator
 * 9/24/2018
 */
abstract class ReturnableActivity<T : ViewBinding> : BaseActivity<T>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}