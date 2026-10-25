advancement revoke @s only radio:clicked_block
execute if predicate radio:sneaking run return 0
execute if items entity @s weapon.mainhand * run return 0
scoreboard players set #found radio.tmp 0
scoreboard players set #ray radio.tmp 0
execute anchored eyes positioned ^ ^ ^ run function radio:station/ray_open
