--
-- @author 17719247306
-- 2018/8/15 21:04
-- DeviceInfoSample.lua
--

info = system.getDeviceInfo()

print('屏幕信息', info.screenInfo)
print("屏幕高度",info.screenInfo.getHeight())
print("屏幕宽度",info.screenInfo.getWidth())
print("密度",info.screenInfo.getDensity())
print('厂商名', info.manufacturerName)
print('产品名', info.productName)
print('品牌', info.brandName)
print('型号', info.model)
print('设备名', info.deviceName)
print('序列号', info.serial)
print('Sdk版本', info.sdkInt)
print('Android版本', info.androidVersion)
print('当前语言', info.language)