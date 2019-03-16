package cn.vove7.common.view.finder;

import android.util.Range;

import java.util.Arrays;

import cn.vove7.common.R;
import cn.vove7.common.accessibility.AccessibilityApi;
import cn.vove7.common.accessibility.viewnode.ViewNode;
import cn.vove7.common.app.GlobalApp;
import cn.vove7.common.app.GlobalLog;
import kotlin.Suppress;

import static cn.vove7.common.view.finder.ViewFinderWithMultiCondition.TEXT_MATCH_MODE_CONTAIN;
import static cn.vove7.common.view.finder.ViewFinderWithMultiCondition.TEXT_MATCH_MODE_EQUAL;
import static cn.vove7.common.view.finder.ViewFinderWithMultiCondition.TEXT_MATCH_MODE_FUZZY_WITH_PINYIN;
import static cn.vove7.common.view.finder.ViewFinderWithMultiCondition.TEXT_MATCH_MODE_MATCHES;


/**
 * @author Vove
 * <p>
 * 视图节点查找类
 * <p>
 * 2018/8/5
 */
public class ViewFindBuilder extends FindBuilderWithOperation {
    private ViewFinderWithMultiCondition viewFinderX;

    public ViewFinderWithMultiCondition getViewFinderX() {
        return viewFinderX;
    }

    public ViewFindBuilder() {
        AccessibilityApi accessibilityService = AccessibilityApi.Companion.getAccessibilityService();
        if (accessibilityService == null) {//没有运行
            GlobalLog.INSTANCE.err("AccessibilityService is not running.");
            return;
        }
        viewFinderX = new ViewFinderWithMultiCondition(accessibilityService);
        setFinder(viewFinderX);
    }

    @Override
    public ViewNode waitFor() {
        return waitFor(30000L);
    }

    /**
     * 需要CExecutorI
     *
     * @param m 时限
     * @return ViewNode which is returned until show in screen
     */
    @Override
    public ViewNode waitFor(long m) {
        if (viewFinderX != null) {
            return viewFinderX.waitFor(m);
        } else {
            GlobalLog.INSTANCE.err(GlobalApp.APP.getString(R.string.text_acc_service_not_running));
            return null;
        }
    }

    public ViewFindBuilder depths(Integer[] ds) {
        viewFinderX.setDepths(ds);
        return this;
    }

    /**
     * 包含文本
     *
     * @param text text
     * @return this
     */
    public ViewFindBuilder containsText(String... text) {
        viewFinderX.addViewTextCondition(text);
        viewFinderX.setTextMatchMode(TEXT_MATCH_MODE_CONTAIN);
        return this;
    }

    public ViewFindBuilder containsText(String text) {
        viewFinderX.addViewTextCondition(text);
        viewFinderX.setTextMatchMode(TEXT_MATCH_MODE_CONTAIN);
        return this;
    }

    /**
     * 正则匹配
     *
     * @param regs 表达式 %消息%
     * @return this
     */
    @Suppress(names = "unused")
    public ViewFindBuilder matchesText(String... regs) {
        viewFinderX.addViewTextCondition(regs);
        viewFinderX.setTextMatchMode(TEXT_MATCH_MODE_MATCHES);
        return this;
    }

    @Suppress(names = "unused")
    public ViewFindBuilder matchesText(String regs) {
        viewFinderX.addViewTextCondition(regs);
        viewFinderX.setTextMatchMode(TEXT_MATCH_MODE_MATCHES);
        return this;
    }


    /**
     * 相同文本 不区分大小写
     *
     * @param text text
     * @return this
     */
    public ViewFindBuilder equalsText(String... text) {
        viewFinderX.addViewTextCondition(text);
        viewFinderX.setTextMatchMode(TEXT_MATCH_MODE_EQUAL);
        return this;
    }

    public ViewFindBuilder equalsText(String text) {
        viewFinderX.addViewTextCondition(text);
        viewFinderX.setTextMatchMode(TEXT_MATCH_MODE_EQUAL);
        return this;
    }

    /**
     * 文本拼音相似度
     *
     * @param text 文本内容
     * @return this
     */
    @Suppress(names = "unused")
    public ViewFindBuilder similaryText(String... text) {
        viewFinderX.addViewTextCondition(text);
        viewFinderX.setTextMatchMode(TEXT_MATCH_MODE_FUZZY_WITH_PINYIN);
        return this;
    }

    public ViewFindBuilder similaryText(String text) {
        viewFinderX.addViewTextCondition(text);
        viewFinderX.setTextMatchMode(TEXT_MATCH_MODE_FUZZY_WITH_PINYIN);
        return this;
    }

    public ViewFindBuilder textLengthLimit(int limit) {
        viewFinderX.setTextLengthLimit(Range.create(0, limit));
        return this;
    }

    @Suppress(names = "unused")
    public ViewFindBuilder textLengthLimit(int lower, int upper) {
        viewFinderX.setTextLengthLimit(Range.create(lower, upper));
        return this;
    }


    /**
     * 根据id 查找
     *
     * @param id viewId
     * @return
     */
    public ViewFindBuilder id(String id) {
        viewFinderX.setViewId(id);
        return this;
    }

    /**
     * 说明
     *
     * @param desc
     * @return
     */
    public ViewFindBuilder desc(String... desc) {
        viewFinderX.getDescTexts().addAll(Arrays.asList(desc));
        viewFinderX.setDescMatchMode(TEXT_MATCH_MODE_EQUAL);
        return this;
    }

    public ViewFindBuilder desc(String desc) {
        viewFinderX.getDescTexts().add(desc);
        viewFinderX.setDescMatchMode(TEXT_MATCH_MODE_EQUAL);
        return this;
    }

    public ViewFindBuilder containsDesc(String desc) {
        viewFinderX.getDescTexts().add(desc);
        viewFinderX.setDescMatchMode(TEXT_MATCH_MODE_CONTAIN);
        return this;
    }

    public ViewFindBuilder containsDesc(String... desc) {
        viewFinderX.getDescTexts().addAll(Arrays.asList(desc));
        viewFinderX.setDescMatchMode(TEXT_MATCH_MODE_CONTAIN);
        return this;
    }

    public ViewFindBuilder editable() {
        return editable(true);
    }

    public ViewFindBuilder editable(boolean b) {
        viewFinderX.setEditable(b);
        return this;
    }

    public ViewFindBuilder scrollable() {
        return scrollable(true);
    }

    public ViewFindBuilder scrollable(boolean b) {
        viewFinderX.setScrollable(b);
        return this;
    }

    public ViewFindBuilder type(String type) {
        viewFinderX.getTypeNames().add(type);
        return this;
    }

    public ViewFindBuilder type(String... types) {
        viewFinderX.getTypeNames().addAll(Arrays.asList(types));
        return this;
    }

    public ViewNode await() {
        return waitFor();
    }

    public ViewNode await(long l) {
        return waitFor(l);
    }

}
