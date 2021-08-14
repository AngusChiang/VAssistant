
-- 所有应用包名
print(shell.execWithAdb("pm list package"))
-- 输出sdcard下所有目录
print(shell.execWithAdb("cd /sdcard && ls -a"))