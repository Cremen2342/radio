scoreboard players remove @s radio.freq 1
execute if score @s radio.freq matches ..0 run scoreboard players set @s radio.freq 99
function radio:station/tuned
