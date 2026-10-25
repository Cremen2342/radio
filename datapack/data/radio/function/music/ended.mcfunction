scoreboard players operation #sid radio.tmp = @s radio.serial
execute as @a if score @s radio.hear = #sid radio.tmp run function radio:music/stop_player
scoreboard players set @s radio.serial 0
