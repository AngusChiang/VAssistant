
if runtime.DEBUG then
    argMap['s']='调试标志'
end
print(argMap)
print(argMap['k'])

argMap['k'] = 134

print(argMap['k'])
