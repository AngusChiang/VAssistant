package cn.vove7.common

import cn.vove7.common.view.finder.ViewFinder

/**
 * # exceptions
 * 异常类合集
 * @author Administrator
 * 2018/12/20
 */

/**
 * 视图搜索失败异常
 */
class ViewNodeNotFoundException(finder: ViewFinder?) : Exception(finder?.toString()
    ?: "finder is null")

/**
 * 执行出错 返回信息
 * @constructor
 */
class MessageException(msg: String?) : Exception(msg)

/**
 * 无障碍服务未运行异常
 * @constructor
 */
class NeedAccessibilityException : RuntimeException("无障碍服务未运行")

/**
 * 指令不支持的操作
 */
class NotSupportException : RuntimeException("指令无法完成该请求")

