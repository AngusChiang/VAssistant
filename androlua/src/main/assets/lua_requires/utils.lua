--
-- @author 17719247306
-- 2018/8/15 21:37
-- utils.lua
--

require 'import'
import 'cn.vove7.vtp.text.TextTransHelper'
import 'cn.vove7.vtp.builder.*'

function toPinyin(text, ...)
    local ps = { ... }
    local firstLetter = false
    if (#ps > 0) then
        firstLetter = ps[1]
    end
    return TextTransHelper(app).chineseStr2Pinyin(text, firstLetter)
end



