scoreboard players set #up radio.tmp 0
scoreboard players set #dn radio.tmp 0
execute unless score @s radio.rsu matches 0.. run scoreboard players set @s radio.rsu 0
execute unless score @s radio.rsd matches 0.. run scoreboard players set @s radio.rsd 0
execute if block ~1 ~ ~ #radio:tune_up[powered=true] run scoreboard players set #up radio.tmp 1
execute if block ~1 ~ ~ #radio:tune_down[powered=true] run scoreboard players set #dn radio.tmp 1
execute if block ~-1 ~ ~ #radio:tune_up[powered=true] run scoreboard players set #up radio.tmp 1
execute if block ~-1 ~ ~ #radio:tune_down[powered=true] run scoreboard players set #dn radio.tmp 1
execute if block ~ ~ ~1 #radio:tune_up[powered=true] run scoreboard players set #up radio.tmp 1
execute if block ~ ~ ~1 #radio:tune_down[powered=true] run scoreboard players set #dn radio.tmp 1
execute if block ~ ~ ~-1 #radio:tune_up[powered=true] run scoreboard players set #up radio.tmp 1
execute if block ~ ~ ~-1 #radio:tune_down[powered=true] run scoreboard players set #dn radio.tmp 1
execute if block ~ ~-1 ~ #radio:tune_up[powered=true] run scoreboard players set #up radio.tmp 1
execute if block ~ ~-1 ~ #radio:tune_down[powered=true] run scoreboard players set #dn radio.tmp 1
execute if score #up radio.tmp matches 1 if score #dn radio.tmp matches 0 if score @s radio.rsu matches 0 run function radio:station/step_up
execute if score #dn radio.tmp matches 1 if score #up radio.tmp matches 0 if score @s radio.rsd matches 0 run function radio:station/step_down
scoreboard players operation @s radio.rsu = #up radio.tmp
scoreboard players operation @s radio.rsd = #dn radio.tmp
