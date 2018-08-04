--
-- Created by IntelliJ IDEA.
-- User: SYSTEM
-- Date: 2018/8/1
-- Time: 2:58
-- testTask.lua
--
function f()
    for i = 1, 9 do
        print(i)
    end
end

function cb(...)
    print('finished..')
end

task(f, cb)
