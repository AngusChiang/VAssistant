package cn.vove7.jarvis.activities

import android.support.v4.app.Fragment
import cn.vove7.executorengine.bridges.SystemBridge
import cn.vove7.jarvis.activities.base.OneFragmentActivity
import cn.vove7.jarvis.adapters.ListViewModel
import cn.vove7.jarvis.adapters.SimpleListAdapter
import cn.vove7.jarvis.fragments.SimpleListFragment

/**
 * # OSLActivity
 *
 * @author Administrator
 * 2018/10/4
 */
class OSLActivity : OneFragmentActivity() {

    override var fragments: Array<Fragment> = arrayOf(ListFragment())

    class ListFragment : SimpleListFragment<OslItem>() {

        override fun unification(it: OslItem): ListViewModel<OslItem> {
            return ListViewModel(it.name, it.desc.let { s -> if (s == "") it.url else s }, extra = it)
        }

        override val itemClickListener: SimpleListAdapter.OnItemClickListener<OslItem> =
            object : SimpleListAdapter.OnItemClickListener<OslItem> {
                override fun onClick(holder: SimpleListAdapter.VHolder?, pos: Int, item: ListViewModel<OslItem>) {
                    val it = item.extra
                    if (it.url != null)
                        SystemBridge.openUrl(it.url)
                }
            }

        override fun onLoadData(pageIndex: Int) {
            val l = listOf(
                    OslItem("GreenDao", "https://github.com/greenrobot/greenDAO", "greenDAO is an open source Android ORM making development for SQLite databases fun again. It relieves developers from dealing with low-level database requirements while saving development time.")
                    , OslItem("EventBus", "https://github.com/greenrobot/EventBus", "EventBus is an open-source library for Android and Java using the publisher/subscriber pattern for loose coupling. EventBus enables central communication to decoupled classes with just a few lines of code – simplifying the code, removing dependencies, and speeding up app development.")
                    , OslItem("OkHttp", "https://github.com/square/okhttp", "HTTP is the way modern applications network. It’s how we exchange data & media. Doing HTTP efficiently makes your stuff load faster and saves bandwidth.")
                    , OslItem("Gson", "https://github.com/google/gson", "Gson is a Java library that can be used to convert Java Objects into their JSON representation. It can also be used to convert a JSON string to an equivalent Java object. Gson can work with arbitrary Java objects including pre-existing objects that you do not have source-code of.")
                    , OslItem("AndroLua", "https://github.com/L-JINBIN/AndroidLua", "AndroLua is the Lua interpreter ported to the Android platform.")
                    , OslItem("AndroLua_pro", "https://github.com/nirenr/AndroLua_pro", "")
                    , OslItem("rhino-android", "https://github.com/F43nd1r/rhino-android", "")
                    , OslItem("VTP", "https://github.com/Vove7/VTP", "个人开发工具包")
                    , OslItem("Material Dialogs", "https://github.com/afollestad/material-dialogs", "")
                    , OslItem("DiscreteSeekBar", "https://github.com/AnderWeb/discreteSeekBar", "")
                    , OslItem("AndroidDonate", "https://github.com/didikee/AndroidDonate", "")
                    , OslItem("CodeView (Android)", "https://github.com/kbiakov/CodeView-android", "CodeView helps to show code content with syntax highlighting in native way.")
                    , OslItem("Jsoup", "https://jsoup.org/", "jsoup is a Java library for working with real-world HTML. It provides a very convenient API for extracting and manipulating data, using the best of DOM, CSS, and jquery-like methods.")
                    , OslItem("TapTargetView", "https://github.com/KeepSafe/TapTargetView", "")
                    , OslItem("BGAQRCode-Android", "https://github.com/bingoogolapple/BGAQRCode-Android", "")
                    , OslItem("Glide", "https://bumptech.github.io/glide/", "Glide is a fast and efficient image loading library for Android focused on smooth scrolling. Glide offers an easy to use API, a performant and extensible resource decoding pipeline and automatic resource pooling.")
                    , OslItem("RePlugin", "https://github.com/Qihoo360/RePlugin", "RePlugin is a complete Android plug-in solution which is suitable for general use.")
                    , OslItem("apk-parser", "https://github.com/hsiafan/apk-parser", "Apk parser lib, for decoding binary xml file, getting apk meta info.")
                    , OslItem("Recycler Fast Scroll", "https://github.com/plusCubed/recycler-fast-scroll", "")
                    , OslItem("MarkdownView", "https://github.com/tiagohm/MarkdownView", "Android library to display markdown text.")
                    // , OslItem("", "", "")
            )
            notifyLoadSuccess(l, true)
        }
    }
}

class OslItem(
        val name: String,
        val url: String? = null,
//        val author: String? = null,
//        val lisType: String?,
//        val lisString: String? = null,
        val desc: String?

)