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
import cn.vove7.common.interfaces.VApi.Companion.runtimeFunctions
import cn.vove7.common.interfaces.VApi.Companion.runtimeMap
import cn.vove7.jarvis.R
import com.afollestad.materialdialogs.MaterialDialog

/**
 * # EditorFunsHelper
 *
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
        pager.adapter = FunsFragmentAdapter(fm,c, apis, onClick)
        tabLay.setupWithViewPager(pager)
    }

    companion object {
        val apis = listOf(
                ApiCategory("基本", listOf(
                        ApiFunction("app", "app上下文")
                )),
                ApiCategory("运行时", mutableListOf<ApiFunction>().also {
                    runtimeFunctions.forEach { f ->
                        it.add(ApiFunction(f, runtimeMap[f] ?: "", "runtime.$f"))
                    }
                }),
                ApiCategory("执行器", listOf(

                )),
                ApiCategory("ViewNode", listOf(

                )),
                ApiCategory("ViewFinder", listOf(

                )),
                ApiCategory("全局", listOf(

                )),
                ApiCategory("系统", listOf(

                )),
                ApiCategory("存储", listOf(//指令设置

                )),
                ApiCategory("其他", listOf(//指令设置

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
                .message(text = f.doc?:"")
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