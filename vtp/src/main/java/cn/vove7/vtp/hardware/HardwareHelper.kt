package cn.vove7.vtp.hardware

import android.content.Context
import android.hardware.Camera


/**
 *
 *
 * Created by Vove on 2018/6/20
 */
object HardwareHelper {
    /**
     * 打开闪光灯
     * 需要权限：
     * android.permission.FLASHLIGHT
     * android.permission.CAMERA
     * android.hardware.camera
     * android.hardware.autofocus
     */
    fun switchFlashlight(context: Context, on: Boolean) {
        val camera: Camera
        try {
            camera = Camera.open()
        } catch (e: Exception) {
            throw e
        }
        val parameters = camera.parameters
        parameters.flashMode = if (!on) {
            Camera.Parameters.FLASH_MODE_OFF
        } else {
            Camera.Parameters.FLASH_MODE_TORCH
        }
        camera.parameters = parameters
    }


}