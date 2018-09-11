
importClass(Packages.cn.vove7.common.view.finder.ViewFindBuilder)
ViewFinder = function(){
	return new ViewFindBuilder(executor);
}

importClass(Packages.cn.vove7.common.accessibility.AccessibilityApi)	
rootView = function (){
	var s = AccessibilityApi.Companion.getAccessibilityService()
	if (s) return s.getRootViewNode()
	else return null
}

clickById=function(id){
	return ViewFinder().id(id).tryClick()
}

clickText=function(text){
	return ViewFinder().equalsText(text).tryClick()
}
clickByDesc=function(desc){
	return ViewFinder().desc(desc).tryClick()
}

printAllChild = function(node){
	print('this', node)
	var cs = node.childs
	if(cs != undefined)
        cs.forEach(function(e,i){
            print(i, e)
        })
}

function dd(d,index){
    s = ''
    for (i=0;i<d ;i++)
        s = s + '  '
    s+='|-'+index+' '
    return s
}
traversingNode = function(index,d, node){
	print(dd(d,index) + node.toString())
	var cs = node.childs
	cs.forEach(function(e,ii){
		traversingNode(ii,i+1,e)
	})
}
traversing = function(node){
    traversingNode(0,0, node)
}