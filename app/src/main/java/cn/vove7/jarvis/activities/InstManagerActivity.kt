package cn.vove7.jarvis.activities

import android.support.v4.app.Fragment
import android.view.Menu
import android.view.MenuItem
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.bridges.SystemBridge
import cn.vove7.common.datamanager.DaoHelper
import cn.vove7.common.datamanager.parse.DataFrom
import cn.vove7.common.datamanager.parse.statusmap.ActionNode
import cn.vove7.common.model.UserInfo
import cn.vove7.common.utils.putArgs
import cn.vove7.common.utils.startActivity
import cn.vove7.jarvis.R
import cn.vove7.jarvis.activities.base.BaseActivityWithViewPager
import cn.vove7.jarvis.fragments.GlobalInstListFragment
import cn.vove7.jarvis.fragments.InstAppListFragment
import cn.vove7.jarvis.fragments.SimpleListFragment
import cn.vove7.jarvis.tools.SearchActionHelper
import cn.vove7.vtp.net.GsonHelper
import kotlinx.android.synthetic.main.activity_base_view_pager.*

/**
 * 命令管理
 */
class InstManagerActivity : BaseActivityWithViewPager() {

    override var titleInit: String? = "指令管理"

    override var fragments: Array<Fragment> = arrayOf(
            GlobalInstListFragment(),
            InstAppListFragment()
    )

    override fun onCreateOptionsMenu(m: Menu?): Boolean {
        toolbar.apply {
            inflateMenu(R.menu.menu_sync)
            menu.add("从剪切板导入")
            menu.add("新建指令教程")
            SearchActionHelper(menu!!.findItem(R.id.menu_item_search)) { text ->
                (currentFragment as SimpleListFragment<*>).search(text)
            }
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when {
            item?.itemId == R.id.menu_item_sync -> {//同步
                val p = view_pager.currentItem
                val f = fragments[p] as OnSyncInst
                f.onSync()
                return true
            }
            item?.title == "新建指令教程" -> {
                SystemBridge.openUrl("https://vove.gitee.io/2019/01/29/Customize_Instruction_Regex/")
            }
            item?.title == "从剪切板导入" -> {
                if (!UserInfo.isLogin()) {
                    GlobalApp.toastInfo("请登录后操作")
                    return true
                }
                try {
                    val node = GsonHelper.fromJson<ActionNode>(SystemBridge.getClipText())!!
                    node.id = -1
                    val id = DaoHelper.insertNewActionNode(node) ?: throw Exception("数据保存失败")
                    node.from = DataFrom.FROM_USER
                    node.publishUserId = UserInfo.getUserId()

                    GlobalApp.toastSuccess("导入完成")
                    startActivity<InstDetailActivity> {
                        putArgs("nodeId" to id)
                    }
                } catch (e: Throwable) {
                    GlobalApp.toastError("导入失败(请确保内容完整)\n${e.message}")
                }

            }
        }
        return super.onOptionsItemSelected(item)

    }

    override var titles: Array<String> = arrayOf("全局指令", "应用内指令")
}

interface OnSyncInst {
    fun onSync()
}
