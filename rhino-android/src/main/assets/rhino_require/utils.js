importClass(Packages.cn.vove7.vtp.text.TextTransHelper)
importClass(Packages.cn.vove7.common.utils.TextHelper)
importClass(Packages.cn.vove7.common.utils.TextDateParser)
importClass(Packages.cn.vove7.common.app.AppConfig)
importPackage(Packages.cn.vove7.vtp.builder)

function toPinyin(text,firstLetter){
	if(firstLetter == undefined) firstLetter=false
    return new TextTransHelper(app).chineseStr2Pinyin(text, firstLetter)
}

function matches(s,regex){
    return TextHelper.INSTANCE.matches(s,regex)
}
function matchValues(s,regex){
    return TextHelper.INSTANCE.matchValues(s,regex)
}
function matchParam(s,regex){
    return TextHelper.INSTANCE.matchParam(s,regex)
}
function arr2String(arr){
    return TextHelper.INSTANCE.arr2String(arr,', ')
}
function parseDateText(s){
    return TextDateParser.INSTANCE.parseDateText(s)
}

function notSupport() {
    executor.notSupport()
}

function checkVersion(vc, vn) {
    if(AppConfig.INSTANCE.versionCode < vc)
        utils.throw('此操作需要' + vn + ', 请更新至最新版本')
}