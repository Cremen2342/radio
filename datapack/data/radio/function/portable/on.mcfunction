scoreboard players set @s radio.on 1
playsound minecraft:block.note_block.bit master @s ~ ~ ~ 0.8 1.6
execute if items entity @s armor.head minecraft:copper_helmet run title @s actionbar {"text":"Рация включена","color":"green"}
execute unless items entity @s armor.head minecraft:copper_helmet run title @s actionbar {"text":"Рация включена, но нужен медный шлем (антенна)","color":"yellow"}
function radio:player/update
