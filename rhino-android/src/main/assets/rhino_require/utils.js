importClass(Packages.cn.vove7.vtp.text.TextTransHelper)
importPackage(Packages.cn.vove7.vtp.builder)

function toPinyin(text,firstLetter){
	if(firstLetter == undefined) firstLetter=false
    return new TextTransHelper(app).chineseStr2Pinyin(text, firstLetter)
}

