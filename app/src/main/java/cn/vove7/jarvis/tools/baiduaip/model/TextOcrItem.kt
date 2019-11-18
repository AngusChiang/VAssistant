package cn.vove7.jarvis.tools.baiduaip.model

import java.io.Serializable
import kotlin.math.atan
import kotlin.math.sqrt

/**
 * # TextOcrItem
 *
 * @author 11324
 * 2019/3/11
 */
data class TextOcrItem(
        val text: CharSequence,
        /**
         * 坐标：左上 右上，右下，左下
         */
        val points: List<Point>,
        /**
         * 平均
         */
        val probability: Double,
        var subText: String? = null

) : Serializable {
    /**
     * 中心坐标 1,3 点中心
     */
    val centerPoint: Point
        get() = calCenterPoint(1, 3)

    val height: Int get() = calPointDistance(0, 3)

    val width: Int get() = calPointDistance(0, 1)

    val left: Int = calCenterPoint(0, 3).x
    val top: Int = calCenterPoint(0, 1).y

    private fun calCenterPoint(i: Int, j: Int): Point = Point((points[i].x + points[j].x) / 2,
            (points[i].y + points[j].y) / 2)


    private fun calPointDistance(i: Int, j: Int): Int {
        var dx = points[i].x - points[j].x
        var dy = points[i].y - points[j].y
        dx *= dx
        dy *= dy
        return sqrt((dx + dy).toDouble()).toInt()
    }

    /**
     * 旋转角度
     * 0/1 距离
     */
    val rotationAngle: Float
        get() {
            val k = (points[1].y - points[0].y).toDouble() / (points[1].x - points[0].x) //tan(x)
            return (atan(k) / Math.PI * 180).toFloat()
        }

}

data class Point(
        val x: Int,
        val y: Int
) : Serializable
