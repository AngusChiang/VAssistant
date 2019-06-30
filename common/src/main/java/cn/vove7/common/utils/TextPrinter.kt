package cn.vove7.common.utils

import java.io.PrintWriter
import java.io.Writer

/**
 * 扩展Throwable 打印至TextPrinter
 * @receiver Throwable
 * @return String
 */
fun Throwable.errMessage(): String {
    val w = TextPrinter()
    printStackTrace(w)
    return w.toString()
}

/**
 * 将信息打印至stringBuffer
 *
 * @property w TextWriter
 * @constructor
 */
class TextPrinter(val w: TextWriter = TextWriter()) : PrintWriter(w) {

    override fun toString(): String {
        return w.toString()
    }
}

class TextWriter : Writer() {
    private val buffer = StringBuffer()

    val text get() = buffer.toString()

    override fun write(cbuf: CharArray?, off: Int, len: Int) {
        buffer.append(cbuf, off, len)
    }

    override fun flush() {
    }

    override fun close() {
    }

    override fun toString(): String {
        return text
    }
}