
var sp = newSpHelper('js_sample')

sp.set('no', 127)  //fixme int to float
sp.set('name', 'javaScript')
sp.set('b', true)
var data = [ 'a', 'b', 'c' ]
var set = new SetBuilder().addAll(data)
sp.set('set', set.build())

//print(sp.getInt('no')) //fixme
print(sp.getString('name'))
print(sp.getBoolean('b'))
print(sp.getStringSet('set'))