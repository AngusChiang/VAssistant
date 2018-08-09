--
-- @author 17719
-- 2018/8/7 2:54
-- qq_send_msg.lua
--

i = ViewFinder().id('input').waitFor()
i.setText(args[1])
if (alert('确认发送?', '')) then
    sleep(300)
    clickById('fun_btn')
end