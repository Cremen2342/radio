scoreboard players set #t radio.tmp 0
execute as @e[type=marker,tag=radio.station] at @s run function radio:station/refresh
execute as @a at @s run function radio:player/update
