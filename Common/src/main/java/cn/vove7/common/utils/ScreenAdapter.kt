package cn.vove7.common.utils

import android.graphics.Point
import android.util.Pair
import cn.vassistant.plugininterface.bridges.SystemOperation

/**
 * # ScreenAdapter
 *
 * @author 17719247306
 * 2018/9/6
 */
object ScreenAdapter {

    //    companion object {
    private var mHeight = 1920// deviceInfo.screenInfo.height
    private var mWidth = 1080// deviceInfo.screenInfo.width
    fun init(systemBridge: SystemOperation) {
        val deviceInfo = systemBridge.getDeviceInfo()
        mHeight = deviceInfo.screenInfo.height
        mWidth = deviceInfo.screenInfo.width
    }
//    }

    /**
     * 相对大小
     */
    var relHeight = mHeight
    var relWidth = mWidth

    fun setScreenSize(width: Int, height: Int) {
        relHeight = height
        relWidth = width
    }

    fun getRelPoint(p: Point): Point {
        return Point(
                p.x * relWidth / mWidth,
                p.y * relHeight / mHeight
        )
    }

    fun reSet() {
        relHeight = mHeight
        relWidth = mWidth
    }


    fun scalePoints(points: Array<Pair<Int, Int>>): Array<Pair<Float, Float>> {
//        log('points size: ' + points.length)

        val ps = Array(points.size) { Pair(0f, 0f) }

        val index = 0
        points.forEach {
            val x = scaleX(it.first)
            val y = scaleY(it.second)
            ps[index] = Pair(x, y)
        }
        return ps
    }

    fun scaleX(x: Int): Float {
        return (x.toFloat() / relWidth * mWidth)
    }

    fun scaleY(y: Int): Float {
        return (y.toFloat() / relHeight * mHeight)
    }
}