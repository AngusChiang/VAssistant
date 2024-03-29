### 2.3.8

- 修复语音唤醒自动休眠30分钟无效问题

### 2.3.7

- 支持打开小黑屋中的应用

### 2.3.6

- 修复拨打电话失败

### 2.3.5

- 优化文本点击
- 优化微软语音
- 优化无障碍服务

### 2.3.4

- 接入微软语音合成 （感谢 [ag2s20150909/TTS](https://github.com/ag2s20150909/TTS)）
- 优化Dui语音

### 2.3.2

- 新增Dui语音识别(默认)

### 2.3.1

- 移除输入法相关操作
- 关闭语音唤醒误触打开闪光灯
- 优化设置唤醒词

### 2.3.0

- 修复语音识别错误
- 支持ADB服务授权
- 优化互联服务
= 优化远程调试：VSCode可自动搜寻连接
- 优化脚本执行时自启无障碍服务(若有权限)，无需人工干预
- 优化系统TTS初始化

### 2.2.0

- 支持系统TTS [设置>语音合成>选择引擎>系统TTS]
- 优化屏幕助手动画
- 优化二维码相关
- 支持自动登录地铁WiFi [实验室>扩展]

### 2.1.8

- 修复Android 11文字识别出错
- 更改文字提取选取样式
- 支持即时同步新安装应用的应用内指令
- `waitAccessibility/requireAccessibility` 支持自动启动无障碍服务（前提：具有root或WRITE_SECURE_SETTINGS权限）

### 2.1.7

- 优化应用内指令加载速度
- 优化指令解析速度

### 2.1.5

- 修复氢OS11无法唤出屏幕助手
- 支持打开[冰箱]中冻结的应用
- 新增冰箱冻结指令
- 修复[屏幕助手]翻译出错

### 2.1.4

- 修复执行Tasker任务失败

### 2.1.3

- 支持定时任务
- 修复一些问题

### 2.1.2

- 适配Android10+ 唤醒词设置失败问题
- 添加语音识别[离线支持]设置

### 2.1.1

- 适配Android 11
- 增加语音识别是否使用蓝牙

### 2.1.0

- 支持设置连接蓝牙时是否提示音反馈
- 无障碍服务高亮提示
- 优化列表UI效果
- 优化App搜索速度

### 2.0.9.2

- 优化在Android7+系统上的媒体控制
- 优化底部对话框动画及样式

### 2.0.9.1

- 用户名支持中文输入
- 更丰富的颜色选择面板
- 优化文本编辑翻译
- 优化账户自动退出登录

### 2.0.9

- 支持创建语音指令桌面快捷
- 支持自启高级无障碍服务
- 增加语音识别稳定性
- 优化面板动画
- 添加最近任务隐藏提示
- 添加震动效果设置

### 2.0.8.1

- [屏幕助手] 微信扫码在双开时的成功率
- [屏幕助手] 文字编辑支持朗读
- [屏幕助手] 修复文字编辑框重复文字
- [脚本编辑器] 执行结束后自动弹出log

### 2.0.8

- 文字识别结果可直接编辑
- 添加新语音面板: 侧边依靠[位置可设置]

### 2.0.7

- 桌面快捷方式支持指定语音内容
- 修复一些bug

### 2.0.6.1

- 适配google面板dark模式
- 优化文字识别视图重叠处理
- 修复电源状态监听器

### 2.0.6

- 优化在Android Q自动截图
- 应用内指令创建桌面快捷方式使用App图标
- 适配Dark主题（自动跟随系统夜间模式）
- 优化语音识别处于引擎忙状态
- 修复若干bug

### 2.0.5

- 优化文字识别跳转效果
- 修复新建指令报错
- 移除唤醒其他应用的第三方库
- 修复文本生成二维码乱码

### 2.0.4

- 修复高分辨率机型文字错位
- 优化概率悬浮窗无法隐藏
- 支持play版白描

### 2.0.3

- 查询天气不再询问城市
- 支持无障碍按钮设置[设置/其他]
- 屏幕助手在Android Q支持自动点击立即开始
- 支持外部调用指令 `vassistant://run?cmd=天气`
- 支持息屏语音唤醒点亮屏幕
- 修复跳过广告配置失效
- 前台服务通知增加信息展示
- 设置闹钟添加震动

### 2.0.2

- 优化文字识别页
- 增强稳定性
- 修复指令导出问题
- 添加api `utils.runAppCommand` 负责全局指令调用App内指令
- 添加api `utils.waitAccessibility`
- 添加api `utils.checkVersion`

### 2.0.1

- 优化音乐控制指令
- 支持执行 [Tasker](https://www.coolapk.com/apk/net.dinglisch.android.taskerm) 任务
- [屏幕识别] 支持京东识别
- 修复唤醒词“播放”导致切换输入法

### 2.0.0.1

- 修复唤醒后不会自动停止
- 移除极光推送（推送其他应用消息）

### 2.0.0

- 加入[指令传递]功能(实验室/互联服务)
- [屏幕助手/使用微信扫一扫] 适配新版微信

### 1.9.9.6

- 更换主页样式
- 修复语音识别不自动停止问题
- 优化文字选取框选择

### 1.9.9.5

- 更换服务器IP
- 修复登录邮箱无法输入下划线


### 1.9.9.4

- 优化屏幕助手截屏时机
- 修复部分机型后台启动服务失败
- 优化二维码识别
- 屏幕助手适配宽屏/隐藏导航栏

### 1.9.9.3

- 文字识别/提取支持滑动选择
- 前台通知支持快捷操作
- 长按HOME键默认开启屏幕助手
- 修复文字识别一键翻译后，无翻译结果
- 修复指令同步：无支持应用指令时出现的错误

### 1.9.9.2

- [屏幕助手]添加淘宝识别
- [屏幕助手]适配状态栏/导航栏颜色

### 1.9.9.1

- 修复文字识别错位
- 状态栏快捷图块支持长按调用

### 1.9.9

- 优化冷启动速度（桌面语音识别）
- 新增面板样式
- 支持面板背景设置
- 加入前台服务加强后台能力
- 修改主界面在最近任务隐藏逻辑：按Home键不会从最近任务隐藏，返回键会退出并从最近任务隐藏
- 自动停止识别时间改为`300-3000`
- 修复充值后无法即时更新用户信息
- 适配基于Chromium浏览器的语音输入
- [指令管理菜单]加入指令新建教程链接
- [脚本编辑菜单]加入脚本文档链接

### 1.9.8.3

- 更换图标
- 支持启动时自动设为助手应用[设置/启动选项]

### 1.9.8.2

- 修复在`Android10`无法通过助手应用打开屏幕助手
- 适配部分机型无法启动外部应用[设置/其他/以兼容模式启动应用]
- 自动释放麦克风默认关闭

### 1.9.8.1

- 修复[应用内指令详情页]无法打开执行对话框问题

### 1.9.8

- 添加应用内指令自启App标志
- 移除跟随操作
- 加入单个指令导出/导入 [指令详情菜单]
- 支持全局指令抛出notSupport, 继续匹配
- 匹配应用内指令优先于全局指令
- 指令执行测试自动提示需要赋值参数
- 修复语音唤醒不自动休眠无效

### 1.9.7

- 支持家居系统：Rokid(若琪)
- 去除结束词
- 熄屏语音唤醒自动亮屏
- 调整语音唤醒休眠时长
- 修复酷安下载指令
- 修复语音唤醒音乐控制在服务未开启导致的崩溃

### 1.9.6

- 加快指令解析速度
- 语音唤醒适配音乐控制指令
- 自定义语音结束等待时长
- 加入识别时消除回音
- 在APP未启动时，桌面唤醒可直接进行识别

### 1.9.5

- 语音合成适配9.0
- 修复低概率所有指令执行失败
- 弃用`require 'accessibility'`标志，请使用`requireAccessibility()`

### 1.9.4.1

- 支持自定义按键唤醒
- 修复一些问题

### 1.9.4

- 支持更强大的文本编辑操作
- 适配若琪APP操控设备

### 1.9.3.2

- 修复js脚本无法使用api

### 1.9.3

- 增加发音人
- 增加设备管理器权限（开启可加强后台能力）
- 添加api: removeFloat

### 1.9.2

- 修复语音合成切换发音人失败
- 添加通知栏[屏幕助手]快捷图块

### 1.9.1

- 优化内存使用，减小安装包体积
- 完善[文字操作]对话框，支持更多功能
- 修复因小问题导致的崩溃
- 支持使用adb授权自动开启无障碍(方法查看帮助)
- 常见问题.增加图灵聊天apiKey
- 移除百度离线语音合成(使用离线功能请查看[帮助/使用手册])

### 1.9.0

- 修复音量为0时语音合成，悬浮窗不消失
- 修复开启响应词时连续切换唤醒导致一定几率无法唤醒
- 支持手势软件设置更多快捷功能，设置方法见常见问题


### 1.8.9

- 修复开启响应词时无法长语音
- 二维码对话框不需要悬浮窗权限
- 加入屏幕助手点击效果
- 修复语音面板文字显示不全
- 修复复制内容可能导致崩溃

### 1.8.8

- 取消注册时的验证码
- 支持修改邮箱
- 修复点击屏幕文字后悬浮窗不消失问题
- [屏幕助手]支持使用[微信/支付宝]扫一扫
- 修复跳过广告时导致的崩溃

### 1.8.7

- 优化speak时悬浮窗显示
- 修复语音唤醒和释放麦克风存在的问题

### 1.8.6

- 在已授权麦克风权限的APP内自动释放麦克风
- 修复语音唤醒定时关闭

### 1.8.5

- 加快识别速度（嘈杂环境中）
- 修复部分机型启动助手应用时的崩溃

### 1.8.4

- 语音悬浮窗加入揭露动画[设置>悬浮面板>动画]
- 修复长语音存在的问题
- 高级服务未开启时，执行手势操作提示执行失败
- 文字/屏幕识别加入图像压缩
- 修复屏幕识别导致崩溃
- 关闭应用优先使用root方式
- 降低文字识别匹配率

### 1.8.2

- 更新快捷键帮助
- 修复文字提取界面存在的问题
- 分词对话框不可下滑取消，减少误触

### 1.8.1

- 修复蓝牙无法使用长语音的问题
- 去除自定义长语音关闭时长，默认8s
- 修复设置指令执行参数崩溃问题


### 1.8.0

- 使用参数正则匹配，【为了正常使用必须更新此版本】
- 修复若干问题


### 1.7.4

- 优化屏幕助手弹出效果
- 修复一些问题

### 1.7.3
【[屏幕助手](https://www.coolapk.com/apk/217562)已提取为独立应用，可在酷安中搜索安装😄】

- 屏幕助手自动适配导航栏
- 连接蓝牙耳机时，播放识别音效
- 提供屏幕助手功能活动（可通过Xposed Edge等扩展调用活动快捷方式来启动）
- 新增设置[实验室/屏幕助手/长按HOME键] 用于设置长按HOME键触发的功能

### 1.7.2

- 修改悬浮UI
- 适配蓝牙耳机
- 修复toast不显示问题
- 重构代码
- 脚本无障碍标志替换为‘requireAccessibility()’

### 1.7.1

- 完成 屏幕助手/文字识别功能
- 适配图灵机器人，增加打开返回链接（支持更多命令，例如：红烧茄子怎么做、北京去上海的火车、腾讯股票、今日科技新闻）
注意：自定义过apikey的同学，请在技能扩展 勾选所有技能。 参考(https://vove.gitee.io/2019/01/24/custom_chat_system/
)

### 1.7.0
- 添加语音唤醒不自动休眠选项
- 修复在开启长语音时,开始识别后立即停止识别悬浮窗不关闭问题
- 修改屏幕助手UI

### 1.6.9
- 修复 解析时间x小时x分钟后
- 支持自定义图灵apikey

### 1.6.8
- 修改屏幕助手UI
- 加入兼容模式[设置/语音唤醒]\(不支持耳机麦克风)
- 加入状态栏语音唤醒快捷设置
- 加入语音输入,语音搜索

### 1.6.7
- 加入 ‘一键设置助手应用’ 快捷方式
- 修复应用安装卸载监听器
- 支持开启音量键唤醒时，点按，长按调节音量
> 提示：设为系统应用后，能够大大加强应用后台的能力。
> 设置方法：
> 使用[镧·系统工具箱](https://www.coolapk.com/apk/xzr.La.systemtoolbox)/应用管理设置。

### 1.6.6

- 解决保存截图时图库不显示问题
- 加入语音唤醒状态改变通知震动反馈
- 语音唤醒休眠添加10/20分钟选项
- 修改文字提取界面Ui

### 1.6.4
- 加入智能识别跳过广告[实验室/去广告]
- 修改部分UI，联系人列表可搜索
- 修改system.call()函数(新拨打电话指令依赖此版本)

### 1.6.3
- 分开无障碍服务（卡顿可只开启基础服务）

### 1.6.1

- 修复部分机型无法拨打电话问题
- 加入桌面切换语音唤醒快捷方式[桌面小部件]
- 支持设置日历事件提醒时间

### 1.6.0

- 修复二维码识别链接访问问题
- 加入设置备份恢复[高级/备份]
- 修改首页亮色状态栏
- 修复设置文字过长右边部件不显示问题

### 1.5.9-beta
- 修复长语音定时问题
- 去除无障碍低电量模式、无障碍黑名单
- 加入插件检查更新

### 1.5.8-beta
- 优化无障碍服务
- 加入长语音时间设置
- 优化提示音问题
- 优化二维码识别

### 1.5.7
- 修复speak后开启识别及其他一些问题
### 1.5.6
- 修复一加5/5t 安卓P版本在最近任务页面卡顿问题
- 移除青云客对话系统
- 修复speak后无法长语音问题
- 加入无障碍黑名单[实验室/其他]
### 1.5.5
- 支持[语音唤醒]和[长语音]同时开启
- 加入长语音定时关闭
- 加入语音悬浮窗位置设置[设置/语音面板]
### 1.5.4
- 修复任何开启长语音时，语音合成结束后，识别开启问题
### 1.5.3
- 加入长语音（支持连续说出命令）[设置/语音识别/长语音]
- 修复开启提示音部分情况不识别问题
### 1.5.2
- 修复插件安装失败
### 1.5.1
- 加入提示音
- 加入插件管理 可扩展更多功能
- 修复未安装标记应用匹配问题
- 修复未打开无障碍，使用聊天问题
- 修复一些问题
- 添加api：
  - system.getContactByName()
### 1.5.0 
- 开放脚本编辑、新建指令、指令分享、新建标记等功能
### 1.4.1 
- 修复只翻译第一行文本
- 修复部分三星拨打电话权限问题
### 1.4.0
- 修复7.0版本下脚本执行崩溃问题
### 1.3.8
- 文字提取支持翻译
- 修复一些导致崩溃的问题
### 1.3.7
- 修复卡顿问题
- 加快广告跳过速度
### 1.3.6
- 在其他App内由于麦克风占用关闭唤醒时, 熄屏开启唤醒
- 加入长按耳机中键唤醒设置
- 添加快捷方式，其他应用可通过此方式唤醒(如[悬浮菜单]中选择快捷方式唤醒)
- 悬浮面板显示语音回复内容
### 1.3.5
- 修复引擎未就识别导致崩溃
- 添加设置长按延时
- 修复App内指令设为全局失败
- 添加[快捷唤醒]桌面小部件
- 静音功能适配
- 添加api：
  - ViewFinder.waitHide()
### 1.3.4
- 支持选卡拨号
- 支持VSCode将代码发送至App新建指令
- 支持VSCode将文本复制至手机剪切板
- 添加api
  - system.simCount
  - system.contacts
  - system.saveMarkedContact
  - system.saveMarkedApp
  - system.call(phone,simId)
### 1.3.3
- 修复低于安卓8.0执行system函数崩溃问题
### 1.3.2
- 修复语音唤醒定时问题
### 1.3.1
- 加入省电模式(必关闭语音唤醒)
- 加入快捷屏幕助手[实验室]（设为默认辅助应用，可长按HOME键触发，部分机型需手动设置）
  - 屏幕识别
  - 文字提取
  - 分享屏幕
  - 二维码/条码识别
- 无障碍/语音唤醒状态改通知栏通知
- 修复若干问题
- 添加api
  - system.batteryLevel
  - system.isCharging()
### 1.3.0
- 解决语音唤醒麦克风占用问题
- 修复若干问题
### 1.2.10
- 支持多个用户唤醒词
- 修复识别时仍能使用唤醒词唤醒问题
- 修复开启无障碍崩溃问题
### 1.2.9
- 加入用户唤醒词
- 添加标志runtime.userInterrupt
- 优化当前App/Activity判断

### 1.2.8

- 数据更新方式换为增量更新
- 支持网页内文字提取
- 支持语音唤醒后无间断说出命令（响应词打开则无效）
- 修改后台控制音乐逻辑
- 加入结束词 (实验室)
- 加入执行反馈设置
- 完善自动开启无障碍
- 添加基础引导
- 修复关闭应用指令
- 命令修剪结束词
- 添加api
  - system.sendSMS(phone: String, content: String)
  - system.getLaunchIntent(pkg:String)
  - system.getPhoneByName(name: String)
  - runtime.focusView
  - ViewNode.appendText()
  - ViewNode.globalClick()
  - ViewFinder.containsDesc(...)
### 1.2.6
- 加入指令优先级设置   (指令详情菜单) ps: 指令列表按优先级排列
- 加入连续对话 (实验室)
- 修复若干问题
### 1.2.5
- 修复图灵机器人调用问题
- 修复解析日期‘x小时’后错误
- 其他问题
### 1.2.4
- 加入对话系统：图灵机器人 (/实验室)
### 1.2.3
- 修复部分机型识别后崩溃
### 1.2.2
- 加入App自动检查更新
- 加入对话系统
- 加入语音唤醒自动休眠后亮屏后自动开启
- 加入在播放语音合成时，长按音量下可停止播放
### 1.2.1_0
- fix 7.1.1及以下设备识别结束崩溃问题
- 移除对[5.0-6.0)的支持
- fix 部分设备截屏崩溃问题"),
- ### 1.2.1
- 在[文字提取]加入分词功能
- 离线命令词，识别联系人，和应用更准确
- 支持阿拉伯数字与中文模糊匹配
- 加入 关于/更新日志"),
### 1.2.0
- fix 语音唤醒响应词结束后，播放音乐
- 添加非充电状态下，语音唤醒自动休眠
- 支持有线耳机耳麦唤醒和语音识别
- 支持多唤醒词，支持的唤醒词(唤醒即执行)： 播放,停止,暂停,上一首,下一首,打开手电筒,关闭手电筒,截屏分享,文字提取)
- (自定义过唤醒词的，需要手动恢复默认，如需自定义，可以将上面唤醒词和你的自定义在获取唤醒词一起导出）
- 添加耳机中键唤醒
- 添加代码编辑器快速插入 require 'accessibility'
- tips: 蓝牙快捷键(若支持)可触发默认语音助手
### 1.1.8
- 代码编辑器添加插入声明无障碍模式
- 添加系统Api screenOn screenOff sendKey
- 修复 在speakSync 音量静音时，显示toast后 回调失败问题
- 其他问题
### 1.1.7_5
- fix 指令设置初始化失败
- 新增 运行时 状态栏通知显示命令"),
### 1.1.7_3
- 添加日期解析 'x天后'
- fix some bug
### 1.1.7_2
- 修复指令同步失败
- 修复打开标记功能
- 自启无障碍同时保持其他服务
- 若干其他其他问题
### 1.1.7
- 支持指令多参数
- 添加自动开启无障碍服务(设置/其他 需手动开启)
- 添加指令 创建闹钟和日历事件（均此版本可用）
- 添加api androRuntime (终端/Root命令相关)
- 添加api parseDateText (解析日期文本 ;Fx其他页)
- 更新时数据显示更新进度和内容
- 解决误认输入法为当前activity
### 1.1.6
- 增加创建日历事件、闹钟api (系统函数)
- 修复语音合成通道设置问题及some bug")
### 1.1.5
- fix 唤醒响应词，后台音乐未播放时，识别结束后音乐响起问题
- 完成备份功能
- 自定义唤醒时响应词
- 加入语音合成选择音频输出通道
- 移除对x86的支持
- 修复若干问题
### 1.1.4_1
- 修复部分机型无障碍错误崩溃
- 修复部分机型闪光灯打开失败
- 修复其他若干问题
### 1.1.4
- 加入数据自动更新和一键更新(高级中)
- 加入网络api（示例参考js编辑器中的'天气.js'）
- 加入屏幕文字提取(同步最新数据后，使用文字提取 )
- 加入指令详情代码复制
### 1.1.0
- 完成 代码编辑器
- 自定义唤醒词
- 修复截屏后投屏图标不消失
- 加入云解析
- 增加本地解析失败逻辑
### 1.0.4

- 加入充电时自动开启语音唤醒
- 添加shortcut功能 可添加 唤醒/指令 桌面快捷方式
- 可设置解析失败，自动执行smart打开操作，详情见 高级/命令解析