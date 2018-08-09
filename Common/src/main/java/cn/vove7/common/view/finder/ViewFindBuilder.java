package cn.vove7.common.view.finder;

import android.os.Build;
import android.support.annotation.RequiresApi;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import cn.vove7.common.accessibility.AccessibilityApi;
import cn.vove7.common.executor.CExecutorI;
import cn.vove7.common.viewnode.ViewNode;
import cn.vove7.common.viewnode.ViewOperation;
import cn.vove7.vtp.log.Vog;

import static cn.vove7.common.view.finder.ViewFinderWithMultiCondition.MATCH_MODE_CONTAIN;
import static cn.vove7.common.view.finder.ViewFinderWithMultiCondition.MATCH_MODE_EQUAL;
import static cn.vove7.common.view.finder.ViewFinderWithMultiCondition.MATCH_MODE_FUZZY_WITH_PINYIN;


/**
 * @author Vove
 *
 * 视图节点查找类
 * <p>
 * 2018/8/5
 */
public class ViewFindBuilder implements ViewOperation {
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
    }

    /**
     * 需要CExecutorI
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

    /**
     * 找到第一个
     * @return ViewNode
     */
    public ViewNode findFirst() {
        return viewFinderX.findFirst();
    }

    /**
     *
     * @return list
     */
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

    @Override
    public boolean tryClick() {
        ViewNode node = findFirst();
        return node != null && node.tryClick();
    }

    @Override
    public boolean click() {
        ViewNode node = findFirst();
        return node != null && node.tryClick();
    }

    @Override
    public boolean longClick() {
        ViewNode node = findFirst();
        return node != null && node.longClick();
    }

    @Override
    public boolean doubleClick() {
        ViewNode node = findFirst();
        return node != null && node.doubleClick();
    }

    @Override
    public boolean tryLongClick() {
        ViewNode node = findFirst();
        return node != null && node.tryLongClick();
    }

    @Override
    public boolean select() {
        ViewNode node = findFirst();
        return node != null && node.select();
    }

    @Override
    public boolean trySelect() {
        ViewNode node = findFirst();
        return node != null && node.trySelect();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public boolean scrollUp() {
        ViewNode node = findFirst();
        try {
            return node != null && node.scrollUp();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public boolean scrollDown() {
        ViewNode node = findFirst();
        try {
            return node != null && node.scrollDown();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean setText(@NotNull String text, @Nullable String ep) {
        ViewNode node = findFirst();
        return node != null && node.setText(text, ep);
    }

    @Override
    public boolean setText(@NotNull String text) {
        ViewNode node = findFirst();
        return node != null && node.setText(text);
    }

    @Override
    public boolean setTextWithInitial(@NotNull String text) {
        ViewNode node = findFirst();
        return node != null && node.setTextWithInitial(text);
    }

    @Override
    public boolean trySetText(@NotNull String text) {
        ViewNode node = findFirst();
        return node != null && node.trySetText(text);
    }

    @Nullable
    @Override
    public String getText() {
        ViewNode node = findFirst();
        return node != null ? node.getText() : null;
    }

    @Override
    public boolean focus() {
        ViewNode node = findFirst();
        return node != null && node.focus();
    }

    @Override
    public boolean scrollForward() {
        ViewNode node = findFirst();
        return node != null && node.scrollForward();
    }

    @Override
    public boolean scrollBackward() {
        ViewNode node = findFirst();
        return node != null && node.scrollBackward();
    }

    @Override
    public boolean scrollLeft() {
        ViewNode node = findFirst();
        return node != null && node.tryClick();
    }

    @Override
    public boolean scrollRight() {
        ViewNode node = findFirst();
        return node != null && node.tryClick();
    }

    public ViewNode await() {
        return waitFor();
    }

}
