--
-- @author Vove
-- 2018/8/15 21:37
-- utils.lua
--

--require 'import'
import 'cn.vove7.common.app.AppConfig'
import "cn.vove7.common.appbus.AppBus"
import 'cn.vove7.vtp.text.TextTransHelper'
import 'cn.vove7.common.utils.TextHelper'
import 'cn.vove7.vtp.builder.*'
import 'cn.vove7.common.utils.TextDateParser'

function toPinyin(text, ...)
    local ps = { ... }
    local firstLetter = false
    if (#ps > 0) then
        firstLetter = ps[1]
    end
    return TextTransHelper(app).chineseStr2Pinyin(text, firstLetter)
end

function parseDateText(s)
    return TextDateParser.INSTANCE.parseDateText(s)
end

function matches(s, regex)
    return TextHelper.INSTANCE.matches(s, regex)
end

function matchValues(s, regex)
    return TextHelper.INSTANCE.matchValues(s, regex)
end

function matchParam(s, regex)
    return TextHelper.INSTANCE.matchParam(s, regex)
end

function arr2String(arr)
    return TextHelper.INSTANCE.arr2String(arr, ', ')
end

function notSupport()
    executor.notSupport()
end

function notifyFailed(s)
    executor.executeFailed(s)
end

function log(msg)
    luaman.log(msg)
end

function checkVersion(vc, vn)
    if(AppConfig.INSTANCE.versionCode < vc) then
        utils.throw('此操作需要'..vn..', 请更新至最新版本')
    end
end