package cn.vove7.common.bridges

interface OnExecutorResult {
    fun onExecuteStart(tag: String)
    fun onExecuteFinished(result: Boolean)
    fun onExecuteFailed(errMsg: String?)
    fun onExecuteInterrupt(errMsg: String)
}
