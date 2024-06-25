# 配方触发器

配方触发器是配方选项，不会在配方GUI物品中显示。它们决定了当玩家使用配方时执行的动作。

``` yaml
recipes:
    steel-sword:
        ....
        triggers:
        - 'vanilla{type=STICK,amount=3}'
```

## 可用的触发器类型

| 触发器    | 描述                                   | 格式/示例                                                  |
|------------|--------------------------------------|-----------------------------------------------------------|
| message    | 向玩家发送消息。                      | `message{format="&a您的消息内容... "}`                     |
| command    | 让控制台执行命令。                    | `command{format="give dirt",sender=OP}` *- 可用发送者: PLAYER, CONSOLE, OP* |
| sound      | 向玩家播放声音。                      | `sound{sound=<SOUND_NAME>;volume=<VOLUME>;pitch=<PITCH>}` |
| vanilla    | 给玩家一个原版物品。                  | `vanilla{type=DIAMOND;amount=3}`                          |
| mmoitem    | 给玩家一个MMOItem。                   | `mmoitem{type=SWORD;id=FALCON_BLADE;amount=1}`            |
| mmskill    | 施放一个MythicMobs技能。              | `mmskill{id=MythicMobsSkillInternalName}`                 |
| experience | 给玩家MMOCore经验。                   | `exp{profession=<PROFESSION>;amount=<AMOUNT>}`            |
