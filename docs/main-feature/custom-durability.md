# 自定义耐久

自定义耐久系统是一个非常强大的功能，它允许您创建具有特定使用次数的物品，不像原版耐久度系统那样强制同类型物品始终具有相同的使用次数（除非使用“耐久”附魔，但这可能显得不太整洁）。
所有物品都可以拥有一个 _Max Durability_ 属性，该属性定义了物品在**破损**或**变得无法使用**之前可以使用的次数。_Lost when Broken_ 选项定义了物品在耐久度达到0时是否会丢失。

耐久度显示在物品的原版耐久度条上，您还可以使用PAPI占位符显示玩家手持物品的耐久度。物品只能使用修理消耗品进行修理。

由于自定义耐久度系统模拟了原版耐久度，因此在**1.13或更早版本**中无法再拥有带有耐久度的自定义纹理物品。1.14引入了一个 `CustomModelData` 标签，可以在物品具有任意耐久度的情况下应用自定义纹理，但由于最新的MMOItems耐久度系统覆盖了原版耐久度，因此在1.13中无法同时拥有这两个选项，这也是我们建议1.13用户更新到1.14或1.15的主要原因。

## 耐久度占位符

这些占位符不能在物品说明中使用！这些是可以在记分板、GUI或其他支持PlaceholderAPI的地方使用的占位符。

* **%mmoitems_durability%** 返回玩家手持物品的剩余使用次数。
* **%mmoitems_durability_max%** 返回物品的最大耐久度。
  ![image](https://i.imgur.com/TaumARR.png)
* **%mmoitems_durability_ratio%** 返回物品的耐久度比例（0%到100%）。
  ![image](https://i.imgur.com/90KnpS2.png)
* **%mmoitems_durability_bar_square%** 返回物品耐久度的进度条。
  ![image](https://i.imgur.com/HmS1wFR.png)
* **%mmoitems_durability_bar_diamond%** 返回耐久度条，但使用的字符是钻石。
  ![image](https://i.imgur.com/QPrLKtj.png)
* **%mmoitems_durability_bar_thin%** 返回一个更细的耐久度条。
  ![image](https://i.imgur.com/MJvhd6S.png)

## 物品说明中的耐久度

从6.5+版本开始，您可以在物品说明中显示物品的当前和最大耐久度。首先，您需要将以下行添加到您的说明格式中，尽管从6.5+版本开始默认已经添加：

``` yaml
- '#durability#'
```

然后将这些行添加到您的stats.yml中，这些行也是最近版本的默认配置文件的一部分：

``` yaml
durability: '&7Durability: {current} / {max}'
```

![image](https://i.imgur.com/InWJLD4.png)