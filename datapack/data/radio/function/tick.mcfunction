scoreboard players remove @a[scores={radio.use=1..}] radio.use 1
execute as @a[scores={radio_freq=1..}] at @s run function radio:player/trigger_freq
execute as @a[scores={radio_power=1..}] at @s run function radio:player/trigger_power
scoreboard players enable @a radio_freq
execute as @a[scores={radio_menu=1..}] at @s run function radio:player/trigger_menu
scoreboard players enable @a radio_power
scoreboard players enable @a radio_menu
execute as @e[type=marker,tag=radio.station] at @s run function radio:station/tick
execute as @a[scores={radio.left=1..}] run function radio:player/rejoined
execute as @e[type=item_display,tag=radio.model] at @s unless data block ~ ~ ~ components."minecraft:custom_data"{radio:"station"} run kill @s
scoreboard players add #t radio.tmp 1
execute if score #t radio.tmp matches 10.. run function radio:cycle
