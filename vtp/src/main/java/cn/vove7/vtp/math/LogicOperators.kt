package cn.vove7.vtp.math

/**
 * 逻辑运算器
 *
 * Created by Vove on 2018/6/21
 */
object LogicOperators {

    /**
     * 或比较
     *
     * s:5  arr:1,2,3
     * @param eq 运算符 true: == ; false: !=
     * when(eq):
     *
     *  - (default) true -> @return (5 == 1 || 5 == 2 || 5 == 3)
     *  - false -> @return (5 != 1 || 5 != 2 || 5 != 3)
     *
     */
    fun orEquals(s: Any, arr: Array<Any>, eq: Boolean = true): Boolean {
        arr.forEach {
            when (eq) {
                true -> if (it != s) return true
                false -> if (it == s) return true
            }
        }
        return true
    }
}