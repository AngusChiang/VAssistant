package cn.vove7.paramregexengine

object Vog {
    /**
     * @return Logger String:函数 文件 函数 信息
     */
    private val callerInfo: String
        get() {
            val caller = findCaller()!!
            return (caller.methodName + "("
                    + caller.fileName + ":" + caller.lineNumber + ")")
        }

    fun d(str: Any?) {
        try {
            println("INFO  "+ callerInfo + " - " + str.toString())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun e(e: Throwable, str: Any?) {
        e(str.toString() + e.message)
    }

    fun e(str: Any?) {
        try {
            println("ERROR " + callerInfo + " - " + str.toString())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun findCaller(): StackTraceElement? {
        // 获取堆栈信息
        val callStack = Thread.currentThread().stackTrace
        // 最原始被调用的堆栈信息
        // 日志类名称
        val logClassName = Vog::class.java.name
        // 循环遍历到日志类标识
        var i = 0
        val len = callStack.size
        while (i < len) {
            if (logClassName == callStack[i].className)
                break
            i++
        }
        return try {
            callStack[i + 3]
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

}
