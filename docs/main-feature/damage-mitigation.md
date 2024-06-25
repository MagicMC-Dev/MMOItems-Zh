# 伤害减免

在MMOItems中，伤害减免分为三种机制：**格挡、闪避和招架**。在主插件**MythicLib**的配置文件（_mitigation_ 配置部分）中，可以编辑页面中的任何数值。为了增强与 `MMOCore` 的兼容性，伤害减免功能被移到了 `MythicLib` 中。

每当攻击伤害因减免而降低时，玩家面前会出现粒子效果，并且会在操作栏（或通过聊天）发送一条消息给他。

## 格挡

当格挡近战或投射攻击时，玩家会显著减少所受伤害。格挡几率和格挡能力（所能阻挡的伤害百分比）都可以通过物品来增加。格挡能力有默认值和上限，意味着如果玩家没有任何物品增加额外的格挡能力，他至少能阻挡20%的伤害。格挡能力不能超过75%。格挡攻击的几率由 _Block Rating_ 属性决定。

``` yaml
STEEL_BREASTPLATE:
    material: IRON_CHESTPLATE
    block-power: 10
    block-rating: 5
```

## 闪避

当闪避近战或投射攻击时，玩家可以完全**免疫**所受伤害并快速向后冲刺，帮助他逃离战斗。闪避攻击的几率上限为80%。

``` yaml
SWIFT_LEATHER_BOOTS:
    material: LEATHER_BOOTS
    dodge-rating: 10
```

## 招架

与闪避类似，招架可以完全免疫攻击伤害并将攻击者击退。
击退力可以在配置文件中进行编辑。

``` yaml
DWARVEN_SHIELD:
    material: SHIELD
    parry-rating: 10
```

## 减免冷却时间减少

每个减免属性还有一个冷却时间减少属性。默认情况下，玩家不能在几秒内多次闪避、招架或格挡攻击。这些冷却时间属性减少了这种延迟，如果玩家的健康值较低，这会非常有用。

``` yaml
ROGUE_AMULET:
    material: RED_DYE
    dodge-cooldown-reduction: 40
```
