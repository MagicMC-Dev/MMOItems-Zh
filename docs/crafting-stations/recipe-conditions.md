# 配方条件概述

与材料一样，必须满足条件才能使用配方。与材料不同，条件在使用配方时不会从玩家库存中取走任何物品。主要的配方条件包括**等级**条件和**权限**条件。与材料一样，条件存储在配方配置部分的列表中。

## 配方条件示例

``` yaml
recipes:
    steel-sword:
        conditions:
        - 'level{level=5,consume=true}'
        - 'permission{list="mmoitems.recipe.steel-sword,mmoitems.recipe.station.steel"}'
        - 'placeholder{placeholder="%ac_Stat_Weight%~>~1"}'
```

| 条件类型   | 用法                                                                              | 描述                                                                             |
| ------ | ------------------------------------------------------------------------------- | ------------------------------------------------------------------------------ |
| 最低等级   | `level{level=(min>,consume=(true/false>}`                                       | 玩家必须达到或高于X级。`consume`表示等级是否会被消耗。                                               |
| 职业     | `class{list=(Class Name>,(Class Name 2>...}`                                    | 将配方限制在某些职业中使用                                                                  |
| 权限     | `permission{list="(perm1>,(perm2>...";display="...."}`                          | 仅具有特定权限的玩家可以使用该配方。`display`表示条件在物品说明中的显示方式                                     |
| 占位符    | `placeholder{placeholder="(Placeholder>~(Comparator>~(Number>";display="...."}` | 检查玩家的占位符值，然后将其与您选择的数字进行比较。比较符号列表：(, (=, >, >=, ==, =, !=，或者当比较字符串时可以使用`equals` |
| 食物     | `food{amount=(amount>}`                                                         | 配方消耗（并要求）X食物。                                                                  |
| 法力     | `mana{amount=(amount>}`                                                         | 配方消耗（并要求）X法力。                                                                  |
| 耐力     | `stamina{amount=(amount>}`                                                      | 配方消耗（并要求）X耐力。                                                                  |
| 货币     | `money{amount=(amount>}`                                                        | 配方花费（并要求）X Vault货币。                                                            |
| 最低职业等级 | `profession{profession=(profession>,level=(min>}`                               | 玩家必须在某个职业中达到或高于X级（MMOCore）。                                                    |

## 配置示例

在这个示例中，配方允许玩家制作`钢剑`，但必须满足以下条件：

1. 玩家等级至少为5，并且使用配方时等级会被消耗。
2. 玩家必须具有特定的权限才能使用配方。
3. 玩家占位符值必须满足条件。

## `hide-when-locked`选项

`hide-when-locked`配方选项仅在至少一个条件不满足时触发。如果玩家没有所有所需的材料，配方仍会显示在GUI中。配方条件显示在GUI配方物品说明的开头：

![image](https://i.imgur.com/xPwlm5B.png)
