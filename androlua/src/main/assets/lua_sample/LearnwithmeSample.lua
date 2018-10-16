--
-- @author Vove
-- 2018/8/11 0:16
-- LearnwithmeSample.lua
--

s = waitForVoiceParam()
if (s) then
    speak(s)
    else speak('没听清')
end