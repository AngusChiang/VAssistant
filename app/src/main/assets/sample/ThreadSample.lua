--
-- Created by IntelliJ IDEA.
-- User: SYSTEM
-- Date: 2018/8/1
-- Time: 0:20
-- ThreadSample.lua
--


-- a=0
function f(s)
    for j = 0, 9 do
        print(s ..'-->'.. j)
--        a=a+1 xian'chen   线程函数内不可访问外部变量
    end
end

print('begin')
for i = 0, 3 do
    thread(f, i)
end
print('end')