summon marker ~ ~ ~ {Tags:["radio.station","radio.new"]}
scoreboard players set @e[type=marker,tag=radio.new,distance=..0.1] radio.freq 1
scoreboard players set @e[type=marker,tag=radio.new,distance=..0.1] radio.active 0
scoreboard players set @e[type=marker,tag=radio.new,distance=..0.1] radio.rsu 0
scoreboard players set @e[type=marker,tag=radio.new,distance=..0.1] radio.rsd 0
scoreboard players set @e[type=marker,tag=radio.new,distance=..0.1] radio.serial 0
scoreboard players set @e[type=marker,tag=radio.new,distance=..0.1] radio.disc 0
scoreboard players set @e[type=marker,tag=radio.new,distance=..0.1] radio.jt -1
tag @e[type=marker,tag=radio.new,distance=..0.1] remove radio.new
summon item_display ~ ~ ~ {Tags:["radio.model"],item:{id:"minecraft:stick",count:1,components:{"minecraft:item_model":"radio:station_radio"}},item_display:"none",brightness:{sky:14,block:14},transformation:{left_translation:[0f,0f,0f],left_rotation:[0f,0f,0f,1f],scale:[1.01f,1.01f,1.01f],right_rotation:[0f,0f,0f,1f]}}
scoreboard players set #found radio.tmp 1
tellraw @s [{"text":"Радиостанция установлена. ","color":"green"},{"text":"Постройте антенну: 5 громоотводов столбом на верхней грани. Частота 1: меняется ПКМ пустой рукой или редстоуном (медная лампа рядом: +1, окисленная: -1).","color":"gray"}]
