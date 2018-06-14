function openApp(s){
    return window.at_api.openApp(s);
}
function openAppByName(s){
    return window.at_api.openAppByName(s);
}
function startActivity(p,n){
    return window.at_api.startActivity(p,n);
}
function findFirstNodeById(id){
    return window.as_api.findFirstNodeById(id)
}
function findNodeById(id){
    return window.as_api.findNodeById(id)
}
function findNodeById(text){
    return window.as_api.findFirstNodeByText(text)
}
function findNodeByText(text){
    return window.as_api.findNodeByText(text)
}
function click(node) {
    return operate.click(node)
}
function longClick(node) {
    return operate.longClick(node)
}
function select(node) {
    return operate.select(node)
}
function scrollUp(node) {
    return operate.scrollUp(node)
}
function scrollDown(node) {
    return operate.scrollDown(node)
}
function setText(node, text) {
    return operate.setText(node,text)
}
function getText(node) {
    return operate.getText(node)
}
function focus(node) {
    return operate.focus(node)
}

function back(){
    return as_api.back()
}
function recentApp(){
    return as_api.recentApp()
}
function home(){
    return as_api.home()
}
function showNotification(){
    return as_api.showNotification()
}
console.log("加载完成")
