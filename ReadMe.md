# Summary
## AccessibilityEvent

|  事件类型  |  发生时机  |
|:-------------------------|--------:|
| TYPE_WINDOW_CONTENT_CHANGED  | 局部/"帧"刷新/弹出菜单 |
| TYPE_WINDOW_STATE_CHANGED  | 窗口切换/对话框 |
| TYPE_VIEW_CLICKED | View点击（有响应事件才触发） |
| TYPE_VIEW_LONG_CLICKED | View长按（有响应事件才触发） |
| TYPE_VIEW_SCROLLED | （ListView）滚动 |
| TYPE_VIEW_TEXT_CHANGED | 输入框文本改变 |
| TYPE_VIEW_TEXT_SELECTION_CHANGED | 滑动选择文本触发/输入框文本改变 |
|  |  |
|  |  |
|  |  |
|  |  |
# 匹配流程

 匹配成功历史表 -> 本地模糊匹配 -> 服务提供方案

# 参考

- 中文转拼音 https://www.cnblogs.com/itred/p/4060695.html
- 
