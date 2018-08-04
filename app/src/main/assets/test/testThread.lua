--
-- Created by IntelliJ IDEA.
-- User: SYSTEM
-- Date: 2018/8/1
-- Time: 0:20
-- testThread.lua
--

function f(s)
    for j = 0, 9 do
        print(s ..'-->'.. j)
    end
end

print('begin')
for i = 0, 3 do
    thread(f, i)
end
print('end')