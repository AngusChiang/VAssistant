var i=0;
var t= thread(function () {
	while(1){
		print(++i);
		sleep(1000)
	}
})

sleep(3220)

quit()