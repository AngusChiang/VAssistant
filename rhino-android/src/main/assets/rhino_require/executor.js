
actionCount = executor.actionCount
currentActionIndex = executor.currentActionIndex

checkService = function(){
    return executor.checkAccessibilityService(false)
}

notifyFailed = function(s){
    executor.executeFailed(s)
}

accessibility = function(){
    return executor.checkAccessibilityService(true)
}
requireAccessibility = function(){
    executor.requireAccessibility()
}
waitAccessibility = function(m) {
    if(m)
        return executor.waitAccessibility(m)
    else
        return executor.waitAccessibility()
}

//  语音合成同步 ，说完再向下执行
// @param text
// @return booleam 是否成功
//
speakSync = function(text){
    return executor.speakSync(text)
}


//
// 语音合成
//
speak = function(text){
    executor.speak(text)
}


//
//
// return boolean
alert = function(t, m){
    return executor.alert(t, m)
}


//
//
// return String?
singleChoiceDialog = function(title, choices){
    return executor.singleChoiceDialog(title, choices)
}


//
//
// return string
waitForVoiceParam = function(){
    return executor.waitForVoiceParam(nil)
}

//
// s:询问时显示文字
// return string
waitForVoiceParam = function(s){
    return executor.waitForVoiceParam(s)
}

//
// 等待界面 pkg , activity  millis
// return boolean
waitForApp = function(pkg, activity, millis){
    if(millis == undefined) millis = -1
    log(activity)
    log(millis)
    return executor.waitForApp(pkg, activity, millis)
}


//
// return ViewNode? , null if AccessibilityService is not running
 
waitForId = function(id, millis){
    if(millis == undefined) millis = -1
    return executor.waitForViewId(id, millis)
}


//[[
// return ViewNode? , null if AccessibilityService is not running
 
waitForDesc=function(desc,millis){
    if(millis == undefined) millis = -1
    return executor.waitForDesc(desc, millis)
}

//[[
// return ViewNode? , null if AccessibilityService is not running
 
waitForText = function(text, millis){
    if(millis == undefined) millis = -1
    return executor.waitForText(text, millis)
}


//[[
// smart smartOpen
 
smartOpen = function(s){
    return executor.smartOpen(s)
}
smartClose = function(s){
    return executor.smartClose(s)
}
removeFloat = function() {
    executor.removeFloat()
}
sleep = function(m){
    executor.sleep(m)
}


