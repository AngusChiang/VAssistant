package cn.vove7.common.view.notifier

import cn.vove7.common.accessibility.viewnode.ViewNode
import cn.vove7.common.view.finder.ViewFinder
import cn.vove7.vtp.app.AppInfo
import cn.vove7.vtp.log.Vog

/**
 * # AppAdBlockNotifier
 *
 * @author 17719247306
 * 2018/9/3
 */
@Deprecated("弃用")
class AppAdBlockNotifier(private val app: AppInfo?, finders: MutableSet<ViewFinder>)
    : AbsViewShowNotifier(finders) {

    override fun onShow(finder: ViewFinder, node: ViewNode): Boolean {
        Vog.i("${Thread.currentThread()} 发现广告 ---> $app $finder")
        return node.tryClick().also {
            Vog.i("Ad click ---> $it")
        }
    }

    override fun onFinish(removeList: MutableList<ViewFinder>) {
        //do nothing
    }

}