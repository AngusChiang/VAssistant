package cn.vove7.common.view.finder;

import java.util.Arrays;

import cn.vove7.common.R;
import cn.vove7.common.accessibility.AccessibilityApi;
import cn.vove7.common.accessibility.viewnode.ViewNode;
import cn.vove7.common.app.GlobalApp;
import cn.vove7.common.app.GlobalLog;
import cn.vove7.common.executor.CExecutorI;
import cn.vove7.vtp.log.Vog;

import static cn.vove7.common.view.finder.ViewFinderWithMultiCondition.MATCH_MODE_CONTAIN;
import static cn.vove7.common.view.finder.ViewFinderWithMultiCondition.MATCH_MODE_EQUAL;
import static cn.vove7.common.view.finder.ViewFinderWithMultiCondition.MATCH_MODE_FUZZY_WITH_PINYIN;
import static cn.vove7.common.view.finder.ViewFinderWithMultiCondition.MATCH_MODE_MATCHES;


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

    /**
     * wait使用
     *
     * @param executor CExecutorI
     */
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

    public ViewNode waitFor() {
        return waitFor(-1L);
    }

    /**
     * 需要CExecutorI
     *
     * @param m 时限
     * @return ViewNode which is returned until show in screen
     */
    public ViewNode waitFor(Long m) {
        if (executor == null) {
            Vog.INSTANCE.d(this, "执行器 null");
            return null;
        }
        if (accessibilityService != null) {
            accessibilityService.waitForView(executor, viewFinderX);
            executor.waitForUnlock(m);
            return executor.getViewNode();
        } else {
            GlobalLog.INSTANCE.err(GlobalApp.APP.getString(R.string.text_acc_service_not_running));
            return null;
        }
    }

    public ViewFindBuilder depths(Integer[] ds){
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
        viewFinderX.getViewText().addAll(Arrays.asList(text));
        viewFinderX.setTextMatchMode(MATCH_MODE_CONTAIN);
        return this;
    }

    public ViewFindBuilder containsText(String text) {
        viewFinderX.getViewText().add(text);
        viewFinderX.setTextMatchMode(MATCH_MODE_CONTAIN);
        return this;
    }

    /**
     * 正则匹配
     *
     * @param regs 表达式 %消息%
     * @return this
     */
    public ViewFindBuilder matchesText(String... regs) {
        viewFinderX.getViewText().addAll(Arrays.asList(regs));
        viewFinderX.setTextMatchMode(MATCH_MODE_MATCHES);
        return this;
    }

    public ViewFindBuilder matchesText(String regs) {
        viewFinderX.getViewText().add(regs);
        viewFinderX.setTextMatchMode(MATCH_MODE_MATCHES);
        return this;
    }


    /**
     * 相同文本 不区分大小写
     *
     * @param text text
     * @return this
     */
    public ViewFindBuilder equalsText(String... text) {
        viewFinderX.getViewText().addAll(Arrays.asList(text));
        viewFinderX.setTextMatchMode(MATCH_MODE_EQUAL);
        return this;
    }

    public ViewFindBuilder equalsText(String text) {
        viewFinderX.getViewText().add(text);
        viewFinderX.setTextMatchMode(MATCH_MODE_EQUAL);
        return this;
    }

    /**
     * 文本拼音相似度
     *
     * @param text 文本内容
     * @return this
     */
    public ViewFindBuilder similaryText(String... text) {
        viewFinderX.getViewText().addAll(Arrays.asList(text));
        viewFinderX.setTextMatchMode(MATCH_MODE_FUZZY_WITH_PINYIN);
        return this;
    }

    public ViewFindBuilder similaryText(String text) {
        viewFinderX.getViewText().add(text);
        viewFinderX.setTextMatchMode(MATCH_MODE_FUZZY_WITH_PINYIN);
        return this;
    }

    /**
     * 根据id 查找
     * @param id
     * @return
     */
    public ViewFindBuilder id(String id) {
        viewFinderX.setViewId(id);
        return this;
    }

    /**
     * 说明
     * @param desc
     * @return
     */
    public ViewFindBuilder desc(String... desc) {
        viewFinderX.getDesc().addAll(Arrays.asList(desc));
        return this;
    }

    public ViewFindBuilder desc(String desc) {
        viewFinderX.getDesc().add(desc);
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
