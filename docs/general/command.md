# 命令

| **物品命令**                                                     |                                                            |
| :----------------------------------------------------------- | :--------------------------------------------------------: |
| /mmoitems create (类型) (ID)                                   |              创建一个具有指定物品类型和物品ID的新物品。ID不是显示名称。               |
| /mi copy (类型) (当前ID) (新ID)                                   |          将你手中的物品复制为具有不同ID的新物品。如果需要大量生产，这个命令非常有用。           |
| /mi delete (类型) (ID)                                         |                          删除指定的物品。                          |
| /mi edit (类型) (ID)                                           |                          打开物品编辑器。                          |
| /updateitem                                                  |                         更新你手中的物品。                          |
| /updateitem (类型) (ID)                                        | 为特定物品开启自动[物品更新器](../item-management/item-updater)。不建议始终开启此功能，因为持续更新物品可能会导致延迟。 |
| /mi drop (类型) (物品ID) (世界) (x) (y) (z)                        |               生成并掉落物品（本质上是在不放入玩家库存的情况下生成物品）。               |
| /mi item identify                                            |                        手动鉴定你手中的物品。                         |
| /mi item repair                                              |                        手动修复你手中的物品。                         |
| /mi item unidentify                                          |                       手动取消鉴定你手中的物品。                        |
| /mi item deconstruct                                         |                        手动分解你手中的物品。                         |
| /mi give (类型) (物品) (玩家) (最小-最大) (未鉴定概率) (掉落概率) (灵魂绑定概率) (静默) |                          给玩家一个物品。                          |

| **物品管理**                             |                                                |
| :----------------------------------- | :--------------------------------------------: |
| /mi browse                           | 打开一个可交互的图书馆，展示我们创建的所有物品，按物品类型分类。（推荐用于大多数物品创建！） |
| /mi itemlist (类型)                    |                 列出特定物品类型的所有物品。                 |
| /mi allitems                         |          列出所有创建的物品，不如 /mi browse 有用。           |
| /mi giveall (类型) (物品) (最小-最) (未鉴定概率) |                  给所有在线玩家一个物品。                  |

| **合成站**                     |                              |
| :-------------------------- | :--------------------------: |
| /mi stations list           |         列出所有当前的合成站。          |
| /mi stations open (站点) (玩家) | 为指定玩家打开指定站点。这个命令用于将站点绑定到方块上。 |

| **实用工具/其他**                                       |                                                         |
| :------------------------------------------------ | :-----------------------------------------------------: |
| /mi ability (技能) (玩家) (mod1) (val1) (mod2) (val2) | 施放一个 MMOItems 技能（也适用于 MM 技能）。可以选择技能、施法的玩家，并修改伤害和冷却时间等值。 |
| /mi heal                                          |          类似于 essentials/CMI heal，治愈你并移除所有负面效果。          |
| /mi debug info (玩家)                               |          打开一个有用的聊天菜单，显示玩家信息，包括职业和魔力等状态（如果适用）。           |
| /mi list (类型/灵魂/技能)                               |                  显示所有可用的武器类型、法杖灵魂、实体等。                  |
| /mmotempstat (玩家) (属性名称) (数值) (持续时间)              |             给玩家提供临时属性提升，持续x刻。属性名称需大写并使用下划线。             |

| **通用命令**              |                            |
| :-------------------- | :------------------------: |
| /mmoitems reload      | 在编辑配置文件后重新加载整个插件。无需重启服务器！  |
| /mi reload (高级配方/合成站) | 重新加载插件或重新加载高级配方/合成站（如果适用）。 |

| **物品生成器**                |                              |
| :----------------------- | :--------------------------: |
| /mi generate (玩家) (额外参数) | 详见[获取物品](../item-management/obtaining-an-item)。 |
