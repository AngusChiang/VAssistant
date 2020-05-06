package cn.vove7.jarvis.fragments


import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.TextView
import androidx.cardview.widget.CardView
import cn.vove7.common.appbus.AppBus
import cn.vove7.common.model.UserInfo
import cn.vove7.common.net.ApiUrls
import cn.vove7.common.net.WrapperNetHelper
import cn.vove7.common.utils.color
import cn.vove7.common.utils.gone
import cn.vove7.common.utils.onClick
import cn.vove7.common.utils.show
import cn.vove7.jarvis.R
import cn.vove7.jarvis.activities.*
import cn.vove7.jarvis.services.MainService
import cn.vove7.jarvis.tools.AppLogic
import cn.vove7.jarvis.view.dialog.LoginDialog
import cn.vove7.jarvis.view.dialog.UserInfoDialog
import cn.vove7.vtp.easyadapter.BaseListAdapter
import cn.vove7.vtp.log.Vog
import kotlinx.android.synthetic.main.fragment_mine.*
import kotlinx.android.synthetic.main.fragment_mine.view.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class MineFragment : androidx.fragment.app.Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(code: String) {
        Vog.d("onEvent ---> $code")
        when (code) {
            AppBus.EVENT_USER_INIT, AppBus.EVENT_FORCE_OFFLINE, AppBus.EVENT_LOGOUT -> {
                loadUserInfo()
            }
            AppBus.EVENT_REFRESH_USER_INFO -> {
                refreshUserInfo()
            }
        }
    }

    private fun refreshUserInfo() {
        WrapperNetHelper.postJson<UserInfo>(ApiUrls.GET_USER_INFO) {
            success { _, bean ->
                if (bean.isOk()) {
                    try {
                        val userInfo = bean.data!!
                        AppLogic.onLogin(userInfo)
                        loadUserInfo()
                    } catch (e: Exception) {
                        return@success
                    }
                }
            }
        }
    }

    lateinit var listView: ListView
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_mine, container, false)
        AppBus.reg(this)

        view.top_panel.onClick {
            if (UserInfo.isLogin()) {
                UserInfoDialog(activity!!) {
                    loadUserInfo()
                }
            } else {
                LoginDialog(context!!) {
                    loadUserInfo()
                }
            }
        }
        view.fab.onClick(MainService::switchRecog)

        listView = view.findViewById(R.id.list_view)
        listView.adapter = object : BaseListAdapter<ItemHolder, Pair<Int, Int>>(context!!, listOf(
                Pair(R.color.google_blue, R.string.text_settings),
                Pair(R.color.google_red, R.string.text_advanced_features),
                Pair(cn.vove7.vtp.R.color.brown_800, R.string.text_laboratory),
                Pair(cn.vove7.vtp.R.color.deep_purple_700, R.string.text_permission_manager),
                Pair(R.color.google_green, R.string.text_help),
                Pair(R.color.google_yellow, R.string.text_about)
        )) {
            override fun layoutId(position: Int): Int = R.layout.item_mine_features

            override fun onBindView(holder: ItemHolder, pos: Int, item: Pair<Int, Int>) {
                holder.leftLine.setCardBackgroundColor(context!!.color(item.first))
                holder.textView.setText(item.second)
                holder.clickBody.setOnClickListener {
                    onItemClick(pos)
                }
            }

            override fun onCreateViewHolder(view: View): ItemHolder = ItemHolder(view)
        }
        listView.onItemClickListener = null
        return view
    }


    override fun onResume() {
        super.onResume()
        loadUserInfo()
    }

    override fun onDestroy() {
        AppBus.unreg(this)
        super.onDestroy()
    }

    private fun loadUserInfo() {
        if (UserInfo.isLogin()) {
            login_lay.visibility = View.GONE
            user_info_lay.visibility = View.VISIBLE
            user_name_text.text = UserInfo.getUserName()
            user_vip_text.text = when {
                UserInfo.isPermanentVip() -> {
                    red_heard.show()
                    "永久会员"
                }
                UserInfo.isVip() -> {
                    red_heard.show()
                    "会员用户"
                }
                else -> {
                    red_heard.gone()
                    ""
                }
            }
        } else {
            login_lay.visibility = View.VISIBLE
            user_info_lay.visibility = View.GONE
        }
    }

    private val activities
        get() = arrayOf(
                SettingsActivity::class.java,
                AdvancedSettingActivity::class.java,
                LaboratoryActivity::class.java,
                PermissionManagerActivity::class.java,
                HelpActivity::class.java,
                AboutActivity::class.java
        )

    fun onItemClick(position: Int) {
        val intent = when (position) {
            in 0..5 -> Intent(context, activities[position])
            else -> null
        }
        if (intent != null) {
            startActivity(intent)
        }

    }

    class ItemHolder(v: View) : BaseListAdapter.ViewHolder(v) {
        val leftLine: CardView = v.findViewById(R.id.line)
        val clickBody: View = v.findViewById(R.id.click_body)

        val textView: TextView = v.findViewById(R.id.text)
    }


    companion object {

        @JvmStatic
        fun newInstance() = MineFragment().apply {
            //            arguments = Bundle().apply {
//
//            }
        }
    }
}
