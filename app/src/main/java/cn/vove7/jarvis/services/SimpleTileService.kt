package cn.vove7.jarvis.services

import android.graphics.drawable.Icon
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import cn.vove7.common.app.GlobalApp

/**
 * # SimpleTileService
 * 状态栏快捷设置瓷块 简单封装
 * @author 11324
 * 2019/1/22
 */
@RequiresApi(Build.VERSION_CODES.N)
abstract class SimpleTileService : TileService() {

    open var toggleState: Boolean = false
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
        if (toggleState) {
            if (onInactive())
                setInactiveStatus()
        } else {
            if (onActive())
                setActiveStatus()
        }
    }

    private val appContext get() = GlobalApp.APP

    //设置未激活状态
    private fun setInactiveStatus() {
        toggleState = false

        qsTile?.apply {
            state = Tile.STATE_INACTIVE// 更改成非活跃状态
            icon = Icon.createWithResource(appContext, inactiveIcon)//设置图标
            updateTile()//更新Tile
        }
    }

    //设置激活状态
    private fun setActiveStatus() {
        toggleState = true
        qsTile?.apply {
            state = Tile.STATE_ACTIVE// 更改成非活跃状态
            icon = Icon.createWithResource(appContext, activeIcon)//设置图标
            updateTile()//更新Tile
        }

    }

}