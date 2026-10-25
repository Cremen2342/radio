scoreboard players operation #v radio.tmp = @s radio_freq
scoreboard players set #tgt radio.tmp 0
scoreboard players operation #tgt radio.tmp = @s radio.target
scoreboard players set @s radio_freq 0
scoreboard players set @s radio.target 0
execute unless score #v radio.tmp matches 1..99 run return run tellraw @s {"text":"Частота должна быть от 1 до 99.","color":"red"}
execute if score #tgt radio.tmp matches 2 if entity @e[type=marker,tag=radio.station,distance=..6] run return run function radio:station/set_freq
execute if score #tgt radio.tmp matches 1 if function radio:player/has_portable run return run function radio:player/set_portable_freq
execute if function radio:player/holds_portable run return run function radio:player/set_portable_freq
execute if entity @e[type=marker,tag=radio.station,distance=..6] run return run function radio:station/set_freq
tellraw @s {"text":"Возьмите в руку портативную рацию или подойдите к радиостанции (до 6 блоков).","color":"red"}
