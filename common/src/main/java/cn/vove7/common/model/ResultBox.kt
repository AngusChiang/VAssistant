package cn.vove7.common.model

import cn.vove7.common.utils.runInCatch
import cn.vove7.vtp.log.Vog
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Created by Vove on 2018/7/5
 */
class ResultBox<T> {
    private var mValue: T? = null

    var lock = CountDownLatch(1)

    constructor()

    constructor(initValue: T) {
        this.mValue = initValue
    }

    fun setAndNotify(value: T) {
        Vog.d("setAndNotify ---> $value")
        mValue = value
        lock.countDown()
    }

    //等待结果
    @Throws(InterruptedException::class)
    fun blockedGet(safely: Boolean = true, millis: Long? = null): T? {
        if (lock.count <= 0) return mValue
        fun wait() {
            if (millis != null) lock.await(millis, TimeUnit.MILLISECONDS)
            else lock.await()
        }
        if (safely) runInCatch { wait() }
        else wait()
        return mValue
    }


}
