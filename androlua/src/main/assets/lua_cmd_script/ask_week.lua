--
-- @author Vove
-- 2018/9/11 16:45
-- ask_week.lua
--

local weeks = { '日', '一', '二', '三', '四', '五', '六' }
local c = Calendar.getInstance()
local w = c.get(Calendar.DAY_OF_WEEK)

speak('今天是星期' .. weeks[w])
