# 获取物品

**请务必先阅读[这一段](../item-creation/item-creation)。MMOItems物品生成系统非常复杂，需要一些时间才能完全理解。**

使用 /mi generate

------------------

*/mi generate* 命令允许您根据特定条件生成随机物品，这些条件作为命令参数提供。命令格式如下：**/mi generate (player) (extra-args)**。

*player* 指示将物品给予的玩家，同时也是物品构建时所基于的RPG玩家数据。例如，如果运行命令的玩家是20级，而命令目标是10级，那么物品不会根据20级生成，而是根据10级生成。

可用的命令参数如下所列：

| 参数                      | 用途                                                                                      |
|---------------------------|--------------------------------------------------------------------------------------------|
| -matchlevel               | 物品等级将匹配目标玩家的等级。                                                              |
| -matchclass               | 物品保证可以被目标玩家使用（职业要求）。                                                    |
| -gimme                    | 将物品添加到您的库存中，因此前述关于*(player)*的备注适用。                                   |
| -class:(class-name)       | 物品保证可以被指定职业使用。                                                                |
| -level:(integer)          | 物品保证具有指定的等级。                                                                    |
| -tier:(tier-name)         | 物品保证具有指定的[等级](../main-feature/item-tiers)。                                                       |
| -type:(item-type)         | 选择[物品类型](../item-creation/item-type)。                                                                 |
| -id:(id)                  | 如果您想选择特定的[物品生成模板](../item-creation/item-templates)，请使用此选项。                     |

请记住，这些参数都是可选的（这使得物品生成如此强大），并在MMOItems确定使用什么物品模板、等级和物品等级时充当“过滤器”。例如：我们为玩家提供一件符合他们等级和职业的高等级武器：

**/mi generate PlayerName -matchlevel -matchclass -tier:rare**

这通常用于打开可掠夺的箱子时；不像完成临时圣诞节/万圣节活动或杀死Boss/特殊怪物时的特定战利品，而是稀有的、随机但可用的物品（以免让他感到沮丧）。

如果您要向特殊怪物添加战利品，您很可能会指定您想要的掉落物品，而不是给任何等级，这样玩家需要通过刷怪物来最终找到同一武器的更高等级版本。

使用MythicMobs掉落表

------------------

正在开发中

使用MMOCore掉落表

------------------

如果您想向您的MMOCore掉落表中添加特定的物品生成模板，请使用以下MMOCore表项：

```yaml
gentemplate{id=TEMPLATE_ID;tier=TIER_NAME;level=<int>;match-level=<true/false>} <chance> <min-max>
```

| 参数                       | 用途                                            |
|----------------------------|------------------------------------------------|
| `id=TEMPLATE_ID`           | 选择您使用的物品生成模板。                      |
| `level=<item-level>`       | 强制物品等级在Y级左右。                         |
| `tier=ITEM_TIER_ID`        | 强制物品为X等级。                               |
| `match-level=<true/false>` | 物品等级将匹配玩家的等级。                       |

如果您不想使用特定的生成模板，而是根据特定条件从所有注册的模板中随机选择一个，请使用以下MMOCore表项。所有先前的参数（除了`id`）也可以在此格式中使用：

```yaml
miloot{type=ITEM_TYPE_ID;class=PLAYER_CLASS;match-class=<true/false>;tier=TIER_NAME;level=<int>;match-level=<true/false>} <chance> <min-max>
```

| 参数                       | 用途                                                                 |
|----------------------------|----------------------------------------------------------------------|
| `type=ITEM_TYPE_ID`        | 强制物品为特定类型。                                                 |
| `class="Class Name"`       | 物品保证可以被某个职业使用。                                         |
| `match-class=<true/false>` | 物品职业要求保证玩家可以使用。                                       |
