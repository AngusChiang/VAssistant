--
-- @author 17719247306
-- 2018/8/22 22:14
-- NeteasePlaySample.lua
-- 网易云播放歌曲
--

require 'accessibility'

smartOpen('com.netease.cloudmusic')
search = ViewFinder().desc({ '搜索', 'Search' }).await()
search.tryClick()

s = ViewFinder().id('search_src_text').await()
s.setText(args[1])

bounds = s.getBounds() --(154, 87 - 1055, 185)[1920x1080]
x = (bounds.left + bounds.right) / 2
y = bounds.bottom + 100 --276
sleep(2000)
click(x, y)
