# 物品升级

物品升级是为您的物品添加一些进展的方式。成功升级物品时，其属性会增加，从而使物品变得更好。玩家可以使用合适的消耗品或通过使用站台**升级配方**来升级他们的物品。
升级时，物品会增加一个等级。等级显示在物品显示名称旁边。可以在主插件配置文件中更改显示名称后缀（_item-upgrading.name-suffix_）。确保您的物品具有自定义显示名称，否则后缀将不会显示。

![imagd](https://i.imgur.com/OyrqMYw.png)

要拥有一个有效的物品升级系统，需要设置三件事：首先，您需要设置升级模板，它决定了成功升级物品时增加哪些属性（增加多少）。然后，您需要为要升级的物品设置一些额外的升级选项。最后一步是设置升级消耗品或[升级配方](Upgrading-Recipes)。

## 设置升级模板

升级模板可以在**upgrade-templates.yml**配置文件中设置。这一步相当简单，但可能需要在配置中花费一些时间来平衡您的升级设置。打开您的模板配置文件：

``` yaml
# 模板ID，仅用于参考。这是
# 在编辑GUI中输入的文本，
# 以便为特定物品设置物品升级。
weapon-default:

    # 每次物品升级，其攻击伤害
    # 增加3%（相对于当前值）
    attack-damage: 3%
    
    # 每次升级，其绝对暴击几率
    # 增加2%（独立于当前值）
    critical-strike-chance: 2
    
    pve-damage: 1%

armor-template-example:
    armor: 3%
    armor-toughness: 2%
    block-rating: 5%
    dodge-rating: 5%
    parry-rating: 5%

another-example:
    attack-damage: 0.5%    
    critical-strike-chance: 0.1%
    critical-strike-power: 0.2%
```

配置文件中的每个配置部分对应一个**升级模板**。您可以添加任意数量的模板，甚至可以为每个物品添加一个模板（尽管多个物品可以使用相同的升级模板）。设置完成后，您可以将这些模板绑定到特定物品，以确定它们在升级时的行为。让我们逐步阅读配置的含义。

**weapon-template-example:** 这是一个用于武器的升级模板示例。每次物品升级，武器的攻击伤害将增加3%（即当前攻击伤害将乘以1.03），因为输入以`%`结尾。每次物品升级，武器还会获得2%的暴击几率（不相对于当前暴击几率，但由于暴击几率是以百分比表示，因此物品仍然获得2%的暴击几率），以及1%的PvE伤害，相对于其当前攻击速度。

**armor-default:** 使用此升级模板升级时，物品将获得3%的护甲、2%的韧性、5%的格挡、闪避和招架评级，相对于其当前属性，因为有一个`%`。

---

升级模板中指定的`%`与属性本身无关。它仅表示升级时物品获得的属性值相对于物品之前的属性值。例如，对于一把8攻击伤害的剑，获得3%的攻击伤害（相对）与获得3攻击伤害（绝对）不同。

让我们看另一个例子。假设我们有一把**10攻击伤害的剑**正在升级，绑定到**weapon-default**升级模板。这把剑将获得其当前伤害的3%，因此最终将有`10 + (3% * 10) = 10 + 10 * 0.03 = 10.3攻击伤害`，以及2%的暴击几率，因为它之前没有任何暴击几率，而每次升级获得的暴击几率独立于当前属性值。剑没有任何PvE伤害，因此相对于其当前值获得1%的PvE伤害不会增加PvE伤害，因为1%的0仍然是0。

---

_目前，物品升级仅适用于如暴击几率、攻击伤害、护甲、PvE伤害等数值属性。_

## 设置消耗品的升级选项

玩家可以使用消耗品来升级物品。如果是这样，您需要指定两个选项：**升级参考**，稍后我们会回到这个选项，以及**升级几率**。您可以在编辑菜单中配置这两个选项。

## 设置武器/护甲等的升级选项

任何物品，包括护甲、武器等都可以升级，但您需要设置一些选项以升级物品。就像消耗品一样，武器/护甲也有一个升级几率。武器和消耗品的升级几率叠加，即`total-upgrade-chance = <item-upgrade-chance> * <consumable-upgrade-chance>`。如果两种物品都有40%的升级几率，则总升级几率为`40% * 40% = 16%`。当物品的升级几率不是100%时，您可以启用一个额外选项，使物品在升级失败时**破碎**。

以下是使用`another-example`升级模板的物品示例。

``` yaml
EXAMPLE:
  base:

    # 基础物品选项
    material: IRON_SWORD
    name: §e默认剑

    # 物品升级定义
    upgrade:
      workbench: true # true表示只能在使用合成站时升级
      template: another-example # 升级模板ID
      max: 100 # 最大升级次数
      success: 70.0 # 升级成功率
      destroy: false # 升级失败时销毁物品

    # 其他物品选项
    attack-damage: 20.0
    critical-strike-chance: 2.0
    critical-strike-power: 5.0
    unbreakable: true
```

物品还可以有一个**最大升级次数**，即物品达到特定等级后不能再升级。这是一个重要选项，如果您不希望特定物品通过升级变得过于强大。物品也可以仅通过合成站和升级配方进行升级。

不要忘记使用编辑GUI中的相应选项将升级模板绑定到您的物品！前面描述的选项也可以通过物品编辑GUI设置。

## 升级参考

此选项可用于升级消耗品和可升级物品。此选项可用于限制特定升级消耗品与特定物品的兼容性。升级参考是一个文本，不显示在说明中，但用于MI检查消耗品是否与给定物品兼容进行升级。

如果两个物品的参考匹配，则消耗品可以用于升级物品。如果第一个物品有非空参考，而另一个物品没有参考，它们不兼容。任何具有通用升级参考`all`的物品与任何其他物品兼容。

## Config.yml

您可以修改主MI配置文件，以调整属性更改是否以及如何显示在任何升级物品的说明中。以下是您可能需要查看并可能修改的部分：

``` yaml
item-upgrading:

    # 升级物品的显示名称后缀。
    name-suffix: '&f &e(+#lvl#)'

    # 是否在物品名称或说明中显示
    # 如果允许玩家重命名物品，请禁用。
    # 如果设置为“false”，请记得
    # 在物品说明中包含%upgrade_level%。
    display-in-name: true

    # 属性更改在属性后缀中的格式。
    stat-change-suffix: ' &8(<p>#stat#&8)'
    stat-change-positive: '&a'
    stat-change-negative: '&c'

    # 是否在说明中显示
    # 更改了哪些属性。
    # 实验功能
    display-stat-changes: false
```

`<p>`表示`stat-change-negative`和`stat-change-positive`。您可以完全删除它，或者如果您想更改其位置，也可以更改！

## 升级配方

使用合成站，您可以创建合成配方，要求玩家拥有特定材料并满足某些条件。使用这种配方时，玩家可以升级他们的装备。请查看此[Wiki页面](../main-feature/custom-recipes)以获取更多信息。

以下是实现其中一个升级配方的合成站示例：

``` yaml
name: '升级 (#page#/#max#)'
max-queue-size: 10
sound: ENTITY_EXPERIENCE_ORB_PICKUP
layout: default
items:
    fill:
        material: AIR
    no-recipe:
        material: RED_STAINED_GLASS_PANE
        name: '&a没有新内容'
    no-queue-item:
        material: GREEN_STAINED_GLASS_PANE
        name: '&a排队升级'
recipes:
    dnck1: # 一些升级配方
        item:
            type: SWORD
            id: EXAMPLE
        crafting-time: 0
        ingredients:      
        - mmoitem{type=MATERIAL,id=UPGRADESTONE,amount=1}
```

在上面的材料列表中使用的消耗品（必须放在`material.yml`配置文件中）。基本上这是一个仅作为合成站升级配方材料的物品。

``` yaml
UPGRADESTONE:
  base:
    material: HEART_OF_THE_SEA
    name: '&e升级石'
    lore:
    - '&7&o使用它来升级你的装备'
    disable-crafting: true
    disable-smelting: true
    disable-smithing: true
    disable-enchanting: true
    disable-repairing: true
```
