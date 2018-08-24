--
-- @author 17719247306
-- 2018/8/22 22:14
-- NeteasePlaySample.lua
-- 网易云播放歌曲
--

require 'accessibility'

print(actionCount)
print(currentActionIndex)

smartOpen('com.netease.cloudmusic')
search = ViewFinder().desc({ '搜索', 'Search' }).await()
search.tryClick()

ViewFinder().id('search_src_text').await().setText(args[1])

-- fixme
a = ViewFinder().containsText({ '"' .. args[1] .. '"', '“' .. args[1] .. '”' }).await()
a.tryClick()
sleep(2000) -- TODO  show
s = ViewFinder().id('a02').findFirst()
s.tryClick()


