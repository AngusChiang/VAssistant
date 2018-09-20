# SurplusWork

## Core

- ParseEngine
- ExecutorEngine
- MainService
- AccessibilityService

## Script API

- [x] 设备信息
- [x] 存储Api
- [x] SpHelper

## TODO
- Core
  - [x] lua call java getter setter
  - [x] 完善Lua 
  - [ ] Rhino efficiency
- 截屏api
- 屏幕内容
- 屏幕文字

- [x] 语音识别文字显示
 - 动画

   - [x] 识别动画： 识别开始 -> 结果
     - [x] 解析动画： 解析过程 -> 结果
     - [x] 执行动画： 执行过程 -> 结果
     
 - 自定义命令
   - [x] 命令创建 (desc|regex|script|app pkg)
   - [x] 命令详情页
 - [x] 设置
 - [x] 语音唤醒
 - [x] 跳过广告
 - [ ] 商店
 - [ ] Dashboard
 - [x] Me
 - [x] 登陆
 - [-] 欢迎界面
 - [-] 多参数支持
 - [-] 在线解析?
 - [-] 语音唤醒冲突
 - [x] 命令预览- 执行
 - [x] 指令共存规则
 - [x] 指令标识码
 - [x] 同步
 - [-] User history
 - [ ] inApp -> Global

## 未来

 - [ ] **ActionNode参数编辑**
 - [ ] 屏幕识别
 - [ ] 
 
## Marked

 - [x] 打开MarkedOpen
 - [x] Marked -联系人 -App -广告id
 - [ ] edit

## VIP提供

- [ ] 云备份
- [ ] 高级操作
  - [x] 开发模式
  - [x] 上传分享
  - [x] 远程调试
  - [x] 本地新建
- [ ] 命令数量限制
- [ ] My Shared

## FAQ 

- 突然按键失灵了?
> 按键失灵一般是由于程序停止导致的，目前也属于安卓系统的bug，解决方法就是进入App详情，将此应用强行停止。

- 跳过广告速度慢
> 帮助用户自动跳过广告，只是模拟了用户点击，然而真实情况下用户点击，效果也是延迟很大，跟手机性能有些关系。此外，延迟高一般出现在引用首次加载，从后台再次进入的广告去除效果很好。

## 注意

- 上传操作先从上级操作上传
- 深度不超过3