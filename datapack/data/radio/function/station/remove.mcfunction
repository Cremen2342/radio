execute if score @s radio.serial matches 1.. run function radio:music/ended
kill @e[type=item_display,tag=radio.model,distance=..0.6]
kill @s
