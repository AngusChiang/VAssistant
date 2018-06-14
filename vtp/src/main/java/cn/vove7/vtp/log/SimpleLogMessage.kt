package cn.vove7.vtp.log

/**
 * # SimpleLogMessage
 *
 * Created by Vove on 2018/6/13
 */
data class SimpleLogMessage(
        /**
         * 手机型号
         */
        val mobileModel: String,
        /**
         * 系统版本
         */
        val osVersion: String,
        /**
         * 日志时间
         */
        val dateTime: String,

        val message: String

)