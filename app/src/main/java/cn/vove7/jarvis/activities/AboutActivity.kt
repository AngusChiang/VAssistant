package cn.vove7.jarvis.activities

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.TextView
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.bridges.SystemBridge
import cn.vove7.common.utils.gone
import cn.vove7.common.utils.show
import cn.vove7.jarvis.R
import cn.vove7.jarvis.adapters.IconTitleEntity
import cn.vove7.jarvis.adapters.IconTitleListAdapter
import cn.vove7.jarvis.tools.AppConfig
import cn.vove7.jarvis.view.custom.IconView
import cn.vove7.jarvis.view.dialog.ProgressDialog
import cn.vove7.jarvis.view.dialog.UpdateLogDialog
import cn.vove7.vtp.easyadapter.BaseListAdapter
import cn.vove7.vtp.system.SystemHelper
import cn.vove7.vtp.system.SystemHelper.APP_STORE_COLL_APK
import kotlinx.android.synthetic.main.activity_abc_header.*
import kotlinx.android.synthetic.main.header_about.*
import nl.dionsegijn.konfetti.models.Shape
import nl.dionsegijn.konfetti.models.Size


/**
 * # AboutActivity
 *
 * @author Administrator
 * 9/23/2018
 */
class AboutActivity : AppCompatActivity(), AdapterView.OnItemClickListener {

    var clickTime = 0L
    var clickCount = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_abc_header)

        header_content.addView(layoutInflater.inflate(R.layout.header_about, null))

        root.setOnClickListener {
            val now = System.currentTimeMillis()
            if (now - clickTime > 1000) {//开始点击
                clickTime = now
                clickCount = 0
            }
            if (++clickCount > 4) {
                clickTime = 0
                showEgg()
            }
        }
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

    override fun onBackPressed() {
        if (konfettiView.visibility == View.VISIBLE) {
            konfettiView.gone()
        } else super.onBackPressed()
    }

    private fun showEgg() {//彩蛋
        konfettiView.show()
        konfettiView.build()
                .addColors(
                        resources.getColor(R.color.google_green),
                        resources.getColor(R.color.google_red),
                        resources.getColor(R.color.google_blue),
                        resources.getColor(R.color.google_yellow)
                )
                .setDirection(0.0, 359.0)
                .setSpeed(1f, 3f)
                .setFadeOutEnabled(true)
                .setTimeToLive(5000L)
                .addShapes(Shape.RECT, Shape.CIRCLE)
                .addSizes(Size(12))
                .setPosition(-50f, konfettiView.width + 50f, -50f, -50f)
                .streamFor(300, 7000L)


    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        when (position) {
            0 -> {
                SystemHelper.openApplicationMarket(this, this.packageName, APP_STORE_COLL_APK)
            }
            1 -> {
                val p = ProgressDialog(this) {
                    GlobalApp.toastError("检查失败")
                }
                AppConfig.checkAppUpdate(this, true) {
                    p.dismiss()
                    if (!it) {
                        GlobalApp.toastSuccess("未发现新版本")
                    }
                }
            }
            2 -> {
                SystemBridge.openUrl("https://github.com/Vove7/VOSP")
            }
            3 -> startActivity(Intent(this, OSLActivity::class.java))
            4 -> {
                SystemBridge.sendEmail("vove7@qq.com", null,
                        "\n\n\n\n\n\n\n- 来自" + getString(R.string.app_name))
            }
            5 -> {
                UpdateLogDialog(this)
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
                , IconTitleEntity(null, R.string.text_vosp, R.string.text_vosp_summary)
                , IconTitleEntity(R.drawable.ic_github, R.string.text_open_source_libraries)
                , IconTitleEntity(R.drawable.ic_email_24dp, R.string.text_contact_me, R.string.text_contact_email)
                , IconTitleEntity(R.drawable.ic_update_24dp, R.string.text_update_log)
        )
    }
}

