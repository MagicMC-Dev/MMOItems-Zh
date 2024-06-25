# 物品套装

物品套装允许您将多个物品链接在一起，使它们在同时佩戴时变得更强。物品套装在玩家穿戴足够数量的同一套装物品时会给予额外的属性加成。配置文件相当直观；以下是一个配置示例。

``` yaml
ARCANE:
    name: '&2奥术套装'
    bonuses:
        '3':
            magic-damage: 20
        '4':
            max-mana: 30
            potion-speed: 1
    lore-tag:
    - '&7奥术套装加成:'
    - '&8[3] +20% 魔法伤害'
    - '&8[4] 30 最大法力'
    - '&8[4] 永久速度 I'
```

## 概述

使用 _lore-tag_ 选项可以在物品说明中显示套装加成。您可以在列表中添加任何内容，但通常我会具体给出物品提供的额外属性。 `bonuses` 部分定义了物品套装提供的加成。带数字的子部分对应玩家穿戴一定数量的套装物品时获得的属性。

## 特殊全套加成

全套装可以赋予玩家属性、技能、永久药水效果、权限以及粒子效果。

### 永久药水效果

套装加成可以赋予玩家**永久药水效果**。您需要指定药水效果名称（可以在[这里](https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/potion/PotionEffectType.html)找到名称）和药水等级（需要是整数）。您需要使用以下格式：`potion-<potion-effect-name>: <permanent-level>`。使用`potion-`前缀将指示MMOItems设置永久药水效果。

``` yaml
SPEED:
    name: '&2速度套装'
    bonuses:
        '4':
            potion-speed: 1
            ......
```

### 技能

最后但同样重要的是，您还可以设置**技能**作为全套装加成。设置非常简单，与物品技能的格式相同。在以下示例中，任何持有至少2件Hatred套装物品的玩家将在击中任何实体时暂时获得一个生命终结者技能，该技能有30秒冷却时间。只需确保使用`ability-`前缀，这表示您希望设置永久技能。

``` yaml
HATRED:
    name: '&c憎恨套装'
    bonuses:
        '2':
            ability-1:
                type: LIFE_ENDER
                cooldown: 30
                mode: ON_HIT
            ......
```

### 粒子效果

粒子效果的前缀是`particle-`。您可以在该前缀后面随意填写内容。

``` yaml
HATRED:
    name: '&c憎恨套装'
    bonuses:
        '2':
            particle-whatever_you_want_here:
                type: GALAXY
                particle: FLAME
                # 具体粒子类型（此例中为GALAXY）的修饰符
                height: 1
                speed: 1
           ......
```

您可以在 [这里](../item-creation/item-stats-and-options) 的 _物品粒子_ 子部分找到更多关于 MMOItems 粒子效果的信息。

### 权限

您可以提供一个权限列表，当玩家达到特定数量的套装物品时，这些权限将授予给玩家。

``` yaml
HATRED:
    name: '&c憎恨套装'
    bonuses:
        '2':
            granted-permissions:
              - 'hatred.fullset'
              - 'whatever.permission'
            ......
```
