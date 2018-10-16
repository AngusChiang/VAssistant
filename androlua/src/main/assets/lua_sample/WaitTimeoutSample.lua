--
-- @author Vove
-- 2018/8/19 10:57
-- WaitTimeoutSample.lua
--
function time()
    for i = 1, 5 do
        sleep(900)
        print(i)
    end
end
thread(time)

-- smartOpen('支付宝')  --将此处取消注释可查看不同结果
result = waitForApp('com.eg.android.AlipayGphone', 3000)
print('等待结果', result)

