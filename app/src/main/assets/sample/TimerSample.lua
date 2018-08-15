--
-- Created by IntelliJ IDEA.
-- User: SYSTEM
-- Date: 2018/8/1
-- Time: 1:17
-- TimerSample.lua
--
--
function f()
    print('do something')--..i)
end

print('begin')

a = timer(f, 1000, 1000)

sleep(3500)
a.quit()
print('end')
