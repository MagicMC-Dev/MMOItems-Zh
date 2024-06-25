# 复杂属性

随机生成的物品可以拥有**数值属性**，如攻击伤害、攻击速度、暴击几率，但你也可以添加更**复杂的属性**，比如技能或永久药水效果，这些效果的强度会随物品等级提升而提升。这些复杂属性的设置较为繁琐。

请记住，页面中的任何代码片段都要放在物品生成模板的`base`配置部分（对应模板的基础物品数据）或物品生成修饰符的`stats`配置部分：

``` yaml
ITEM_TEMPLATE_EXAMPLE:
    base:
        material: IRON_SWORD
        # <======== 放在这里
    modifiers:
        first-modifier:
            prefix: '修饰符前缀'
            stats:
                attack-damage: 3
                # <======= 或放在这里
```

## 技能

使用物品生成器，你可以创建具有特殊技能的物品，这些技能会随着物品等级的提升而变得更强。具体来说，技能有修饰符（如技能造成的伤害、药水效果的持续时间等），这些修饰符会根据物品等级进行缩放。使用以下格式向物品生成模板或修饰符中添加技能：

``` yaml
ability:
    first-ability-id:
        type: burn
        mode: on_hit

        # 第一个技能修饰符
        cooldown:
            base: 6
            spread: .1
            max-spread: .3

        # 第二个技能修饰符
        duration:
            base: 3
            scale: .2
    second-ability:
        type: life-ender
        mode: right-click
        damage:
            base: 10
            scale: 3
    third-ability:
        type: blizzard
        mode: left-click
        damage:
            base: 5
        cooldown:
            base: 7
            scale: 2
```

你会注意到，这种格式与为普通（非随机生成）MMOItems添加技能的格式完全相同。唯一的区别在于如何定义技能修饰符，因为这些修饰符的值是可以根据物品等级进行缩放的。定义技能修饰符所用的公式与定义[数值属性](../item-creation/item-stats-and-options)的公式相同。

在这个例子中，“击中触发的燃烧技能”有6秒的冷却时间，但这个值会在平均±10%的范围内浮动，最大相对偏移为10%。燃烧持续6秒，但这个持续时间会随着每个物品等级增加0.2秒。

注意，如果你想指定一个技能修饰符但不希望其缩放，你仍然需要指定一个基础值，并将缩放比例和/或最大偏移设置为0，或完全删除它们。参考上面的`third-ability`示例。在这个例子中，暴风雪技能的伤害将始终为5。然而，冷却时间仍然会按指定的方式进行缩放。

## 食用消耗品获得的药水效果

请记住，药水效果由三部分组成：药水效果类型、持续时间和药水等级。

``` yaml
effects:
    speed:
        level:
            base: 1
            scale: 1
        duration:
            base: 10
            scale: 3
```

你会发现，当处理随机生成的物品时，`base/scale/spread/maxspread`格式的配置几乎随处可见。像其他数值一样，药水的持续时间和等级也可以随物品等级进行缩放。

由于这些是数值属性，如果你希望等级每次都相同，也可以使用这种格式：

``` yaml
effects:
    speed:
        level: 1
        duration: ...
```

这与以下格式完全相同：

``` yaml
effects:
    speed:
        level:
            base: 1
            scale: 0
            spread: 0
            max-spread: 0
        duration: ...
```

你可以在[这里](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/potion/PotionEffectType.html)查看Minecraft的药水效果列表。

## 永久药水效果

与消耗品药水效果完全相同（见上文），但由于这些药水效果在特定物品被持有时应永久给予玩家，你**无需指定效果持续时间**。`level`配置部分也被去掉了，因为这样更简洁。

``` yaml
perm-effect:
    speed:
        base: 1
        scale: 1
    # 这种格式也是可以的
    haste: 3
```

## 附魔

附魔由附魔类型和等级定义。使用以下格式：

``` yaml
enchants:
    efficiency:
        base: 1
        # 不存在效率1.3，但这意味着每10个等级，物品将额外获得一级效率附魔！
        # 这同样适用于药水效果等级。
        scale: .1
    # 这种格式也可以，因为附魔等级是一个数值
    sharpness: 10
```

## 物品元素属性

``` yaml
element:
    fire:
        defense: 
            base: 10
            scale: 3
        # 这种格式同样适用！
        damage: 10
    water:
        defense: 50
```

## 消耗品恢复能力

恢复能力定义了消耗品使用时恢复多少生命值、饥饿值和饱和度。

``` yaml
restore:
    health:
        base: 3
        scale: 2
        spread: .3
        max-spread: .5
    # 你可以为食物使用复杂的公式，也可以简单使用这种格式
    food: 5
    saturation: 3
```
