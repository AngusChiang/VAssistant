
//import 'android.view.KeyEvent'

//*
//通过包名打开App
// ExResult
//
openAppByPkg = function(pkg,reset){
    if(reset==undefined) reset=false
    return system.openAppByPkg(pkg,reset)
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

function mediaNext(){
    system.mediaNext()
}

function mediaPre(){
    system.mediaPre()
}

function volumeMute(){
    system.volumeMute()
}

function volumeUnmute(){
    system.volumeUnmute()
}

function volumeUp(){
    system.volumeUp()
}

function volumeDown(){
    system.volumeDown()
}
function setMusicVolume(index){
    system.setMusicVolume(index)
}
function setAlarmVolume(index){
    system.setAlarmVolume(index)
}
function setNotificationVolume(index){
    system.setNotificationVolume(index)
}