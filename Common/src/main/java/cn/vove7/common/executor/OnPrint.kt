package cn.vove7.common.executor

interface OnPrint {
    fun onPrint(l: Int, output: String)


    companion object {
        const val LOG = 0
        const val WARN = 1//Prompt
        const val INFO = 2
        const val ERROR = 3
    }
}
