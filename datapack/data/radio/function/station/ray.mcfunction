execute if data block ~ ~ ~ components."minecraft:custom_data"{radio:"station"} align xyz positioned ~0.5 ~0.5 ~0.5 unless entity @e[type=marker,tag=radio.station,distance=..0.1] run function radio:station/register
scoreboard players add #ray radio.tmp 1
execute if score #found radio.tmp matches 0 if score #ray radio.tmp matches ..60 positioned ^ ^ ^0.1 run function radio:station/ray
