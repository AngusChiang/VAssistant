package cn.vove7.common.model

import cn.vove7.common.utils.runInCatch
import cn.vove7.vtp.log.Vog
import java.util.concurrent.CountDownLatch

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
    fun blockedGet(safely: Boolean = true): T? {
        if (lock.count <= 0) return mValue
        if (safely) {
            runInCatch {
                lock.await()
            }
        } else {
            lock.await()
        }
        return mValue
    }


}
