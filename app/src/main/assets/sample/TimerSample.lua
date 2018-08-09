--
-- Created by IntelliJ IDEA.
-- User: SYSTEM
-- Date: 2018/8/1
-- Time: 1:17
-- TimerSample.lua
--
i = 0
function f()
    print('f()'..i)
    i = i + 1
end

print('begin')

a = timer(f, 1000, 1000)

--a.quit()
sleep(3500)
a.stop()
print('end')
