
//import 'android.view.KeyEvent'

//*
//通过包名打开App
// ExResult
//
openAppByPkg = function(pkg){
    return system.openAppByPkg(pkg)
}


//*
//通过通过关键字匹配
// return ExResult pkgName if ok
//
openAppByWord=function(appWord){
    return system.openAppByWord(appWord)
}


//*
//手电
//ExResult
openFlashlight = function(){
    return system.openFlashlight()
}
//

//[[
//获取App信息
//s : 包名或App名
// App名可能随系统语言变化  不建议使用App名查找
//
getAppInfo=function (s){
    return system.getAppInfo(s)
}
//
//useless TODO media control
sendKey=function (key){
    system.sendKey(key)
}
//

isMediaPlaying = function(){
    return system.isMediaPlaying()
}

function mediaPause(){
    system.mediaPause()
}
function mediaStart(){
    system.mediaStart()
}
function mediaResume(){
    system.mediaResume()
}
function mediaStop(){
    system.mediaStop()
}
