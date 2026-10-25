execute if score @s radio.freq matches ..0 run scoreboard players set @s radio.freq 1
execute store result storage radio:ui f int 1 run scoreboard players get @s radio.freq
data modify storage radio:ui d set value "$(freq)"
scoreboard players set @s radio.target 1
function radio:ui/show_portable with storage radio:ui
