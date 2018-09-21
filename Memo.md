# Memo

## 点击操作
- tryClick 可点击其父级容器简化操作

## 解析"播放许嵩的断桥残雪"

解析方法(两种):

1. 通过优先级控制 b > a, 分两步解析 "播放%" -> "的%"  ;(局限[歌]优先于%)
2. 脚本内解析 % -> 歌

以第一种来说解析节点：
- 播放%    ------------------------ a
- 播放%的歌   -------------------b
- 的%  -> parent = 播放%  --c    跟随 a

## 预解析
预解析跟随操作 ：  QQ扫一扫  QQ浏览器

## # 语义问题

如 播放周杰伦的歌 与 播放我的祖国 ,

显然，在后者c代码中先判断列表是否有此歌，再执行点击歌手栏操作；或其他骚操作 ,判断即ok

## 运行时参数

- currentActionIndex: Int
- actionCount: Int
- currentApp: ActionScope?
- currentActivity: String
- actionScope: Int?
- isGlobal():Boolean

## App内命令提升至全局

> 将ActionNode复制一条 修改actionScopeType类型


## Lua 与 js 区别

|                    | lua                          | js                       | 注意               |
| :----------------- | :--------------------------- | ------------------------ | ------------------ |
| 数组               | 从1开始`args[1]`             | 从0开始`args[0]`         | 影响到外部参数args |
| 函数参数（多参数） | `desc({ '搜索', 'Search' })` | `desc('搜索', 'Search')` |                    |



- 数组索引: lua 从1开始(args[1])，js从0开始(args[0]) ,影响到外部参数
- js:` ViewFinder().desc('搜索', 'Search')`   lua:`ViewFinder().desc({ '搜索', 'Search' })`



## cmd 批量重命名 

> `for /f "tokens=2 delims=-." %i in ('dir frame-*.gif /b') do ren  frame-%i.gif listening_%i.gif`

## Marked.. 

- 只显示已安装，只获取本机已安装

## Rule

- 服务器数据不可修改

- 备份 ：备份用户自己的 DataFrom.from_user

- 同步
  - Action同步
    - 全局同步
    - App内搜索 / 一键同步 
    - ActionNode.parent 数据根据tagid 获取id 指定parentId
    
  - Marked同步
    - contact...app..open
    - ad只显示已安装，只获取本机已安装