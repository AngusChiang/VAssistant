
importClass(Packages.cn.vove7.common.view.finder.ViewFindBuilder)
ViewFinder = function(){
	return new ViewFindBuilder();
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

function printAllChild(node){
	print(node)
	var cs = node.childs()
	for (var i = 0;i< cs.size();i++) {
		var child_i = cs[i]
		print(i, child_i)
	}
}
