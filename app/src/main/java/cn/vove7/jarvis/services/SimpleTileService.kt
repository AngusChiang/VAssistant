package cn.vove7.jarvis.services

import android.graphics.drawable.Icon
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi

/**
 * # SimpleTileService
 * 状态栏快捷设置瓷块 简单封装
 * @author 11324
 * 2019/1/22
 */
@RequiresApi(Build.VERSION_CODES.N)
abstract class SimpleTileService : TileService() {

    private var toggleState = STATE_OFF
    abstract var activeIcon: Int
    abstract var inactiveIcon: Int

    /**
     * 初始化状态
     */
    abstract val initStatus: Boolean

    override fun onStartListening() {
        if (initStatus) {
            setActiveStatus()
        } else {
            setInactiveStatus()
        }
    }

    /**
     * 激活函数
     * @return Boolean 是否改变状态
     */
    abstract fun onActive(): Boolean

    /**
     * 取消激活
     * @return Boolean 是否改变状态
     */
    abstract fun onInactive(): Boolean

    //点击事件
    override fun onClick() {
        if (toggleState == STATE_ON) {
            if (onInactive())
                setInactiveStatus()
        } else {
            if (onActive())
                setActiveStatus()
        }
    }

    //设置未激活状态
    private fun setInactiveStatus() {
        val icon = Icon.createWithResource(applicationContext, inactiveIcon)
        qsTile.state = Tile.STATE_INACTIVE// 更改成非活跃状态

        toggleState = STATE_OFF
        qsTile.icon = icon//设置图标
        qsTile.updateTile()//更新Tile
    }

    //设置激活状态
    private fun setActiveStatus() {
        val icon = Icon.createWithResource(applicationContext, activeIcon)
        qsTile.state = Tile.STATE_ACTIVE// 更改成非活跃状态

        toggleState = STATE_ON
        qsTile.icon = icon//设置图标
        qsTile.updateTile()//更新Tile
    }

    companion object {
        private const val STATE_OFF = 0
        private const val STATE_ON = 1
    }
}