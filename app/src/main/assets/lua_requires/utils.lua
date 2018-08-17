--
-- @author 17719247306
-- 2018/8/15 21:37
-- utils.lua
--

require 'import'
import 'cn.vove7.vtp.text.TextTransHelper'
import 'cn.vove7.vtp.builder.*'

function toPinyin(text)
    return TextTransHelper(app).chineseStr2Pinyin(text, false)
end

function toFirstPinyin(text)
    return TextTransHelper(app).chineseStr2Pinyin(text, true)
end


