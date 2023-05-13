
# Build VAssistant

1. 克隆仓库到本地
```shell
git clone https://github.com/Vove7/VAssistant
git clone https://github.com/Vove7/VAssist-Scrcpy
git clone https://github.com/Vove7/EdgeTTS
```
将上面3个仓库 clone 到同一目录


2. 使用 Android Studio 打开 VAssistant


3. 修改配置 VAssistant/local.properties 文件

添加：
```properties
sdk.dir=....
scrcpy-lib.dir=../VAssist-Scrcpy/common
tts-lib.dir=../EdgeTTS/app
```

4. 使用 Android Studio 运行 app

