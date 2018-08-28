
importClass(Packages.android.util.Pair)
var deviceInfo = system.getDeviceInfo()
var mHeight = deviceInfo.screenInfo.height
var mWidth = deviceInfo.screenInfo.width

// 相对度量
var relHeight = mHeight
var relWidth = mWidth

function setScreenSize(width, height){
    relHeight = height
    relWidth = width
}
//

function scaleX(x){
    var sx = x / relWidth * mWidth
    log('x: ' .. sx)
    return sx
}

function scaleY(y){
    var sy = y / relHeight * mHeight
    log('y: ' .. sy)
    return sy
}
//

//[[
//全局操作
 //
function swipe(x1, y1, x2, y2, delay){
    return automator.swipe(scaleX(x1), scaleY(y1), scaleX(x2), scaleY(y2), delay)
}
//

function press(x, y, delay){
    return automator.press(scaleX(x), scaleY(y), delay)
}
//


function longClick(x, y){
    return automator.longClick(scaleX(x), scaleY(y))
}
//

function scrollDown(){
    return automator.scrollDown()
}
//

function click(x, y){
    return automator.click(scaleX(x), scaleY(y))
}
//


function scrollUp(){
    return automator.scrollUp()
}
//

function back(){
    return automator.back()
}
//

function home(){
    return automator.home()
}
//

function splitScreen(){
    return automator.splitScreen()
}
//

function gesture(durtion, points){
    return automator.gesture(0, durtion, scaleTable(points))
}
//

function scaleTable(points){
    log('points size: ' + points.length)
    var ps = []
    points.forEach( function(element, index) {
        var x = scaleX(element.first)
        var y = scaleY(element.second)
        ps[index] = new Pair(x, y)
    });
    return ps
}
//

function gestureAsync(durtion, points){
    return automator.gestureAsync(0, durtion, scaleTable(points))
}
//

function powerDialog(){
    return automator.powerDialog()
}
//

function notifications(){
    return automator.notifications()
}
//

function quickSettings(){
    return automator.quickSettings()
}
//

function recents(){
    return automator.recents()
}
//


function toast(msg, l){
    automator.toast(msg, l)
}
//

function toast(msg){
    automator.toast(msg)
}
//