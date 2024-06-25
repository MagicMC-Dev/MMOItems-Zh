# 技能

技能是可以绑定到物品上的独特技能，使它们更加独特。一个重要的配置功能是技能触发/施放模式，即您需要执行的动作才能施放技能。从 `MMOItems 6.6.2` 开始，这些技能触发与 `MMOCore` 共享；完整列表可以在[这里](https://gitlab.com/phoenix-dvpmt/mythiclib/-/wikis/Trigger%20Types)找到。

![image](/9.png)

使用正确的技能触发，您可以在玩家击杀实体时或在您射出的箭矢落地时施放技能。

**伤害**技能可以造成**物理**或**魔法**伤害。根据技能类型，技能伤害可以通过特定的物品属性增加，分别是`Physical Damage`或`Magical Damage`。物理技能对应于利用物品或武器的技能，如 _圆形斩击_ 或 _投掷物品_ 。魔法技能对应于元素/魔法技能，如_火焰箭_、_火焰彗星_等。

## 使用MythicMobs向MMOItems添加技能

从 `MMOItems 6.7` 开始，自定义技能在 `MythicLib` 中处理。请参阅[此Wiki页面](https://gitlab.com/phoenix-dvpmt/mythiclib/-/wikis/Using%20MythicMobs)。

## 编辑技能

进入`MMOItems/skills`文件夹，找到对应于您想编辑的技能的YML文件。在本例中，我们将考虑`arcane-rift.yml`：

``` yaml
name: Arcane Rift
modifier:
  duration:
    name: Duration
    default-value: 1.5
  damage:
    name: Damage
    default-value: 5.0
  mana:
    name: Mana
    default-value: 0.0
  stamina:
    name: Stamina
    default-value: 0.0
  cooldown:
    name: Cooldown
    default-value: 10.0
  timer:
    name: Timer
    default-value: 0.0
  amplifier:
    name: Amplifier
    default-value: 2.0
  speed:
    name: Speed
    default-value: 1.0
```

您可以编辑技能名称，这是将技能绑定到物品时显示在物品说明中的名称。您还可以修改每个技能修饰符的名称和默认值。这些数值是在将技能添加到物品时，如果在物品编辑器GUI中未指定的情况下，MMOItems将考虑的数值。
