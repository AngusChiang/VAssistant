
system.startActivity(app.packageName,'cn.vove7.jarvis.debug.ClickCountActivity')

sleep(600)

a = 0
setScreenSize(100, 100)
while system.isScreenOn() and not runtime.userInterrupted do
    click(20, 40)
    a = a + 1
    sleep(15)
end

print(a)