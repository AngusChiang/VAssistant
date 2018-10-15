
// 正则 %天气%
if (runtime.DEBUG) {
    runtime.command = "昨天天气怎么样"
}

function outData(weatherData) {
    command = runtime.command
    var data;
    // if (command.indexOf('今天') != -1)
    //     data = weatherData.forecast[0]
    // else
    var isYesToday = false
    if (command.indexOf('昨天') != -1) {
        data = weatherData.yesterday
        isYesToday = true
    } else if (command.indexOf('明天') != -1)
        data = weatherData.forecast[1]
    else if (command.indexOf('大后天') != -1)
        data = weatherData.forecast[3]
    else if (command.indexOf('后天') != -1)
        data = weatherData.forecast[2]
    else
        data = weatherData.forecast[0]
    var wt;
    if (isYesToday) {
        wt = data.date + ',' + data.type + ',' + data.low + ' - ' + data.high
            + ', ' + data.fx + data.fl.replace('<![CDATA[','').replace(']]>','')
    } else
        wt = data.date + ',' + data.type + ',' + data.low + ' - ' + data.high
            + ', ' + data.fengxiang + data.fengli.replace('<![CDATA[','').replace(']]>','')

    print(wt)
    speakSync(wt)

}

toast('正在获取天气信息')
var ip = system.getNetAddress()
print(ip)
sleep(500)

var cityJson = http.getAsPc('http://ip.taobao.com/service/getIpInfo.php?ip=' + ip)
//print(cityJson)

var j = JSON.parse(cityJson)

if (j['code'] != 0) {
    speak('定位失败')
} else {
    var city = j['data']['city']
    print(city)
    var data = http.get('http://wthrcdn.etouch.cn/weather_mini?city=' + city)
    data = JSON.parse(data)
    print(data['status'])
    if (data['status'] == 1000) {
        outData(data.data)
    } else speak('天气数据获取失败')
}