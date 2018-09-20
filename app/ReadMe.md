# Summary
## AccessibilityEvent

|  事件类型  |  发生时机  |
|:-------------------------|--------:|
| TYPE_WINDOW_CONTENT_CHANGED  | 局部/"帧"刷新/弹出菜单 |
| TYPE_WINDOW_STATE_CHANGED  | 窗口切换/对话框/弹出输入法  |
| TYPE_VIEW_CLICKED | View点击（有响应事件才触发） |
| TYPE_VIEW_LONG_CLICKED | View长按（有响应事件才触发） |
| TYPE_VIEW_SCROLLED | （ListView）滚动 |
| TYPE_VIEW_TEXT_CHANGED | 输入框文本改变 |
| TYPE_VIEW_TEXT_SELECTION_CHANGED | 滑动选择文本触发/输入框文本改变 |
|  |  |
|  |  |
|  |  |
|  |  |
# 脚本支持
- 全局操作

> ACTION_OPEN 

> ACTION_CALL 

- 导航

> ACTION_BACK 
> ACTION_HOME 
> ACTION_RECENT
> ACTION_PULL_NOTIFICATION 

 
- 视图控件基本操作

> ACTION_CLICK_TEXT 
> ACTION_CLICK_ID 
> ACTION_CLICK_ID_AND_TEXT 
> ACTION_LONG_CLICK_TEXT 
> ACTION_LONG_CLICK_ID 
> ACTION_LONG_CLICK_ID_AND_TEXT 
> ACTION_SET_TEXT_BY_ID 
> ACTION_SCROLL_UP 
> ACTION_SCROLL_DOWN 
> ACTION_FOCUS_TEXT 
> ACTION_FOCUS_ID 

- 其他支持
> waitForActivity 
# 其他功能

- 识别屏幕内容
- 截屏(分享)
- 清空编辑区/输入框

# 匹配

- 流程： 匹配成功历史表 -> 标记 -> 本地模糊匹配 -> 服务提供方案
 
 # 标记

 - 标记识别
 - 联系人
 - App

# 参考

- 中文转拼音 https://www.cnblogs.com/itred/p/4060695.html
- 

# 