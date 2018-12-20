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
class ViewNodeNotFoundException(finder: ViewFinder?) : Exception(finder?.toString()?:"finder is null")