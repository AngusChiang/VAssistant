package cn.vove7.common.utils

import com.luoxudong.app.threadpool.ThreadPoolHelp
import com.luoxudong.app.threadpool.ThreadTaskObject
import java.util.concurrent.ExecutorService

/**
 * # ThreadPool
 *
 * @author Administrator
 * 2018/11/14
 */
object ThreadPool {
    private val cachePool: ExecutorService by lazy {
        ThreadPoolHelp.Builder
                .cached()
                .name("cache_pool")
                .builder()
    }

    fun runOnCachePool(runnable: Runnable) {
        cachePool.execute(runnable)
    }

    fun runOnCachePool(r: () -> Unit) {
        cachePool.execute { r.invoke() }
    }


    fun runOnPool(r: () -> Unit) {
        object : ThreadTaskObject() {
            override fun run() {
                r.invoke()
            }
        }.start()
    }


}