scoreboard players set @s radio_menu 0
execute if function radio:player/holds_portable run return run function radio:ui/open_portable
execute if entity @e[type=marker,tag=radio.station,distance=..6] run return run function radio:ui/open_station_near
tellraw @s {"text":"Возьмите в руку портативную рацию или подойдите к радиостанции (до 6 блоков).","color":"red"}
