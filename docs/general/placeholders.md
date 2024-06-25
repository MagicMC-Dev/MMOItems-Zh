# Placeholders

所有占位符在插件加载时自动注册，并需要 **PlaceholderAPI** 才能正常工作。你无需运行任何命令来初始化这些占位符。请注意，你可以通过 `{placeholderapi_mmoitems_...}` 从 MVdWPlaceholderAPI 访问 PAPI 占位符。

如果你找不到某个占位符，请查看 [**MythicLib 占位符**](https://gitlab.com/phoenix-dvpmt/mythiclib/-/wikis/Placeholders)！

## 属性

|                     **占位符**                     |                 **描述**                 |
| :---------------------------------------------: | :------------------------------------: |
|            `%mmoitems_stat_<属性名称>%`             | 返回格式化后的玩家[属性](../item-creation/item-stats-and-options)值 |
| `%mmoitems_stat_<元素名称>_<伤害/伤害百分比/弱点/防御/防御百分比>%` |                返回元素属性值                 |

## 类型

|            **占位符**             |           **描述**            |
| :----------------------------: | :-------------------------: |
| `%mmoitems_type_<物品类型>_name%`  |  返回指定[物品类型](../item-creation/item-type)的名称  |
| `%mmoitems_type_<物品类型>_total%` | 返回指定[物品类型](../item-creation/item-type)的物品总数 |

## 耐久度

|               **占位符**               |      **描述**       |
| :---------------------------------: | :---------------: |
|       `%mmoitems_durability%`       |   返回当前物品的剩余耐久度    |
|     `%mmoitems_durability_max%`     |   返回当前物品的最大耐久度    |
|    `%mmoitems_durability_ratio%`    |   返回当前物品的耐久度百分比   |
| `%mmoitems_durability_bar_square%`  | 返回当前物品的耐久度条（方块样式） |
| `%mmoitems_durability_bar_diamond%` | 返回当前物品的耐久度条（钻石样式） |
|  `%mmoitems_durability_bar_thin%`   | 返回当前物品的耐久度条（细条样式） |

（详见[耐久度信息](../main-feature/custom-durability)）

## 杂项

|            **占位符**             |    **描述**     |
| :----------------------------: | :-----------: |
|    `%mmoitems_tier_<物品等级>%`    |   返回指定等级的名称   |
| `%mmoitems_ability_cd_<技能名称>%` | 返回指定技能的当前冷却时间 |
