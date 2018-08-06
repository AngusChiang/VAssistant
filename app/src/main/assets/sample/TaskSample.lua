--
-- Created by IntelliJ IDEA.
-- User: SYSTEM
-- Date: 2018/8/1
-- Time: 2:58
-- TaskSample.lua
--
function f()
    for i = 1, 9 do
        print(i)
    end
    return { 1, "s", 1.1 }
end

function cb(...)
    s = { ... }
    for i, v in ipairs(s) do
        print(i, v, type(v))
    end
    print('finished..')
end

task(f, cb)
