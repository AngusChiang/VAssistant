package cn.vove7.common.app

import android.app.Application
import android.support.annotation.StringRes

/**
 * # GlobalApp
 *
 * @author 17719
 * 2018/8/8
 */

open class GlobalApp : Application() {
    override fun onCreate() {
        super.onCreate()
        APP = this
    }

    companion object {
        lateinit var APP: Application
        fun getString(@StringRes id: Int): String = APP.getString(id)
    }
}