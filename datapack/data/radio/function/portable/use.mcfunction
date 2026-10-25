advancement revoke @s only radio:use_portable
execute unless function radio:player/holds_portable run return 0
execute if score @s radio.use matches 1.. run return run scoreboard players set @s radio.use 3
scoreboard players set @s radio.use 3
execute if predicate radio:sneaking run return run function radio:ui/open_portable
execute if score @s radio.on matches 1 run return run function radio:portable/off
function radio:portable/on
