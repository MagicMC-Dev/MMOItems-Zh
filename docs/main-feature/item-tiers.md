# 物品层

## 物品分解

具有等级（普通、稀有、传奇等）的物品可以**分解**。分解物品会将其转换为另一种物品，通常是玩家可以在高级工作台上使用来合成其他物品的材料。玩家从分解物品中获得的物品完全取决于物品的等级。玩家可能会获得其他武器，甚至是可以用来合成其他武器的材料。

这个功能实际上允许玩家处理他们无法使用的物品，因为他们不满足要求，同时还能使用另一种资源（因为分解物品需要一个消耗品）。

玩家可以通过拖放特定的消耗品到他们的物品上来分解物品。这个消耗品必须启用_Can Deconstruct_选项。成功分解后，玩家会听到升级的声音，并收到一条消息，提示他们的物品已成功分解。

默认情况下，等级包括_垃圾、普通、不常见、稀有、非常稀有、传奇、神话、史诗、魔法和独特_。您可以在主插件文件夹中的tiers.yml配置文件中编辑这些等级。您可以更改等级名称和分解物品掉落表。此掉落表的行为与怪物/方块掉落表完全相同，请参考[**这个Wiki页面**](https://gitlab.com/phoenix-dvpmt/mmoitems/-/wikis/Item%20Drop%20Tables)了解如何设置它们。

``` yaml
RARE:
    name: '&6&lRARE'
    deconstruct-item:
        success:
            coef: 1
            items:
                MATERIAL:
                    RARE_WEAPON_ESSENCE: 100,1-1,0
        lose:
            coef: 3
            items:
                MATERIAL:
                    WEAPON_POWDER: 100,1-1,0
```

如GIF所示，分解物品实际上可能会产生不同的掉落，这意味着在分解有等级的物品时，某些物品可能比其他物品更难获得。

![image](https://i.imgur.com/zH6OKO9.gif)

## 物品发光和提示

您可以使用两种功能显示掉落在地上的稀有物品：**物品提示**，这会使用原版实体名称（通过全息显示）功能显示武器/物品的名称，以及**物品发光**，这会使您的物品在掉落时发出发光效果（原版药水效果）。

``` yaml
RARE:
    name: '&6&lRARE'
    item-glow:
        hint: true
        color: 'GRAY'
```

这两个选项都取决于掉落物品的等级。因此，不同的等级可能具有不同的提示选项和不同的发光颜色。

**您必须安装GlowAPI和PacketListenerAPI。**

您可以通过在等级配置文件中切换`item-glow.hint`选项来启用提示功能。移除此配置部分或将其设置为`false`以禁用它。`item-glow.color`选项允许您设置物品的发光颜色。您可以通过配置文件中提供的链接访问颜色列表。

![image](https://i.imgur.com/aNIW7av.png)

## 未鉴定的物品

物品等级还定义了未鉴定物品的一些显示选项，如以下配置模板所示：

``` yaml
EPIC:
    name: '&4&lEPIC'
    unidentification:
        name: 'Epic'
        range: 6
        prefix: '&4'
```

物品等级不定义未鉴定物品的模式（物品类型会，参见这个[Wiki部分](../item-creation/item-type)）。`name`选项对应于未鉴定物品说明中显示的等级名称。`range`选项对应于未鉴定物品的等级范围，仅在物品具有`Required Level`选项时显示。
等级范围为玩家提供了额外的信息，让他知道武器的大致等级。由于他还可以看到物品等级，他可以决定是否要鉴定物品。`prefix`选项用于为未鉴定物品的显示名称和物品说明中显示的等级名称添加颜色前缀。

![image](https://i.imgur.com/4IuCQ72.png)

## 修饰容量（物品生成）

**确保您先了解** [**物品生成**](https://gitlab.com/phoenix-dvpmt/mmoitems/-/wikis/Item%20Creation#how-items-work-very-important)**，** [**物品模板**](https://gitlab.com/phoenix-dvpmt/mmoitems/-/wikis/Item%20Templates) **和** [**物品修饰**](https://gitlab.com/phoenix-dvpmt/mmoitems/-/wikis/Item%20Modifiers)**。**

以下是默认item-tiers.yml的一个示例，我们将逐步解析如何为您的物品等级设置修饰容量。

``` yaml
UNCOMMON:
    ....
    generation:
        chance: 0.15
        capacity:
            base: 6
            
            # 随物品等级增加
            scale: .1
            
            spread: .1
            max-spread: .3
```

`generation.chance`选项确定在生成随机物品时选择您物品等级的几率。如果设置为0.15，您的物品将有15%的几率成为不常见。

`generation.capacity`配置部分定义了等级修饰容量，其工作方式与常规的[数值属性公式](../item-creation/item-stats-and-options)类似。公式基本如下：`capacity = <base> + <item level> * <scale>`，您可以加上+/-`<spread>`%偏移，最大偏移为`<max-spread>`%。

例如，使用上面配置示例中的容量公式，假设我们正在生成一个12级物品：`average-capacity = 6 + 0.1 * 12 = 7.2`。由于有30%的最大偏移，容量最终值将在`7.2的70% = 5.04`和`7.2的130% = 9.36`之间随机选择，平均偏移+/-10%（相对于7.2的平均值）。
