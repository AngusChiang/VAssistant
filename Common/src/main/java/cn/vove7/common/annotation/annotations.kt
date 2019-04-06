package cn.vove7.common.annotation

/**
 * # annotations
 *
 * @author 11324
 * 2019/3/14
 */
/**
 * 脚本类
 * @property name 类别
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS, AnnotationTarget.ANNOTATION_CLASS)
annotation class ScriptApiClass(
        val name: String = "_",
        val prefix: String = ""//前缀 pre.fun()
)


/**
 * 脚本引擎函数接口
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.FIELD)
annotation class ScriptApi(
        val summary: String = "",
        val deprecatedVersion: String = ""
)

/**
 * 接口文档
 * @property document String
 * @constructor
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class ScriptApiDocument(
        val document: String = ""
)

/**
 * 函数参数说明
 * @property description String
 * @constructor
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class ScriptApiParamDesc(
        val description: Array<String>,
        val optional: Boolean = false
)
