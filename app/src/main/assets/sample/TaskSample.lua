--
-- Created by IntelliJ IDEA.
-- User: SYSTEM
-- Date: 2018/8/1
-- Time: 2:58
-- TaskSample.lua
--
function f(b)
    for i = b, 9 do
        update(i)
        sleep(200)
    end
    return 1, "s", 1.1 -- 返回给callback
end

function callback(...)
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

-- 无法强行停止，cancel只能阻止执行
task(f, 3, update, callback)

task(1000, callback)