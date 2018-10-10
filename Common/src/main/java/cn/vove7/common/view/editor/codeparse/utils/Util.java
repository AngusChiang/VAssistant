package cn.vove7.common.view.editor.codeparse.utils;

import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.Stack;

/**
 * Created by IntelliJ IDEA.
 * User: Student
 * Date: 2018/7/2
 * Time: 15:03
 */
public class Util {
    public static boolean isLetter(char c) {
        return isLowerLetter(c) || isUpperLetter(c) || c == '_';
    }

    public static boolean isUpperLetter(char c) {
        return (c >= 'A' && c <= 'Z');
    }

    public static boolean isLowerLetter(char c) {
        return (c >= 'a' && c <= 'z');
    }

    public static boolean isNumber(char c) {
        return c <= '9' && c >= '0';
    }

    public static boolean isRelationOperator(char c) {
        return c == '<' || c == '>' || c == '=';
    }

    public static boolean isArithmeticWord(char c) {
        return c == '+' || c == '-' || c == '*' || c == '/';
    }

    //public static boolean isTerminator(char c) {
    //    return (isLowerLetter(c) || isArithmeticWord(c)
    //            || isNumber(c) || isRelationOperator(c)
    //            || t.contains(c));
    //}

    private static final List<Character> t = Arrays.asList('(', ')');


    /**
     * 遍历栈
     *
     * @param stack 栈
     * @return 栈String
     */
    public static String stackElem(Stack stack) {
        StringBuilder builder = new StringBuilder();
        for (Object o : stack) {
            builder.append(o);
        }
        return builder.toString();
    }

    /**
     * 遍历队列
     *
     * @param q 队列
     * @return 队列String
     */
    public static String queueElem(Queue q) {
        StringBuilder builder = new StringBuilder();
        for (Object o : q) {
            builder.append(o);
        }
        return builder.toString();
    }
}
