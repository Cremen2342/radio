scoreboard players operation #sid radio.tmp = @s radio.serial
scoreboard players operation #f radio.tmp = @s radio.freq
tag @a[distance=..60] add radio.near
execute as @a[tag=!radio.near] unless score @s radio.hear matches 1.. if score @s radio.channel = #f radio.tmp run function radio:music/start_player
tag @a[tag=radio.near] remove radio.near
