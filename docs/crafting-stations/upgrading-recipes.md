# 升级配方概述

升级配方是一种特定类型的站点配方。它们允许任何玩家使用材料来升级特定的物品。这些配方还支持配方条件和其他配方选项，如`hide-when-locked`。在了解如何设置升级配方之前，您应该先学习[制作站](../crafting-stations/crafting-stations)。

升级配方目前不能设置制作时间。

## 升级配方示例

``` yaml
recipes:
    steel-sword-upgrade:
        item:
            type: SWORD
            id: STEEL_SWORD
        conditions:
        - 'level{level=5}'
        ingredients:
        - 'mmoitem{type=MATERIAL,id=STEEL_INGOT,amount=4}'
```

此升级配方允许玩家升级名为`钢剑`的物品。此类配方**不**支持制作时间，因此它们必须全部是即时的，就像使用消耗品一样。要使用此配方，玩家必须至少达到5级，并且必须在其库存中有4个钢锭。\
当然，玩家在使用配方时还必须拥有一把钢剑，否则将收到错误消息提示。

![image](https://i.imgur.com/F1ugbnJ.png)

## 配置说明

### `item`

这是玩家使用升级配方时将升级的MMOItem。您需要指定物品的类型和ID。

### `conditions`

这些是玩家在使用升级配方时需要满足的条件。在这个例子中，玩家必须达到至少5级。

### `ingredients`

这些是玩家在使用升级配方时需要拥有的材料。在这个例子中，玩家需要4个钢锭。

### 示例说明

在这个示例中，配方允许玩家将`钢剑`升级。玩家必须满足5级的条件，并且在其库存中拥有4个钢锭。配方不支持制作时间，因此它是即时生效的。玩家使用配方时，必须同时拥有一把钢剑，否则将无法完成升级。
