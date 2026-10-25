execute unless score @s radio.channel matches -2147483648..2147483647 run scoreboard players set @s radio.channel 0
execute if score @s radio.freq matches ..0 run scoreboard players set @s radio.freq 1
scoreboard players set #new radio.tmp 0
scoreboard players set #mode radio.tmp 0
execute if score @s radio.on matches 1 if items entity @s armor.head minecraft:copper_helmet if function radio:player/has_portable run scoreboard players set #mode radio.tmp 1
execute if score #mode radio.tmp matches 1 run scoreboard players operation #new radio.tmp = @s radio.freq
execute if score #mode radio.tmp matches 0 as @e[type=marker,tag=radio.station,scores={radio.active=1},distance=..6,sort=nearest,limit=1] run function radio:player/from_station
scoreboard players operation #old radio.tmp = @s radio.channel
scoreboard players operation @s radio.channel = #new radio.tmp
scoreboard players operation @s radio.mode = #mode radio.tmp
execute unless score @s radio.channel = #old radio.tmp run function radio:player/changed
