package cn.vove7.plugin_test

import android.app.Activity
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast

//import cn.vassistant.plugininterface.app.GlobalApp
//import cn.vove7.vtp.log.Vog

/**
 * # MainActivity
 *
 * @author Administrator
 * 2018/11/18
 */
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<View>(R.id.button).setOnClickListener {
//            GlobalApp.toastShort("yes")
            Toast.makeText(this,"re plugin",Toast.LENGTH_SHORT).show()
        }
//        Vog.d(this,"onCreate ---> 插件Activity已启动")
    }
}