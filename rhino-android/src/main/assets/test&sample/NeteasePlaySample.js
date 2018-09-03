
smartOpen('com.netease.cloudmusic')
var search = ViewFinder().desc('搜索', 'Search').await()
search.tryClick()

var s = ViewFinder().id('search_src_text').await()
s.setText(args[0])
var bounds = s.getBounds() //(154, 87 - 1055, 185)[1920x1080]
var x = (bounds.left +  bounds.right)/2
var y = bounds.bottom + 100//276
sleep(2000)
click(x,y)
// ---- fixme
// --a = ViewFinder().containsText({ '"' .. args[1] .. '"', '“' .. args[1] .. '”' }).await()
// --a.tryClick()  
//sleep(2000) // TODO  show

s = ViewFinder().type('listview').await(6000)
c=s.childs()[0]
if(c == null){
	toast("搜索失败")
}else {
	c.tryClick()
}


