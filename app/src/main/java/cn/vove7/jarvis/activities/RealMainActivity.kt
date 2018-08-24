package cn.vove7.jarvis.activities

import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import cn.vove7.jarvis.R
import kotlinx.android.synthetic.main.activity_real_main.*

class RealMainActivity : AppCompatActivity() {

    private val mOnNavigationItemSelectedListener
            = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.nav_home -> {
                return@OnNavigationItemSelectedListener true
            }
            R.id.nav_store -> {
                return@OnNavigationItemSelectedListener true
            }
            R.id.nav_me -> {
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_real_main)

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
    }
}
