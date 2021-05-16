package cn.vove7.jarvis.fragments


import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import cn.vove7.common.appbus.AppBus
import cn.vove7.common.model.UserInfo
import cn.vove7.common.utils.*
import cn.vove7.jarvis.R
import cn.vove7.jarvis.activities.*
import cn.vove7.jarvis.app.AppApi
import cn.vove7.jarvis.databinding.FragmentMineBinding
import cn.vove7.jarvis.lifecycle.LifecycleScope
import cn.vove7.jarvis.services.MainService
import cn.vove7.jarvis.tools.AppLogic
import cn.vove7.jarvis.view.dialog.LoginDialog
import cn.vove7.jarvis.view.dialog.UserInfoDialog
import cn.vove7.vtp.easyadapter.BaseListAdapter
import cn.vove7.vtp.log.Vog
import kotlinx.coroutines.launch
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

    private fun refreshUserInfo() = LifecycleScope(lifecycle).launch {
        kotlin.runCatching {
            AppApi.getUserInfo()
        }.onSuccessMain { bean ->
            if (bean.isOk()) {
                bean.data?.also {
                    AppLogic.onLogin(it)
                    loadUserInfo()
                }
            }
        }
    }

    lateinit var viewBinding: FragmentMineBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        viewBinding = FragmentMineBinding.inflate(inflater, container, false)
        AppBus.reg(this)

        viewBinding.topPanel.onClick {
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
        viewBinding.fab.onClick(MainService::switchRecog)

        viewBinding.listView.adapter = object : BaseListAdapter<ItemHolder, Pair<Int, Int>>(context!!, listOf(
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
        viewBinding.listView.onItemClickListener = null
        return viewBinding.root
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
            viewBinding.loginLay.visibility = View.GONE
            viewBinding.userInfoLay.visibility = View.VISIBLE
            viewBinding.userNameText.text = UserInfo.getUserName()
            viewBinding.userVipText.text = when {
                UserInfo.isPermanentVip() -> {
                    viewBinding.redHeard.show()
                    "永久会员"
                }
                UserInfo.isVip() -> {
                    viewBinding.redHeard.show()
                    "会员用户"
                }
                else -> {
                    viewBinding.redHeard.gone()
                    ""
                }
            }
        } else {
            viewBinding.loginLay.show()
            viewBinding.userInfoLay.gone()
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
}
