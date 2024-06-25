# 物品修饰符

**请确保首先阅读** [**这个页面**](../item-creation/item-creation)**。MMOItems的物品生成系统相当复杂，需要一些时间才能完全理解。**

模板修饰符（或称 _name modifiers_）让你在使用物品生成器时可以进一步随机化你的物品。修饰符是 **物品属性包**，在生成物品时有一定概率被添加到物品上。

## 概念

修饰符是附加到物品模板上的属性包。例如，一个随机化武器的典型修饰符可能是 `锋利` 修饰符，它会给武器增加+3攻击伤害，或 `剧毒` 修饰符，它会为武器添加击中时的中毒能力。单个修饰符不足以完全定义一个物品，因为它只为基础物品添加了一些额外属性。

一些修饰符比其他修饰符更强大/更稀有，因此应该有不同的选择几率……一些修饰符甚至只能通过特定的物品等级获得。

## 创建物品修饰符

在创建生成模板时，你需要指定修饰符列表：

``` yaml
# 示例物品
LONG_SWORD:
    base: ...

    # 有一定几率被应用的修饰符
    modifiers:

        # 第一个修饰符
        sharp:
            weight: 2.5
            prefix: '&f锋利'
            stats:
                attack-damage: 3
                lore:
                - '&7非常锋利！'

        # 第二个修饰符
        fiery:
            weight: 5
            prefix:
                format: '&c火焰'
                priority: 1
            stats:
               ability:
                   on-hit:
                       type: burn
                       mode: on_hit
```

如 [此页面](../item-creation/item-creation) 所述，一个物品可以生成多个修饰符。修饰符由四个选项定义：

- **选择几率**
- **修饰符权重**
- **物品属性列表**
- **前缀/后缀**（均称为 _name modifiers_）

## 前缀/后缀

在物品生成过程中选择修饰符时，其前缀/后缀会自动应用到物品上。如果添加了多个前缀/后缀，MMOItems 只保留优先级最高的前缀/后缀，例如，具有前缀优先级 **0**（默认优先级为 **0** ）的 `锋利` 和优先级 **1** 的 `火焰` 的物品只会显示 `火焰`。如果你不想使用前缀/后缀优先级系统，只需使用以下格式定义前缀/后缀：

``` yaml
modifier:
    prefix: '&f锋利'
```

而不是

``` yaml
modifier:
    prefix:
        format: '&f锋利'
        priority: <integer-needed>
```

## 权重

修饰符权重是应用修饰符时从生成的物品中扣除的容量。如果你的物品初始容量为5，它不会收到权重为6的修饰符。由于修饰符容量由物品等级直接决定，一些等级的物品无法拥有特定的修饰符。

``` yaml
modifier:
    weight: 5
```

请参阅[此维基部分](../main-feature/item-tiers)来设置物品的修饰符容量。

## 选择几率

这是一个较小但易于操作的选项，用于平衡你的修饰符。你可以设置修饰符，即使生成的物品有足够的修饰符容量来接收修饰符，仍然有X%的几率不会应用修饰符。使用以下格式：

``` yaml
modifier:
    chance: 0.1 # 对应10%的应用几率
```

## 修饰符属性

这些是修饰符被选中时将应用于物品的属性，即如果生成的物品有足够的修饰符容量并且修饰符选择几率测试成功。此处定义属性所用的格式与定义[物品模板](../item-creation/item-templates)的基础物品数据的格式完全相同。

``` yaml
toxic:
    prefix: '&2剧毒'
    stats:
        ability:
            on-hit:
                type: poison
                type: on-hit
        fire-damage-reduction: 5
        lore:
        - '好烫！！'
```

## 公共修饰符

公共修饰符是你可以用来节省配置文件时间的快捷方式。进入 `/modifiers` 文件夹并创建一个新的YAML配置文件（可以有任何名称）。同一个YAML配置文件可以包含多个模板修饰符。例如，你可以输入以下内容：

``` yaml
toxic:
    prefix: '&2剧毒'
    stats:
        ability:
            on-hit:
                type: poison
                type: on-hit
        fire-damage-reduction: 5
        lore:
        - '好烫！！'
```

然后你可以在任何其他物品配置中引用此修饰符，使用有限的行数：

``` yaml
KATANA:
    base: ...
    modifiers:
        toxic:
            weight: 2.5 # 仍然可以编辑
            chance: .1  # 仍然可以编辑
            # 无需其他内容！
        another-modifier: ...
```

## 修饰符组（MI 6.9.5+）

修饰符组极大地增加了物品模板的可配置性。通过将修饰符打包成组，你可以确保在生成物品时，至少/最多会应用N个来自预定列表的修饰符。你还可以让组递归调用其他修饰符组，进一步增加可能性。

进入你的/modifiers配置文件夹，在任何YAML配置文件中输入以下代码片段。在此代码片段中，定义了一个包含三个_子修饰符_的修饰符组：`剧毒`、`锋利` 和 `火焰`。min和max选项表示至少会有1个来自该组的修饰符应用到物品上，最多会有3个修饰符应用到物品上。这两个选项是可选的，如果只指定min选项，那么理论上所有修饰符都可以同时应用（取决于几率和权重）。

``` yaml
example_modifier_group:
    min: 1    # 可选
    max: 3    # 可选
    chance: 1 # 默认1
    weight: 1 # 默认0
    modifiers:
        toxic: 1
        sharp: 10
        fiery: 2
```

这个修饰符组可以像简单模板修饰符一样在物品配置中使用，例如下例所示，MMOItems几乎不区分简单修饰符和修饰符组。你可以覆盖公共修饰符组配置中的min和max选项，

``` yaml
KATANA:
    base: ...
    modifiers:
        some_modifier: ...
        another_modifier: ...
        example_modifier_group:
            chance: 1           # 默认1
            weight: 1           # 默认0
            min: 1              # 可选
            max: 2              # 可选
```

还有其他更复杂的格式可以使用，以下是总结所有格式的代码片段：

``` yaml
LONG_SWORD:
    base: ...
    modifiers:
        first_modifier:                # 在模板定义中私有定义修饰符。
            weight: 2.5
            prefix: '&f锋利'
            stats:
                attack-damage: 3
                lore:
                - '&7非常锋利！'
        example_modifier_group:        # 在模板定义中私有定义组。
            min: 1                     # 可选
            max: 3                     # 可选
            chance: 1                  # 默认1
            weight: 1                  # 默认0
            modifiers:
                bleed: 1               # 引用公共修饰符
                blunt: 10              # 引用公共修饰符
                fiery:                 # 在私有定义的组中私有定义修饰符。
                    weight: 5          # 在模板定义中的组定义。
                    prefix:
                        format: '&c火焰'
                        priority: 1
                    stats:
                        ability:
                            on-hit:
                                type: burn
                                mode: on_hit
                modifier_group:        # 在组中使用组。
                    min: 1
                    max: 2
                    modifiers:
                        water: 1
                        fire: 1
                        earth: 1

KATANA:
    base: ...
    modifiers:
        public_modifier_group: {}       # 对公共修饰符组的最简单引用
```
