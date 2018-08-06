--
-- Created by IntelliJ IDEA.
-- User: SYSTEM
-- Date: 2018/8/1
-- Time: 0:21
-- testIntent.lua
--
i = Intent(Intent.ACTION_VIEW)
i.setData(Uri.parse('tel:10086'))
i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
print(i)
app.startActivity(i)

