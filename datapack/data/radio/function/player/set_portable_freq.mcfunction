scoreboard players operation @s radio.freq = #v radio.tmp
tellraw @s [{"text":"Частота рации: ","color":"green"},{"score":{"name":"@s","objective":"radio.freq"},"color":"yellow"}]
function radio:player/update
