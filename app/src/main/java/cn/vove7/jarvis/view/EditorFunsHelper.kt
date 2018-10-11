package cn.vove7.jarvis.view

import android.content.Context
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import cn.vove7.common.interfaces.VApi
import cn.vove7.common.interfaces.VApi.Companion.executorFunctions
import cn.vove7.common.interfaces.VApi.Companion.executorMap
import cn.vove7.common.interfaces.VApi.Companion.runtimeFunctions
import cn.vove7.common.interfaces.VApi.Companion.runtimeMap
import cn.vove7.common.interfaces.VApi.Companion.systemFunMap
import cn.vove7.common.interfaces.VApi.Companion.systemFuncs
import cn.vove7.jarvis.R
import com.afollestad.materialdialogs.MaterialDialog

/**
 * # EditorFunsHelper
 * 编辑器api
 * @author Administrator
 * 2018/10/11
 */
typealias OnClick = (String) -> Unit

class EditorFunsHelper(
        val c: Context,
        fm: FragmentManager,
        val pager: ViewPager,
        val tabLay: TabLayout,
        val onClick: OnClick
) : VApi {
    init {
        pager.adapter = FunsFragmentAdapter(fm, c, apis, onClick)
        tabLay.setupWithViewPager(pager)
    }

    companion object {
        val apis = listOf(
//                ApiCategory("基本", listOf(
//                        ApiFunction("app", "app上下文")
//                )),
                ApiCategory("运行时", mutableListOf<ApiFunction>().also {
                    runtimeFunctions.forEach { f ->
                        it.add(ApiFunction(f, runtimeMap[f] ?: f, "runtime.$f"))
                    }
                }),
                ApiCategory("执行器", mutableListOf<ApiFunction>().also {
                    executorFunctions.forEach { f ->
                        it.add(ApiFunction(f, executorMap[f] ?: f))
                    }
                }),
                ApiCategory("ViewNode", listOf(
                        ApiFunction("tryClick()"," 尝试点击"),
                        ApiFunction("swipe(dx, dy, delay)","以此Node中心滑动到相对(dx,dy)的地方"),
                        ApiFunction("tryLongClick()","长按"),
                        ApiFunction("longClick()","长按")
                        , ApiFunction("doubleClick()","双击")
                        , ApiFunction("setText(text)","设置文本,一般只能用于可编辑控件")
                        , ApiFunction("trySetText(text)","设置文本")
                        , ApiFunction("getChilds()","获取下级所有Node,返回Array<ViewNode>")
                        , ApiFunction("getParent()","获取父级Node，返回ViewNode?")
                        , ApiFunction("getBounds()","获取边界范围")
                        , ApiFunction("getCenterPoint()","获取中心点坐标Point(x,y)(相对于本机屏幕),")
                        , ApiFunction("getText()","获取Node包含的文本")
                        , ApiFunction("select()","选择")
                        , ApiFunction("trySelect()","选择")
                        , ApiFunction("focus()","获得焦点")
                )),
                ApiCategory("ViewFinder", listOf(
                        ApiFunction("find()", "搜索所有符合条件,返回Array<ViewNode>")
                        , ApiFunction("waitFor()", "无限等待，直到搜索到，返回ViewNode")
                        , ApiFunction("waitFor(m)", "等待最长m毫秒，超时失败返回空")
                        , ApiFunction("findFirst()", "立即搜索，返回找到第一个，可能失败")
                        , ApiFunction("equalsText(texts)", "文本匹配模式：相同文本,不区分大小写")
                        , ApiFunction("similaryText(texts)", "根据文本相似度 > 0.75(中文转为拼音后的比较)")
                        , ApiFunction("id(id)", "匹配id")
                        , ApiFunction("desc(descs)", "匹配desc")
                        , ApiFunction("editable()", "匹配可编辑控件")
                        , ApiFunction("scrollable()", "匹配可滑动")
                        , ApiFunction("type(types)", "匹配控件的className")
                        , ApiFunction("await()", "同waitFor()")
                )),
                ApiCategory("全局", listOf(
                        ApiFunction("waitForId(id,m)", "等待指定视图id的出现，m:等待时长")
                        , ApiFunction("waitForDesc(desc,m)", "等待desc出现")
                        , ApiFunction("waitForText(text,m)", "等待text出现")
                        , ApiFunction("smartOpen(s)", "s:可以为应用包名，应用名，打开标记的记录")
                        , ApiFunction("smartClose(s)", "关闭应用、标记的记录")
                        , ApiFunction("sleep(m)", "睡眠m毫秒")
                        , ApiFunction("toast(msg)", "消息弹框")
                        , ApiFunction("back()", "返回操作")
                        , ApiFunction("home()", "返回主页")
                        , ApiFunction("powerDialog()", "打开电源菜单")
                        , ApiFunction("quickSettings()", "打开快捷设置(通知栏)")
                        , ApiFunction("recents()", "打开最近应用")
//                        , ApiFunction("notificationBar()","通知栏")
                        , ApiFunction("setScreenSize(width, height)",
                        "设置使用坐标操作时的屏幕相对尺寸")
                        , ApiFunction("swipe(x1, y1, x2, y2, dur)", "滑动手势，从[x1,y1] => [x2,y2] dur执行使用时间，毫秒 Android7.0+")
                        , ApiFunction("click(x, y)", "点击坐标[x,y] Android7.0+")
                        , ApiFunction("longClick(x, y)", "长按[x,y] Android7.0+")
                        , ApiFunction("gesture(dur, points)", "根据坐标数组执行手势 Android7.0+")
                        , ApiFunction("scrollDown()", "下划手势 Android7.0+")
                        , ApiFunction("scrollUp()", "上划手势 Android7.0+")
                        , ApiFunction("waitForId()", "等待指定视图id的出现，millis等待时长\n参数(id [,millis])"),
                        ApiFunction("waitForDesc()", "等待指定视图descd的出现\n参数(desc[,millis])"),
                        ApiFunction("waitForText()", "等待文本包含text的节点出现\n参数(text[,millis])")
                )),
                ApiCategory("系统", mutableListOf<ApiFunction>().also {
                    systemFuncs.forEach { f ->
                        it.add(ApiFunction(f, systemFunMap[f] ?: f, "system.$f"))
                    }
                }),
                ApiCategory("存储", listOf(//指令设置
                        ApiFunction("settings", "插入指令存储代码",
                                insertText = "\nsettings = {\n" +
                                        "  \n" +
                                        "}\n" +
                                        "\n" +
                                        "config = registerSettings(\"\", settings, 0)\n")
                        , ApiFunction("SpHelper('')","运行时存储")
                        , ApiFunction("set(k,v)","参数k,v v: int,bol,string stringset(运行时存储)")
                        , ApiFunction("getInt()","参数k")
                        , ApiFunction("getString()","参数k")
                        , ApiFunction("getBoolean()","参数k")
                )),
                ApiCategory("其他", listOf(//指令设置
                ApiFunction("toPinyin()","将text文本中的中文转换为拼音，\n参数：(text [,onlyFirstLetter])\nonlyFirstLetter是否只需要首字母")
                ,ApiFunction("matches()","匹配字符串,参数(text,regexStr)\ntext: 待匹配字符串 regex:正则式字符串 %为匹配任意字符 返回boolean 是否匹配成功")
                ,ApiFunction("matchValues()","同matches 返回匹配成功的数组\n如 用%(2)?3% 匹配1234 返回 [1,2,4] " )
                ))

        )
    }
}

class FunsFragmentAdapter(fm: FragmentManager, val c: Context, val apis: List<ApiCategory>, val onClick: OnClick)
    : FragmentPagerAdapter(fm) {

    override fun getItem(p0: Int): Fragment {
        return CateFrag.newI(apis[p0].functions, onClick)
    }

    override fun getPageTitle(position: Int): CharSequence? = apis[position].typeName

    override fun getCount(): Int = apis.size
}

class CateFrag : Fragment() {
    lateinit var functions: List<ApiFunction>
    lateinit var onClick: OnClick

    companion object {
        fun newI(functions: List<ApiFunction>, onClick: OnClick): CateFrag {
            return CateFrag().also {
                it.functions = functions
                it.onClick = onClick
            }
        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.item_api_category_rev, null)
        v.findViewById<RecyclerView>(R.id.rev).apply {
            layoutManager = GridLayoutManager(context, 3)
            adapter = GridAdapter(context!!, functions, onClick)
        }
        return v
    }
}

class GridAdapter(val c: Context, val functions: List<ApiFunction>, val onClick: OnClick) : RecyclerView.Adapter<GridAdapter.VV>() {
    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): VV {
        val v = LayoutInflater.from(c).inflate(R.layout.item_of_funcs, null)
        return VV(v)
    }

    override fun getItemCount(): Int = functions.size

    private fun showDetail(f: ApiFunction) {
        MaterialDialog(c).title(text = f.name)
                .message(text = f.doc ?: "")
                .show()
    }

    private fun getItem(p: Int): ApiFunction = functions[p]
    override fun onBindViewHolder(p0: VV, p1: Int) {
        val item = getItem(p1)
        p0.title.text = item.name
        p0.summary.text = item.summary
        p0.itemView.apply {
            setOnClickListener {
                onClick.invoke(item.insertText)
            }
            setOnLongClickListener {
                showDetail(item)
                true
            }
        }

    }

    class VV(v: View) : RecyclerView.ViewHolder(v) {
        val title = v.findViewById<TextView>(R.id.title)
        val summary = v.findViewById<TextView>(R.id.summary)
    }
}

class ApiCategory(
        val typeName: String,
        val functions: List<ApiFunction>
)

class ApiFunction(
        val name: String,
        val summary: String = name,
        val insertText: String = name,
        val doc: String = summary
)