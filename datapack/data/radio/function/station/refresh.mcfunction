scoreboard players set @s radio.active 0
execute if block ~ ~1 ~ #radio:antenna if block ~ ~2 ~ #radio:antenna if block ~ ~3 ~ #radio:antenna if block ~ ~4 ~ #radio:antenna if block ~ ~5 ~ #radio:antenna run scoreboard players set @s radio.active 1
execute if score @s radio.active matches 1 run particle minecraft:electric_spark ~ ~5.6 ~ 0.15 0.15 0.15 0.02 1
function radio:music/scan
