advancement revoke @s only radio:placed_station
scoreboard players set #found radio.tmp 0
scoreboard players set #ray radio.tmp 0
execute anchored eyes positioned ^ ^ ^ run function radio:station/ray
