
importClass(java.lang.Thread);
importClass(Packages.cn.vove7.rhino.api.ThreadApi);

//	thread  = function(runnable) {
//		var t = new Thread(runnable);
//		gcx.reg(this,t);
//		t.start();
//		return t;
//	}
thread  = function(runnable) {
	return ThreadApi.start(this,runnable);
}

// sleep = function(m) {
// 	Thread.sleep(m)
// }