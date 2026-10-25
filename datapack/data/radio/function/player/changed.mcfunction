scoreboard players operation @s radio.prev = #old radio.tmp
function #radio:changed
execute if score @s radio.hear matches 1.. run function radio:music/stop_player
execute if score @s radio.mode matches 1 run title @s actionbar [{"text":"Рация: частота ","color":"green"},{"score":{"name":"@s","objective":"radio.channel"},"color":"yellow"}]
execute if score @s radio.mode matches 2 run title @s actionbar [{"text":"Радиостанция: частота ","color":"green"},{"score":{"name":"@s","objective":"radio.channel"},"color":"yellow"}]
execute if score @s radio.mode matches 0 run title @s actionbar {"text":"Радио: связь потеряна","color":"gray"}
execute if score @s radio.mode matches 1.. run playsound minecraft:block.note_block.bit master @s ~ ~ ~ 0.6 1.8
execute if score @s radio.mode matches 0 run playsound minecraft:block.note_block.bit master @s ~ ~ ~ 0.6 0.6
