execute unless score @s radio.serial matches 0.. run scoreboard players set @s radio.serial 0
execute unless score @s radio.disc matches 0.. run scoreboard players set @s radio.disc 0
execute unless score @s radio.jt matches -1.. run scoreboard players set @s radio.jt -1
execute unless score @s radio.active matches 1 run return run function radio:music/silence
scoreboard players set #found radio.tmp 0
scoreboard players set #pl radio.tmp 0
scoreboard players set #restart radio.tmp 0
scoreboard players set #disc radio.tmp 0
execute positioned ~1 ~ ~ run function radio:music/probe
execute if score #found radio.tmp matches 0 positioned ~-1 ~ ~ run function radio:music/probe
execute if score #found radio.tmp matches 0 positioned ~ ~ ~1 run function radio:music/probe
execute if score #found radio.tmp matches 0 positioned ~ ~ ~-1 run function radio:music/probe
execute if score #found radio.tmp matches 0 positioned ~ ~-1 ~ run function radio:music/probe
execute if score #found radio.tmp matches 0 run return run function radio:music/silence
execute if score #pl radio.tmp matches 1 if score @s radio.serial matches 1.. unless score @s radio.disc = #disc radio.tmp run function radio:music/ended
execute if score #pl radio.tmp matches 1 if score #restart radio.tmp matches 1 if score @s radio.serial matches 1.. run function radio:music/ended
execute if score #pl radio.tmp matches 1 if score @s radio.serial matches 0 run function radio:music/begin
execute if score #pl radio.tmp matches 0 if score @s radio.serial matches 1.. run function radio:music/ended
execute if score #pl radio.tmp matches 1 run function radio:music/broadcast
