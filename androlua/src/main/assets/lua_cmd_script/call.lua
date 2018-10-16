--
-- @author Vove
-- 2018/8/6 22:41
-- call.lua
--

local args = { ... }
if (#args >= 1) then
    executor.smartCallPhone(args[1])
end



function playLocal()
    -- 操作
end

function playSinger(s)
    --检查列表有此歌s
    --若有
    --点击play
    --若无点击歌手页，播放
end

function search(s)
end

function playSong(s)
    -- 点击
end

arg = arg[1]
if (arg == '本地音乐') then
    playLocal()
else
    local s = matchValues(arg, '%的%')
    if (s) then
        if (s[1] == '歌') then
            playSinger(arg)
        else
            playSong(s[1])
        end
    else
        playSong(arg)
    end
end