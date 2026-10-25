execute unless block ~ ~ ~ minecraft:jukebox run return 0
execute if data block ~ ~ ~ components."minecraft:custom_data"{radio:"station"} run return 0
execute unless data block ~ ~ ~ RecordItem run return 0
scoreboard players set #found radio.tmp 1
execute if data block ~ ~ ~ RecordItem.components."minecraft:custom_data" run return 0
execute store result score #jt radio.tmp run data get block ~ ~ ~ ticks_since_song_started
function radio:music/identify
execute if score #disc radio.tmp matches 0 run return 0
execute if score @s radio.jt matches -1 run scoreboard players operation @s radio.jt = #jt radio.tmp
execute unless score #jt radio.tmp = @s radio.jt run scoreboard players set #pl radio.tmp 1
execute if score #jt radio.tmp < @s radio.jt run scoreboard players set #restart radio.tmp 1
scoreboard players operation @s radio.jt = #jt radio.tmp
