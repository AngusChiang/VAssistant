package cn.vove7.jarvis.tools

import android.os.Handler
import android.support.v4.view.MenuItemCompat
import android.support.v7.widget.SearchView
import android.view.MenuItem
import cn.vove7.vtp.log.Vog

class SearchActionHelper(searchMenuItem: MenuItem, val lis: (String) -> Unit) {
    init {
        val searchView = MenuItemCompat.getActionView(searchMenuItem) as SearchView

        val handler = Handler()
        var sR: Runnable? = null
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                Vog.d(this, "onQueryTextChange ----> $newText")
                handler.removeCallbacks(sR)

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
