scoreboard players set #found radio.tmp 1
scoreboard players set @s radio.target 2
execute store result storage radio:ui f int 1 run scoreboard players get @e[type=marker,tag=radio.station,distance=..0.1,limit=1] radio.freq
data modify storage radio:ui d set value "$(freq)"
data modify storage radio:ui s set value "не собрана, нужно 5 громоотводов столбом сверху"
execute if entity @e[type=marker,tag=radio.station,scores={radio.active=1},distance=..0.1] run data modify storage radio:ui s set value "работает"
function radio:ui/show_station with storage radio:ui
