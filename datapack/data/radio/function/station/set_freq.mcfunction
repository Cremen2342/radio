scoreboard players operation @e[type=marker,tag=radio.station,distance=..6,sort=nearest,limit=1] radio.freq = #v radio.tmp
tellraw @s [{"text":"Частота радиостанции: ","color":"green"},{"score":{"name":"#v","objective":"radio.tmp"},"color":"yellow"}]
function radio:player/update
