# 物品模板

物品生成模板是生成随机物品的最基本工具。它们由一组无论如何生成的物品都会具备的 **默认物品属性** 和一组 **物品修饰符** 组成，这些修饰符会被随机挑选并应用于生成的物品，从而影响其稀有度。

``` yaml
LONG_SWORD:
    # 基本模板选项
    option:
        tiered: true
        level-item: true
        roll-modifier-check-order: false
        capacity: # 从MI 6.9.5+开始
            base: 10
            scale: 3
    # 基础物品数据
    base:
        material: IRON_SWORD
        name: '&f长剑'
        attack-speed: 1.6
        attack-damage:
            base: 6
            scale: 1.2
        required-level:
            base: 0
            scale: 1
    # 模板修饰符
    modifiers: 
        sharp:
            chance: 0.3
            prefix: '&f锋利'
            stats:
                attack-damage: 3
                lore:
                - '&7非常锋利！'
```

`base` 配置部分对应基础物品属性。例如，基础物品是一把铁剑，名称为 `长剑` 。默认攻击速度为1.6，武器有6点攻击伤害，每提升一级增加1.2点。

`option` 配置部分用于为模板配置一些额外选项。详见下段内容。

物品生成模板位于 `/MMOItems/items` 文件夹中。你可以在该文件夹中添加任意数量的YML配置文件来整理你的模板。

## 物品模板选项

以下是为物品模板提供的一些额外选项。除了容量以外，将其设置为。`true` 以启用它们。

| 模板选项                      | 描述                                                                                               |
| ------------------------- | ------------------------------------------------------------------------------------------------ |
| roll-modifier-check-order | 在物品生成时打乱物品修饰符列表                                                                                  |
| tiered                    | 如果在生成物品时未指定随机等级，则会为你的物品选择一个随机等级。这适用于`/mi generate`命令，也适用于工作台合成配方！                                |
| level-item                | 如果在生成物品时未指定随机等级，则会为你的物品选择一个随机物品等级。                                                               |
| capacity                  | `capacity`从MMOItems 6.9.5开发版本开始可用。它可以用于为你的物品强制设定一个修饰符容量公式，而无需使用物品等级。它覆盖默认的修饰符容量公式以及生成物品的等级提供的公式。 |
