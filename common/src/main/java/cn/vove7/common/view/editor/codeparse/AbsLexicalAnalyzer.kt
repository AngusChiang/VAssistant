package cn.vove7.common.view.editor.codeparse


import cn.vove7.common.view.editor.codeparse.utils.Util
import java.io.File
import java.io.FileNotFoundException
import java.util.*
import java.util.regex.Pattern


/**
 * Created by Administrator.
 * Date: 2018/10/9
 */
abstract class AbsLexicalAnalyzer {
    var wordList: MutableList<Word>? = null//

    //当前行字符列下标
    private var currentCol = 0
    //当前行字符数组
    private var lineChars: CharArray? = null
    private var cLine: String? = null //当前行
    //当前行
    private var row = 1

    //单词列,当前行单词序号
    private var wordCol = 1

    //分界符
    abstract var delimiters: Map<Int, String>

    //关键字
    abstract val keyWords: Map<Int, String>
    //算数运算符
    abstract var arithmeticWords: Map<Int, String>
    //关系运算符
    abstract var relationOperators: Map<Int, String>

    /**
     * 进行分析
     *
     * @param file 输入文件
     */
    fun analysis(file: File) {
        analysis(file.readText())
    }

    fun analysis(s: String) {
        wordList = ArrayList()
        val scanner: Scanner
        try {
            scanner = Scanner(s)
        } catch (e: FileNotFoundException) {
            println("文件未找到！")
            return
        }

        row = 1
        while (scanner.hasNextLine()) {//分析行
            cLine = scanner.nextLine()
            lineChars = cLine!!.toCharArray()
            currentCol = 0
            wordCol = 1
            while (currentCol != lineChars!!.size) {
                begin()//0节点
            }
            row++
        }
    }

    fun getSpace(it: Int): String {
        var p = ""
        for (i in 0 until it)
            p += " "
        return p
    }

    /**
     * 起始节点
     */
    private fun begin() {
        while (currentCol < lineChars!!.size && lineChars!![currentCol] == ' ') {
            currentCol++
        }
        if (currentCol >= lineChars!!.size) {
            wordList!!.add(Word(getSpace(currentCol), 0, "", row, wordCol++,
                    0, currentCol))
            return
        }
        val c = lineChars!![currentCol]
        if (Util.isLetter(c)) {//分析字母关键字
            getWordOrKey()
        } else if (Util.isNumber(c)) {//分析数字
            getNumber()
        } else if (Util.isArithmeticWord(lineChars!![currentCol])) {//算数运算
            getArithmeticWord()
        } else if (Util.isRelationOperator(c)) {//关系运算
            getRelationOperator()
        } else if (delimiters.containsValue(c + "")) {
            wordList!!.add(Word(c + "", 2, c + "", row, wordCol++,
                    currentCol, currentCol + 1))
            currentCol++
        } else if (c == '"' || c == '\'') {
            parseConstStr(c)
        } else {//ERROR
            wordList!!.add(Word(c + "", 5, c + "", row, wordCol++,
                    currentCol, currentCol + 1, true))
            currentCol++
        }

        val e = currentCol
        while (currentCol < lineChars!!.size && lineChars!![currentCol] == ' ') {
            currentCol++
        }
        if (currentCol >= lineChars!!.size) {
            if (currentCol - e > 0)
                wordList!!.add(Word(getSpace(currentCol - e), 0, "", row, wordCol++,
                        0, currentCol))
        }
    }

    private fun parseConstStr(c: Char) {
        val constStrRegex = Pattern.compile(
                if (c == '\'') "'[\\S\\s]*?'"
                else "\"[\\S\\s]*?\"")
        val right = cLine!!.substring(currentCol)
        val mat = constStrRegex.matcher(right)
        if (mat.find()) {
            val s = mat.group()
            wordList!!.add(Word(s, 7, s, row, wordCol++, currentCol, currentCol + s.length))
            currentCol += s.length
        } else {
            wordList!!.add(Word(c.toString(), 7, c.toString(), row, wordCol++, currentCol, currentCol + 1, true))
            currentCol++
        }
    }

    /**
     * 获取算数运算符
     */
    private fun getArithmeticWord() {//算数运算
        val numBuilder = StringBuilder()
        while (currentCol < lineChars!!.size && Util.isArithmeticWord(lineChars!![currentCol])) {
            numBuilder.append(lineChars!![currentCol])
            currentCol++
        }
        val num = numBuilder.toString()
        if (arithmeticWords.containsValue(num)) {
            wordList!!.add(Word(num, 3, num, row, wordCol++, currentCol - 1,
                    currentCol + num.length - 1))
        } else {//ERROR
            wordList!!.add(Word(num, 3, num, row, wordCol++, currentCol - 1,
                    currentCol + num.length - 1, true))
        }
    }

    /**
     * 获取关系运算符
     */
    private fun getRelationOperator() {
        val sBuilder = StringBuilder()
        while (Util.isRelationOperator(lineChars!![currentCol])) {
            sBuilder.append(lineChars!![currentCol])
            currentCol++
        }

        val o = sBuilder.toString()
        if (relationOperators.containsValue(o)) {
            wordList!!.add(Word(o, 4, o, row, wordCol++, currentCol - 1,
                    currentCol + o.length - 1))
        } else {//ERROR
            wordList!!.add(Word(o, 4, o, row, wordCol++, currentCol - 1,
                    currentCol + o.length - 1, true))
        }
    }

    /**
     * 获取数字
     */
    private fun getNumber() {
        val numBuilder = StringBuilder()
        val b = currentCol
        while (currentCol < lineChars!!.size && (Util.isNumber(lineChars!![currentCol]) ||
                        Util.isLetter(lineChars!![currentCol]) || lineChars!![currentCol] == '.')) {
            numBuilder.append(lineChars!![currentCol])
            currentCol++
        }
        val num = numBuilder.toString()
        try {
            Integer.parseInt(num)
            wordList!!.add(Word(num, 5, num, row, wordCol++, b,
                    currentCol))
        } catch (e: NumberFormatException) {//ERROR
            //e.printStackTrace();
            wordList!!.add(Word(num, 5, num, row, wordCol++, currentCol,
                    currentCol + 1, true))
        }

    }


    /**
     * 获取标识符或关键字
     */
    private fun getWordOrKey() {
        val wordBuilder = StringBuilder()
        val b = currentCol
        while (currentCol < lineChars!!.size && (Util.isNumber(lineChars!![currentCol])
                        || Util.isLetter(lineChars!![currentCol]))) {
            wordBuilder.append(lineChars!![currentCol])
            currentCol++
        }
        val word = wordBuilder.toString()
        //单词构造完毕
        //关键字、符号
        if (keyWords.containsValue(word)) {//关键字
            wordList!!.add(Word(word, 1, word, row, wordCol++, b,
                    currentCol))
        } else {//符号
            wordList!!.add(Word(word, 6, word, row, wordCol++, b,
                    currentCol))
        }
    }


    /**
     * 输出字符表
     */
    fun printWords() {
        println(String.format("%-15s%-20s%s", "类 型", "位置（行,列）", "单词"))
        for (w in wordList!!) {
            if (w.error) {
                System.err.println(w)
            } else println(w)
        }
    }
}


