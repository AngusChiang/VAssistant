package cn.vove7.common.view.finder;

import java.util.List;

import cn.vove7.common.accessibility.AccessibilityApi;
import cn.vove7.common.executor.CExecutorI;
import cn.vove7.common.viewnode.ViewNode;
import cn.vove7.vtp.log.Vog;


/**
 * @author Vove
 * <p>
 * 2018/8/5
 */
public class ViewFindBuilder {
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

    private ViewFindBuilder() {
        accessibilityService =
                AccessibilityApi.Companion.getAccessibilityService();
        if (accessibilityService == null) {//没有运行
            Vog.INSTANCE.d(this, "AccessibilityService is not running.");
            return;
        }
        viewFinderX = new ViewFinderWithMultiCondition(accessibilityService);
    }

    public void waitFor() {
        if (accessibilityService != null) {
            accessibilityService.waitForView(executor, viewFinderX);
        } else {
            Vog.INSTANCE.d(this, "AccessibilityService is not running.");
        }
    }

    public ViewFindBuilder containsText(String text) {
        viewFinderX.setViewText(text);
        viewFinderX.setTextMatchMode(ViewFinderByText.MATCH_MODE_CONTAIN);
        return this;
    }

    public ViewFindBuilder equalsText(String text) {
        viewFinderX.setViewText(text);
        viewFinderX.setTextMatchMode(ViewFinderByText.MATCH_MODE_EQUAL);
        return this;
    }

    public ViewNode findFirst() {
        return viewFinderX.findFirst();
    }

    public List<ViewNode> find() {
        return viewFinderX.findAll();
    }

    public ViewFindBuilder id(String id) {
        viewFinderX.setViewId(id);
        return this;
    }

    public ViewFindBuilder desc(String desc) {
        viewFinderX.setDesc(desc);
        return this;
    }

}
