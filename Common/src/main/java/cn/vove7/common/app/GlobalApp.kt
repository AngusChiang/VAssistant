package cn.vove7.common.app

import android.app.Application

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
        var APP: Application? = null
    }
}