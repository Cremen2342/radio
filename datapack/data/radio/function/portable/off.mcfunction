scoreboard players set @s radio.on 0
playsound minecraft:block.note_block.bit master @s ~ ~ ~ 0.8 0.6
title @s actionbar {"text":"Рация выключена","color":"gray"}
function radio:player/update
