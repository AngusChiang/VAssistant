package cn.vove7.jarvis.fragments


import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.TextView
import cn.vove7.common.appbus.AppBus
import cn.vove7.common.model.UserInfo
import cn.vove7.jarvis.R
import cn.vove7.jarvis.activities.AdvancedSettingActivity
import cn.vove7.jarvis.activities.PermissionManagerActivity
import cn.vove7.jarvis.activities.SettingsActivity
import cn.vove7.jarvis.view.dialog.LoginDialog
import cn.vove7.jarvis.view.dialog.UserInfoDialog
import cn.vove7.vtp.easyadapter.BaseListAdapter
import cn.vove7.vtp.log.Vog
import kotlinx.android.synthetic.main.fragment_mine.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class MineFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(code: String) {
        Vog.d(this, "onEvent ---> $code")
        if (code == AppBus.EVENT_LOGOUT) {
            loadUserInfo()
        }
    }


    lateinit var listView: ListView
    lateinit var loginView: View
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_mine, container, false)

        loginView = view.findViewById(R.id.text_login)
        view.findViewById<View>(R.id.user_name_text).setOnClickListener {
            UserInfoDialog(activity!!) {
                loadUserInfo()
            }
        }
        loginView.setOnClickListener {
            LoginDialog(context!!) {
                loadUserInfo()
            }
        }
        listView = view.findViewById(R.id.list_view)
        listView.adapter = object : BaseListAdapter<ItemHolder, Pair<Int, Int>>(context!!, listOf(
                Pair(R.color.google_blue, R.string.text_settings),
                Pair(R.color.google_red, R.string.text_advanced_features),
                Pair(cn.vove7.vtp.R.color.deep_purple_700, R.string.text_permission_manager),
                Pair(R.color.google_green, R.string.text_help),
                Pair(R.color.google_yellow, R.string.text_about)
        )) {
            override fun layoutId(): Int = R.layout.item_mine_features

            override fun onBindView(holder: ItemHolder, pos: Int, item: Pair<Int, Int>) {
                holder.leftLine.setBackgroundResource(item.first)
                holder.textView.setText(item.second)
                holder.clickBody.setOnClickListener {
                    onItemClick(pos)
                }
            }

            override fun onCreateViewHolder(view: View): ItemHolder = ItemHolder(view)
        }
        return view
    }

    override fun onResume() {
        super.onResume()
        loadUserInfo()
    }

    override fun onDestroy() {
        super.onDestroy()
        AppBus.unreg(this)
    }

    private fun loadUserInfo() {
        if (UserInfo.isLogin()) {
            login_lay.visibility = View.GONE
            user_info_lay.visibility = View.VISIBLE
            user_name_text.text = UserInfo.getUserName()
            user_vip_text.text = if (UserInfo.isVip()) {
                red_heard.visibility = View.VISIBLE
                "高级用户"
            } else {
                red_heard.visibility = View.GONE
                ""
            }
        } else {
            login_lay.visibility = View.VISIBLE
            user_info_lay.visibility = View.GONE
        }
    }

    fun onItemClick(position: Int) {
        when (position) {
            0 -> {
                startActivity(Intent(context, SettingsActivity::class.java))
            }
            1 -> {
                startActivity(Intent(context, AdvancedSettingActivity::class.java))
            }
            2 -> {
                startActivity(Intent(context, PermissionManagerActivity::class.java))
            }
            3 -> {

            }
            4 -> {

            }
            else -> {
            }
        }

    }

    class ItemHolder(v: View) : BaseListAdapter.ViewHolder(v) {
        val leftLine: View = v.findViewById(R.id.line)
        val clickBody: View = v.findViewById(R.id.click_body)

        val textView: TextView = v.findViewById(R.id.text)
    }


    companion object {

        @JvmStatic
        fun newInstance() = MineFragment().apply {
            arguments = Bundle().apply {

            }
        }
    }
}
