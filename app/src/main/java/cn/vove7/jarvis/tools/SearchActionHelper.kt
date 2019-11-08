package cn.vove7.jarvis.tools

import android.os.Handler
import androidx.core.view.MenuItemCompat
import androidx.appcompat.widget.SearchView
import android.view.MenuItem
import cn.vove7.vtp.log.Vog

/**
 * Toolbar 菜单搜索框
 * @property lis Function1<String, Unit>
 * @constructor
 */
class SearchActionHelper(searchMenuItem: MenuItem, val lis: (String) -> Unit) {
    var first = true

    init {
        val searchView = MenuItemCompat.getActionView(searchMenuItem) as SearchView

        val handler = Handler()
        var sR: Runnable? = null
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                Vog.d("onQueryTextChange ----> $newText")
                handler.removeCallbacks(sR)
                if (first && newText == "") {
                    first = false
                    return true
                }
                val nt = newText.trim()
                sR = Runnable {
                    lis.invoke(nt)
                }
                handler.postDelayed(sR, 500)
                return true
            }
        })
    }
}

interface QueryListener {
    fun onQuery(text: String)
}
