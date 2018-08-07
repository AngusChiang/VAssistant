--
-- @author 17719
-- 2018/8/7 2:54
-- qq_send_msg.lua
--

i = ViewFinder().id('input').waitFor()
i.setText(args[1])
alert('确认发送?', '')
sleep(500)
clickById('fun_btn')