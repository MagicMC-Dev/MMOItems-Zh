# 物品属性设置

## 剑/武器属性示例

``` yaml
WIKISWORD:  
  base:  
    material: NETHERITE_SWORD
```

> material字段用于插入你希望物品成为的Minecraft材质类型，这意味着它支持你当前MC版本的所有有效材质枚举。

``` yaml
    revision-id: 2
```

> revision-id字段定义了你物品的当前“版本”。你可以根据需要随意更改，任何玩家拥有的物品，如果其版本ID与编辑器中设置的不匹配，都会在交互时更新为最新版本。

``` yaml
    durability: 100.0
```

> durability字段虽然名字有些误导，但它是1.14自定义模型数据之前用于自定义纹理的旧方法。它设置物品的当前耐久度，不影响实际的最大耐久度。

``` yaml
    custom-model-data: 5.0
```

> 这是自定义模型数据字段，不言自明。

``` yaml
    max-durability: 1000.0
```

> max-durability字段设置此物品的自定义耐久度。

``` yaml
    will-break: true
```

> 该物品在耐久度为0时是否会破损，或者仅仅是变得不可用？

``` yaml
    name: '&cThis is a test sword.'
```

> 物品的显示名称。

``` yaml
    lore:
    - reeee
    - fffffffff
```

> 物品的描述，以列表形式给出，当然支持颜色代码。

``` yaml
    custom-nbt:
    - NBT_TAG_KEY TAG_VALUE
```

> 你可以在此设置任意自定义的NBT标签以支持外部插件，或使用Minecraft的can-place系统等。

``` yaml
    lore-format: lore-format
```

> 在这里可以输入来自lore-formats文件夹的自定义描述格式文件，这样不同类型的物品可以有不同的格式。极度自定义。

``` yaml
    displayed-type: Fake Type Here
```

> 可以在此显示自定义的物品类型名称，而不是实际的物品类型，例如将“SWORD”显示为“Kings Sword”或其他名称。

``` yaml
    enchants:
      sharpness: 1.0
      mending: 2.0
```

> 此物品上的附魔列表，采用缩进格式。

``` yaml
    hide-enchants: true
```

> 是否隐藏附魔效果，仅使物品发光。

``` yaml
    permission:
    - useme.sword
```

> 使用此物品所需的权限。

``` yaml
    item-particles:
      type: FIREFLIES
      particle: FLAME
      amount: 5.0
      radius: 1.4
      speed: 1.0
      rotation-speed: 2.0
      height: 2.0
```

> 物品粒子效果列表，所有有效类型可以通过游戏内命令list查看，粒子效果则为Minecraft的所有有效粒子类型。不同粒子有不同的修饰符，具体哪些有效需要使用游戏内编辑器进行测试。

``` yaml
    disable-interaction: true
```

> 是否禁用与该物品的交互，例如放置方块。

``` yaml
    disable-crafting: true
```

> 该物品是否可以用于合成配方？

``` yaml
    disable-smelting: true
```

> 该物品是否可以用于熔炼？

``` yaml
    disable-smithing: true
```

> 该物品是否可用于锻造？

``` yaml
    disable-enchanting: true
```

> 是否禁用该物品的附魔？

``` yaml
    disable-repairing: true
```

> 该物品是否可修复/重命名？

``` yaml
    disable-attack-passive: true
```

> 该物品的MMOItems钝击/挥砍/穿刺攻击被动效果是否被关闭。

``` yaml
    required-level: 5.0
```

> 从兼容的RPG插件或原版经验等级中要求的等级。

``` yaml
    required-class:
    - ARCHER
```

> 从兼容的RPG插件中要求的职业。

``` yaml
    attack-damage: 10.0
```

> 设置武器的基础攻击伤害，或射箭时的箭伤害。

``` yaml
    attack-speed: 10.0
```

> 物品的攻击速度，单位是每秒攻击次数。

``` yaml
    critical-strike-chance: 5.0
```

> 发生暴击的几率。

``` yaml
    critical-strike-power: 5.0
```

> 暴击造成的额外倍率，与MythicLib config.yml中的基础倍率结合。

``` yaml
    block-power: 5.0
```

> 该物品能阻挡的伤害百分比。

``` yaml
    block-rating: 5.0
```

> 实际阻挡的几率，与MythicLib config.yml中的基础几率结合。

``` yaml
    block-cooldown-reduction: 5.0
```

> 从MythicLib config.yml中的基础值额外减少阻挡冷却时间。

``` yaml
    dodge-rating: 5.0
```

> 完全闪避攻击的几率。

``` yaml
    dodge-cooldown-reduction: 5.0
```

> 从MythicLib config.yml中的基础值额外减少闪避冷却时间。

``` yaml
    parry-rating: 5.0
```

> 招架攻击的几率，成功招架会抵消伤害并击退攻击者。

``` yaml
    parry-cooldown-reduction: 5.0
```

> 从MythicLib config.yml中的基础值额外减少招架冷却时间。

``` yaml
    cooldown-reduction: 5.0
```

> 减少物品和玩家技能的冷却时间（百分比）。

``` yaml
    mana-cost: 5.0
```

> 使用该物品的法力消耗。

``` yaml
    stamina-cost: 5.0
```

> 使用该物品的耐力消耗。

``` yaml
    pve-damage: 5.0
```

> 对非人类实体造成的额外伤害（百分比）。

``` yaml
    pvp-damage: 5.0
```

> 对人类实体造成的额外伤害（百分比）。

``` yaml
    weapon-damage: 5.0
```

> 额外基础武器伤害（百分比）。

``` yaml
    skill-damage: 5.0
```

> 额外的MMOItems技能伤害（百分比）。

``` yaml
    projectile-damage: 5.0
```

> 额外的技能/武器投射物伤害。

``` yaml
    magic-damage: 5.0
```

> 额外的魔法技能和法杖伤害（百分比）。

``` yaml
    physical-damage: 5.0
```

> 额外的技能/武器物理（近战）伤害。

``` yaml
    defense: 100.0
```

> 提供的防御力，公式在MythicLib config.yml中设置。

``` yaml
    damage-reduction: 5.0
```

> 总体伤害减免（百分比）。

``` yaml
    fall-damage-reduction: 5.0
```

> 减少摔落伤害（百分比）。

``` yaml
    projectile-damage-reduction: 5.0
```

> 减少投射物伤害（百分比）。

``` yaml
    physical-damage-reduction: 5.0
```

> 减少物理（近战）伤害（百分比）。

``` yaml
    fire-damage-reduction: 5.0
```

> 减少火焰伤害（百分比）。

``` yaml
    magic-damage-reduction: 5.0
```

> 减少魔法伤害（百分比）。

``` yaml
    pve-damage-reduction: 5.0
```

> 减少来自怪物的伤害（百分比）。

``` yaml
    pvp-damage-reduction: 5.0
```

> PVP受到的伤害减少（百分比）。

``` yaml
    undead-damage: 5.0
```

> 增加对亡灵生物的伤害（百分比）。

``` yaml
    unbreakable: true
```

> 该物品是否不可破坏？

``` yaml
    tier: RARE
```

> 设置物品的品质等级。

``` yaml
    set: STARTER
```

> 设置物品所属的套装。

``` yaml
    armor: 5.0
```

> 设置物品的护甲值，视觉上限为20，功能上限为30。

``` yaml
    armor-toughness: 5.0
```

> 护甲韧性，这是一个有点复杂的属性公式。

``` yaml
    max-health: 10.0
```

> 增加最大生命值，累加。

``` yaml
    unstackable: true
```

> 该物品是否可堆叠？

``` yaml
    max-mana: 20.0
```

> 增加最大法力值，类似于增加最大生命值。

``` yaml
    knockback-resistance: 0.7
```

> 抵抗击退的几率，0.7即为70%。

``` yaml
    movement-speed: 0.2
```

> 增加玩家的移动速度，原版默认值为0.1。

``` yaml
    two-handed: true
```

> 该物品是否为双手武器，也就是说不能在副手使用其他物品。

``` yaml
    equip-priority: 5.0
```

> 新的装备优先级属性，详细信息请参阅单独的Wiki页面。

``` yaml
    perm-effects:
      POISON: 1.0
```

> 给予持有者的永久效果。为什么要毒性1？我也不知道。

``` yaml
    granted-permissions:
    - tempperm.ree
```

> 持有/穿戴时给予的临时权限。

``` yaml
    item-cooldown: 10.0
```

> 适用于消耗品和物品命令的冷却时间，单位为秒。

``` yaml
    crafting:
      shaped:
        '1':
        - diamond AIR AIR
        - AIR diamond AIR
        - AIR AIR netherite_block
      shapeless:
        '1':
        - AIR
        - AIR
        - AIR
        - AIR
        - nether_star
        - AIR
        - AIR
        - slime_ball
        - AIR
      furnace:
        '1':
          item: COAL
          time: 200
          experience: 10.0
      blast:
        '1':
          item: COAL
          time: 200
          experience: 10.0
      smoker:
        '1':
          item: COAL
          time: 200
          experience: 10.0
      campfire:
        '1':
          item: COD
          time: 100
          experience: 10.0
      smithing:
        '1':
          input1: DIAMOND_SWORD
          input2: DIAMOND
```

> 所有的合成配方类型。

``` yaml
    craft-permission: youneedthis.tocraft
```

> 合成权限。

``` yaml
    crafted-amount: 10.0
```

> 合成该物品的数量。

``` yaml
    sounds:
      on-attack:
        sound: entity.generic.drink
        volume: 1.0
        pitch: 1.0
      on-pickup:
        sound: entity.generic.drink
        volume: 1.0
        pitch: 1.0
      on-consume:
        sound: entity.generic.drink
        volume: 1.0
        pitch: 1.0
      on-right-click:
        sound: entity.generic.drink
        volume: 1.0
        pitch: 1.0
      on-left-click:
        sound: entity.generic.drink
        volume: 1.0
        pitch: 1.0
      on-item-break:
        sound: entity.generic.drink
        volume: 1.0
        pitch: 1.0
      on-block-break:
        sound: entity.generic.drink
        volume: 1.0
        pitch: 1.0
      on-craft:
        sound: entity.generic.drink
        volume: 1.0
        pitch: 1.0
      on-placed:
        sound: entity.generic.drink
        volume: 1.0
        pitch: 1.0
```

> 添加自定义声音。

``` yaml
    element:
      fire:
        damage: 10.0
      earth:
        defense: 10.0
```

> 元素伤害/防御。这个系统正在重新设计中。

``` yaml
    commands:
      cmd0:
        format: -d1 bc Hello, this is a test command.
        delay: 0.0
        op: true
      cmd1:
        format: -d1 bc Hello, this is a test command from console.
        delay: 0.0
        console: true
```

> 右键单击物品时添加命令。

``` yaml
    gem-sockets:
    - RED
    - GREEN
    - Poop
```

> 添加任意宝石插槽以匹配你的宝石。

``` yaml
    repair-type: EXAMPLE_REPAIR_STAT
```

> 此修复类型属性需要与用于修复的消耗品相匹配，或者留空以匹配所有。

``` yaml
    ability:
      ability1:
        type: BLOODBATH
        mana: 3.0
        mode: RIGHT_CLICK
```

> 这是技能格式，类似于粒子效果，请使用GUI配置所有可能的修改器。

``` yaml
    upgrade:
      workbench: true
```

> 请参阅维基页面上的物品升级，并使用GUI学习如何配置，详情过多无法在此一一解释。

``` yaml
    health-regeneration: 10.0
```

> MMOCore生命值恢复百分比。

``` yaml
    mana-regeneration: 10.0
```

> MMOCore法力恢复百分比。

``` yaml
    max-stamina: 10.0
```

> MMOCore最大耐力加成。

``` yaml
    stamina-regeneration: 10.0
```

> MMOCore耐力恢复百分比。

``` yaml
    additional-experience: 50.0
```

> 额外MMOCore经验百分比。

``` yaml
    faction-damage-enemy: 50.0
```

> 设置对敌对派系的伤害增加。

``` yaml
    required-dexterity: 10.0
    required-strength: 10.0
```

> 所需的MMOCore属性。

``` yaml
    profession-enchanting: 10.0
    profession-smithing: 10.0
    profession-mining: 10.0
```

> 所需的MMOCore专业技能。

## 由消耗品增加的额外属性

``` yaml
WIKICONSUMABLE:
  base:
    material: APPLE
    disable-right-click-consume: true
```

> 这个消耗品是否可以通过右键点击食用？

``` yaml
    restore-health: 10.0
```

> 食用时恢复多少生命值？

``` yaml
    restore-food: 10.0
```

> 食用时恢复多少饱食度？

``` yaml
    restore-saturation: 10.0
```

> 食用时恢复多少饱和度？

``` yaml
    restore-mana: 10.0
```

> 食用时恢复多少法力值？

``` yaml
    restore-stamina: 10.0
```

> 食用时恢复多少耐力值？

``` yaml
    can-identify: true
```

> 该物品是否可以用于鉴定未鉴定的物品？

``` yaml
    can-deconstruct: true
```

> 该物品是否可以用于分解其他物品？

``` yaml
    can-deskin: true
```

> 该物品是否可以去除其他物品的皮肤？

``` yaml
    effects:
      REGENERATION:
        duration: 100.0
        amplifier: 1.0
```

> 食用后获得的效果列表。

``` yaml
    soulbinding-chance: 100.0
```

> 灵魂绑定几率。

``` yaml
    soulbound-break-chance: 100.0
```

> 绑定失败时破碎的几率。

``` yaml
    soulbound-level: 1.0
```

> 绑定的等级。

``` yaml
    item-cooldown: 5.0
```

> 食用后的冷却时间。

``` yaml
    vanilla-eating: true
```

> 使用原版的食用动画？仅对可以正常食用的材料有效。

``` yaml
    inedible: true
```

> 不可食用。

``` yaml
    max-consume: 5.0
```

> 物品的使用次数。

``` yaml
    repair: 100.0
```

> 该物品修复的自定义耐久度。

``` yaml
    repair-type: EXAMPLE_STAT
```

> 该物品可以修复哪些物品，接收物品需要具有匹配的修复类型。

## 由弓增加的额外属性

``` yaml
ELF_KINGS_LOST_BOW:
  base:
    material: BOW
    name: '&aElf King''s Lost Bow'
    arrow-particles:
      particle: FLAME
      amount: 5
      offset: 1.0
      speed: 1.0
```

> 飞行时的箭矢粒子效果。

``` yaml
    arrow-velocity: 100.0
```

> 弓箭的射程。

``` yaml
    arrow-potion-effects:
      POISON:
        duration: 100.0
        amplifier: 1
```

> 弓箭命中时的药水效果。
