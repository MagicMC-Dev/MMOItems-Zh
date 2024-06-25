# 物品掉落

数据存储在主插件文件夹中的`drops.yml`文件中。以下是一个配置示例：

``` yaml
monsters:
    ZOMBIE:
        rock-table:
            coef: 7
            items:
                CONSUMABLE:
                    ROCK: 50,1-3,10
        coin-table:
            coef: 1
            items:
                MISCELLANEOUS:
                    GOLD_COIN: 1,1-10,0
```

掉落表分为**子表**。当读取掉落表时，会从所有子表中随机选择一个，然后从该子表中掉落物品。拥有多个子表可以实际防止同时生成两个物品。

例如，如果您希望僵尸掉落自定义的铁剑或自定义的皮革胸甲，由于同时掉落这两种物品的几率会太高，您可以将僵尸的掉落表分为两个子表。在第一个子表中放置铁剑，在第二个子表中放置皮革胸甲。这样，这两种物品就不会同时生成。

子表可以具有不同的子表系数。系数越高，子表被选中的几率越高。计算子表被选中几率的方法很简单。如果在一个掉落表中，第一个子表的系数为 `1`，第二个子表的系数为 `2`，那么第一个子表有 `1/3` 的几率被选中。子表的选择几率可以用这个公式计算：`chance = <subtable-coefficient> / <sum-of-all-subtable-coefficients>`

怪物部分对应于实体死亡时掉落的物品。以下是配置模板：

``` yaml
MOB_NAME:
    first-subtable:
        coef: <subtable-coefficient>
        items:
            ITEM_TYPE: (not case sensitive):
                ITEM_ID: (drop-chance),(min)-(max),(unidentified-chance)
            ITEM_TYPE:
                ITEM_ID: (drop-chance),(min)-(max),(unidentified-chance)
                etc.
            etc.
    second-subtable:
        coef: <subtable-coefficient>
        items:
            ITEM_TYPE: (not case sensitive):
                  ITEM_ID: (drop-chance),(min)-(max),(unidentified-chance)
                  etc.
            etc.
    etc.
```

_blocks_ 配置部分对应于玩家破坏/挖掘方块时掉落的物品。遵循与怪物部分相同的格式，但将生物名称替换为方块类型名称。方块类型可以在 [这里](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Material.html) 找到（1.13）。

**您可以为每个子表项配置三个值：**

* **掉落几率** - 物品掉落的几率。
* **最小值和最大值** - 掉落数量在这两个值之间随机选择。
* **未鉴定几率** - 物品掉落时未鉴定的几率。

**子表的附加选项：**

* **disable-silk-touch** - 设置为true时，如果使用精准采集镐挖掘方块，将阻止子表掉落任何物品。这可以防止像矿石这样的方块出现物品重复漏洞。

``` yaml
blocks:
    DIAMOND_ORE:
        rare-diamond:
            coef: 1
            disable-silk-touch: true
            items:
                MATERIAL:
                    RARE_DIAMOND: 100,2-3,0
```

## 将MMOItems添加到MythicMobs掉落表

将MMOItems添加到MythicMobs掉落表非常简单。自4.5版本起，使用MI创建的物品可以使用以下格式在MM掉落表中召唤：

``` yaml
TestDropTable:
  Conditions:
  - playerwithin 100
  Drops:
  - mmoitems{type=SWORD;id=CUTLASS} 1 .1
```

这告诉掉落表添加来自MMOItems的名为 _CUTLASS_ 的剑，掉落几率为10%（即在MM掉落表中的0.1）。您可以像原版掉落一样更改要掉落的物品数量。如果您希望物品掉落时未鉴定，需要在括号内添加一个选项：这意味着物品掉落时有30%的几率未鉴定：`mmoitems{type=SWORD;id=CUTLASS;unidentified=0.1} 1 .1`

注意：`type`和`id`值必须为大写。

缺点是您不能像使用MMOItems子表那样直接每次掉落一个物品。
