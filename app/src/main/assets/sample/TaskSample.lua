--
-- Created by IntelliJ IDEA.
-- User: SYSTEM
-- Date: 2018/8/1
-- Time: 2:58
-- TaskSample.lua
--
function f()
    for i = 1, 9 do
        update(i)
    end
    return 1, "s", 1.1
end

function cb(...)
    local s = { ... }
    print('返回参数: ')
    for i, v in ipairs(s) do
        print(i, v, type(v))
    end
    print('finished..')
end

function update(p)
    print(p)
end

task(f, update, cb)
