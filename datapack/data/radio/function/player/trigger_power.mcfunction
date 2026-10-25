scoreboard players set @s radio_power 0
execute unless function radio:player/has_portable run return run tellraw @s {"text":"У вас нет портативной рации.","color":"red"}
execute if score @s radio.on matches 1 run return run function radio:portable/off
function radio:portable/on
