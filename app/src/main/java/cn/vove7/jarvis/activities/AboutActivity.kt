package cn.vove7.jarvis.activities

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.TextView
import cn.vove7.common.view.toast.ColorfulToast
import cn.vove7.executorengine.bridges.SystemBridge
import cn.vove7.jarvis.R
import cn.vove7.jarvis.adapters.IconTitleEntity
import cn.vove7.jarvis.adapters.IconTitleListAdapter
import cn.vove7.jarvis.utils.AppConfig
import cn.vove7.jarvis.view.custom.IconView
import cn.vove7.vtp.easyadapter.BaseListAdapter
import cn.vove7.vtp.system.SystemHelper
import cn.vove7.vtp.system.SystemHelper.APP_STORE_COLL_APK
import kotlinx.android.synthetic.main.activity_abc_header.*
import kotlinx.android.synthetic.main.header_about.*


/**
 * # AboutActivity
 *
 * @author Administrator
 * 9/23/2018
 */
class AboutActivity : AppCompatActivity(), AdapterView.OnItemClickListener {

    val toast: ColorfulToast by lazy {
        ColorfulToast(this).blue()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_abc_header)

        header_content.addView(layoutInflater.inflate(R.layout.header_about, null))

        ver_name_view.text = AppConfig.versionName
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        list_view.adapter = IconTitleListAdapter(this, getData())
        list_view.onItemClickListener = this
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        when (position) {
            0 -> {
                SystemHelper.openApplicationMarket(this, this.packageName, APP_STORE_COLL_APK)
            }
            1 -> {
                AppConfig.versionName
                SystemHelper.openApplicationMarket(this, this.packageName, APP_STORE_COLL_APK)
            }
            2 -> startActivity(Intent(this, OSLActivity::class.java))
            3 -> {
                SystemBridge.sendEmail("vove7@qq.com", null,
                        "\n\n\n\n\n\n\n- 来自" + getString(R.string.app_name))
            }
        }
    }

    class VH(v: View) : BaseListAdapter.ViewHolder(v) {
        val iconView = v.findViewById<IconView>(R.id.icon)!!
        val titleView = v.findViewById<TextView>(R.id.title)!!
        val subTitleView = v.findViewById<TextView>(R.id.sub_title)!!
    }

    private fun getData(): List<IconTitleEntity> {
        return listOf(
                IconTitleEntity(R.drawable.ic_favorite_border_24dp, R.string.text_favor_it)
                , IconTitleEntity(R.drawable.ic_update_24dp, R.string.text_check_for_updates)
                , IconTitleEntity(R.drawable.ic_github, R.string.text_open_source_libraries)
                , IconTitleEntity(R.drawable.ic_email_24dp, R.string.text_contact_me, R.string.text_contact_email)
        )
    }
}