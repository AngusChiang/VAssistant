package cn.vove7.common.executor

interface OnExecutorResult {
    fun onExecuteStart(words: String)
    fun onExecuteFinished(result: String)
    fun onExecuteFailed(errMsg: String)
}
