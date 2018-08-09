package cn.vove7.common.view.finder;

import cn.vove7.common.accessibility.AccessibilityApi;
import cn.vove7.common.executor.CExecutorI;
import cn.vove7.common.viewnode.ViewNode;
import cn.vove7.vtp.log.Vog;

import static cn.vove7.common.view.finder.ViewFinderWithMultiCondition.MATCH_MODE_CONTAIN;
import static cn.vove7.common.view.finder.ViewFinderWithMultiCondition.MATCH_MODE_EQUAL;
import static cn.vove7.common.view.finder.ViewFinderWithMultiCondition.MATCH_MODE_FUZZY_WITH_PINYIN;


/**
 * @author Vove
 * <p>
 * 视图节点查找类
 * <p>
 * 2018/8/5
 */
public class ViewFindBuilder extends FindBuilder {
    private ViewFinderWithMultiCondition viewFinderX;

    public ViewFinderWithMultiCondition getViewFinderX() {
        return viewFinderX;
    }

    private AccessibilityApi accessibilityService;
    private CExecutorI executor;

    public ViewFindBuilder(CExecutorI executor) {
        this();
        this.executor = executor;
    }

    public ViewFindBuilder() {
        accessibilityService =
                AccessibilityApi.Companion.getAccessibilityService();
        if (accessibilityService == null) {//没有运行
            Vog.INSTANCE.d(this, "AccessibilityService is not running.");
            return;
        }
        viewFinderX = new ViewFinderWithMultiCondition(accessibilityService);
        setFinder(viewFinderX);
    }

    /**
     * 需要CExecutorI
     *
     * @return ViewNode which is returned until show in screen
     */
    public ViewNode waitFor() {
        if (executor == null) {
            Vog.INSTANCE.d(this, "执行器 null");
            return null;
        }
        if (accessibilityService != null) {
            accessibilityService.waitForView(executor, viewFinderX);
            executor.waitForUnlock(-1);
            return executor.getViewNode();
        } else {
            Vog.INSTANCE.d(this, "AccessibilityService is not running.");
            return null;
        }
    }

    /**
     * 包含文本
     *
     * @param text text
     * @return this
     */
    public ViewFindBuilder containsText(String text) {
        viewFinderX.setViewText(text);
        viewFinderX.setTextMatchMode(MATCH_MODE_CONTAIN);
        return this;
    }

    /**
     * 相同文本 不区分大小写
     *
     * @param text text
     * @return this
     */
    public ViewFindBuilder equalsText(String text) {
        viewFinderX.setViewText(text);
        viewFinderX.setTextMatchMode(MATCH_MODE_EQUAL);
        return this;
    }

    /**
     * 文本拼音相似度
     *
     * @param text 文本内容
     * @return this
     */
    public ViewFindBuilder similaryText(String text) {
        viewFinderX.setViewText(text);
        viewFinderX.setTextMatchMode(MATCH_MODE_FUZZY_WITH_PINYIN);
        return this;
    }

    public ViewFindBuilder id(String id) {
        viewFinderX.setViewId(id);
        return this;
    }

    public ViewFindBuilder desc(String desc) {
        viewFinderX.setDesc(desc);
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

    public ViewNode await() {
        return waitFor();
    }

}
