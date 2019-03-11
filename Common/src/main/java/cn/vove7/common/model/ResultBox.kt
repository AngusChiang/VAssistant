package cn.vove7.common.model

import android.os.Looper

import org.greenrobot.greendao.annotation.NotNull

import cn.vove7.vtp.log.Vog

import java.lang.Thread.sleep

/**
 * Created by Vove on 2018/7/5
 */
class ResultBox<T> {
    private var mValue: T? = null
    var has = false

    private var loop: Looper? = null

    constructor()

    @NotNull
    fun prepare(): ResultBox<T> {
        if (Looper.myLooper() == null) {
            Looper.prepare()
        }
        loop = Looper.myLooper()
        return this
    }

    constructor(mValue: T) {
        this.mValue = mValue
    }

    fun get(): T? {
        return mValue
    }

    fun loopGet(): T? {
        Looper.loop()
        Vog.d(this, "loopGet ---> get it " + mValue!!)
        return mValue
    }

    fun set(value: T) {
        this.mValue = value
    }

    fun setAndQuit(value: T) {
        this.mValue = value
        if (loop != null)
            loop!!.quitSafely()
    }

    fun setAndNotify(value: T) {
//        Vog.d(this,"setAndNotify ---> $value")
        mValue = value
        has = true
    }

    //等待结果
    fun blockedGet(): T? {
        try {
            while (!has) sleep(20)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

        return mValue
    }


}
