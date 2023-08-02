package net.Indyuce.mmoitems;

import io.lumine.mythic.lib.version.VersionMaterial;
import net.Indyuce.mmoitems.stat.*;
import net.Indyuce.mmoitems.stat.block.*;
import net.Indyuce.mmoitems.stat.type.*;
import org.bukkit.Material;

/**
 * A central file for the plugin since new stats are added ALL the time.
 * It is also much safer to initialize class objects OUTSIDE of the field type class.
 */
@SuppressWarnings("unused")
public class ItemStats {

    // Main Item Stats
    public static final ItemStat
            REVISION_ID = new RevisionID(),
            MATERIAL = new MaterialStat(),
            ITEM_DAMAGE = new ItemDamage(),
            CUSTOM_MODEL_DATA = new CustomModelData(),
            MAX_DURABILITY = new MaximumDurability(),
            WILL_BREAK = new LostWhenBroken(),
            NAME = new DisplayName(),
            LORE = new Lore(),
            NBT_TAGS = new NBTTags(),
            LORE_FORMAT = new LoreFormat(),

    // Block Specific Stats
    BLOCK_ID = new BlockID(),
            REQUIRED_POWER = new RequiredPower(),
            REQUIRE_POWER_TO_BREAK = new RequirePowerToBreak(),
            MIN_XP = new MinXP(),
            MAX_XP = new MaxXP(),
            GEN_TEMPLATE = new GenTemplate(),

    // Misc Stats
    DISPLAYED_TYPE = new DisplayedType(),
            ENCHANTS = new Enchants(),
            HIDE_ENCHANTS = new HideEnchants(),
            PERMISSION = new Permission(),
            ITEM_PARTICLES = new ItemParticles(),
            ARROW_PARTICLES = new ArrowParticles(),
            PROJECTILE_PARTICLES = new ProjectileParticles(),

//     // Disable Interaction Stats
//     DISABLE_INTERACTION = new DisableStat("INTERACTION", VersionMaterial.GRASS_BLOCK.toMaterial(), "Disable Interaction", new String[]{"!block", "all"}, "Disable any unwanted interaction:", "block placement, item use..."),
//             DISABLE_CRAFTING = new DisableStat("CRAFTING", VersionMaterial.CRAFTING_TABLE.toMaterial(), "Disable Crafting", "Players can't use this item while crafting."), DISABLE_SMELTING = new DisableStat("SMELTING", Material.FURNACE, "Disable Smelting", "Players can't use this item in furnaces."),
//             DISABLE_SMITHING = new DisableStat("SMITHING", Material.DAMAGED_ANVIL, "Disable Smithing", "Players can't smith this item in smithing tables."),
//             DISABLE_ENCHANTING = new DisableStat("ENCHANTING", VersionMaterial.ENCHANTING_TABLE.toMaterial(), "Disable Enchanting", new String[]{"!block", "all"}, "Players can't enchant this item."),
//             DISABLE_REPAIRING = new DisableStat("REPAIRING", Material.ANVIL, "Disable Repairing", new String[]{"!block", "all"}, "Players can't use this item in anvils."),
//             DISABLE_ARROW_SHOOTING = new DisableStat("ARROW_SHOOTING", Material.ARROW, "Disable Arrow Shooting", new Material[]{Material.ARROW}, "Players can't shoot this", "item using a bow."),
//             DISABLE_ATTACK_PASSIVE = new DisableStat("ATTACK_PASSIVE", Material.BARRIER, "Disable Attack Passive", new String[]{"piercing", "slashing", "blunt"}, "Disables the blunt/slashing/piercing", "passive effects on attacks."),
//             DISABLE_DROP = new DisableStat("DROPING", Material.LAVA_BUCKET, "Disable Item Dropping", new String[]{"all"}, "Disables the dropping of this item!"),
//             DISABLE_ARROW_CONSUMPTION = new DisableStat("ARROW_CONSUMPTION", Material.ARROW, "Disable Arrow Consumption", new String[]{"crossbow"}, "Disable arrow requirement and consumption."),
// 禁用交互统计
    DISABLE_INTERACTION = new DisableStat("INTERACTION", VersionMaterial.GRASS_BLOCK.toMaterial(), "禁用交互", new String[]{"!block", "all"}, "禁止任何不需要的交互: 放置方块、使用物品..."),
            DISABLE_CRAFTING = new DisableStat("CRAFTING", VersionMaterial.CRAFTING_TABLE.toMaterial(), "禁用合成", "玩家无法在合成时使用此物品。"),
            DISABLE_SMELTING = new DisableStat("SMELTING", Material.FURNACE, "禁用熔炼", "玩家无法在熔炉中使用此物品。"),
            DISABLE_SMITHING = new DisableStat("SMITHING", Material.DAMAGED_ANVIL, "禁用锻造", "玩家无法在锻造台上锻造此物品。"),
            DISABLE_ENCHANTING = new DisableStat("ENCHANTING", VersionMaterial.ENCHANTING_TABLE.toMaterial(), "禁用附魔", new String[]{"!block", "all"}, "玩家无法对此物品进行附魔。"),
            DISABLE_REPAIRING = new DisableStat("REPAIRING", Material.ANVIL, "禁用修复", new String[]{"!block", "all"}, "玩家无法在铁砧中使用此物品进行修复。"),
            DISABLE_ARROW_SHOOTING = new DisableStat("ARROW_SHOOTING", Material.ARROW, "禁用箭射击", new Material[]{Material.ARROW}, "玩家无法使用弓射击此物品。"),
            DISABLE_ATTACK_PASSIVE = new DisableStat("ATTACK_PASSIVE", Material.BARRIER, "禁用攻击被动效果", new String[]{"piercing", "slashing", "blunt"}, "禁止攻击对被动效果: 刺穿、砍击、钝击。"),
            DISABLE_DROP = new DisableStat("DROPING", Material.LAVA_BUCKET, "禁用物品丢弃", new String[]{"all"}, "禁止丢弃此物品！"),
            DISABLE_ARROW_CONSUMPTION = new DisableStat("ARROW_CONSUMPTION", Material.ARROW, "禁用箭消耗", new String[]{"crossbow"}, "禁止箭矢的需求和消耗。"),




//     // RPG Stats
//     REQUIRED_LEVEL = new RequiredLevel(),
//             REQUIRED_CLASS = new RequiredClass(),
//             ATTACK_DAMAGE = new AttackDamage(),
//             ATTACK_SPEED = new AttackSpeed(),
//             CRITICAL_STRIKE_CHANCE = new DoubleStat("CRITICAL_STRIKE_CHANCE", Material.NETHER_STAR, "Critical Strike Chance", new String[]{"Critical Strikes deal more damage.", "In % chance."}, new String[]{"!miscellaneous", "!block", "all"}),
//             CRITICAL_STRIKE_POWER = new DoubleStat("CRITICAL_STRIKE_POWER", Material.NETHER_STAR, "Critical Strike Power", new String[]{"The extra damage weapon crits deals.", "(Stacks with default value)", "In %."}, new String[]{"!miscellaneous", "!block", "all"}),
//             SKILL_CRITICAL_STRIKE_CHANCE = new DoubleStat("SKILL_CRITICAL_STRIKE_CHANCE", Material.NETHER_STAR, "Skill Critical Strike Chance", new String[]{"Increases the chance of dealing skill crits (in %)."}, new String[]{"!miscellaneous", "!block", "all"}),
//             SKILL_CRITICAL_STRIKE_POWER = new DoubleStat("SKILL_CRITICAL_STRIKE_POWER", Material.NETHER_STAR, "Skill Critical Strike Power", new String[]{"Extra damage dealt (in %) by skill crits.", "(Stacks with default value)", "In %."}, new String[]{"!miscellaneous", "!block", "all"}),
//             BLOCK_POWER = new DoubleStat("BLOCK_POWER", Material.IRON_HELMET, "Block Power", new String[]{"The % of the damage your", "armor/shield can block.", "Default: 25%"}, new String[]{"!miscellaneous", "!block", "all"}),
//             BLOCK_RATING = new DoubleStat("BLOCK_RATING", Material.IRON_HELMET, "Block Rating", new String[]{"The chance your piece of armor", "has to block any entity attack."}, new String[]{"!miscellaneous", "!block", "all"}),
//             BLOCK_COOLDOWN_REDUCTION = new DoubleStat("BLOCK_COOLDOWN_REDUCTION", Material.IRON_HELMET, "Block Cooldown Reduction", new String[]{"Reduces the blocking cooldown (%)."}, new String[]{"!miscellaneous", "!block", "all"}),
//             DODGE_RATING = new DoubleStat("DODGE_RATING", Material.FEATHER, "Dodge Rating", new String[]{"The chance to dodge an attack.", "Dodging completely negates", "the attack damage."}, new String[]{"!miscellaneous", "!block", "all"}),
//             DODGE_COOLDOWN_REDUCTION = new DoubleStat("DODGE_COOLDOWN_REDUCTION", Material.FEATHER, "Dodge Cooldown Reduction", new String[]{"Reduces the dodging cooldown (%)."}, new String[]{"!miscellaneous", "!block", "all"}),
//             PARRY_RATING = new DoubleStat("PARRY_RATING", Material.BUCKET, "Parry Rating", new String[]{"The chance to parry an attack.", "Parrying negates the damage", "and knocks the attacker back."}, new String[]{"!miscellaneous", "!block", "all"}),
//             PARRY_COOLDOWN_REDUCTION = new DoubleStat("PARRY_COOLDOWN_REDUCTION", Material.BUCKET, "Parry Cooldown Reduction", new String[]{"Reduces the parrying cooldown (%)."}, new String[]{"!miscellaneous", "!block", "all"}),
//             COOLDOWN_REDUCTION = new DoubleStat("COOLDOWN_REDUCTION", Material.BOOK, "Cooldown Reduction", new String[]{"Reduces cooldowns of item and player skills (%)."}),
//             RANGE = new DoubleStat("RANGE", Material.STICK, "Range", new String[]{"The range of your item attacks."}, new String[]{"staff", "whip", "wand", "musket", "gem_stone"}),
//             MANA_COST = new ManaCost(),
//             STAMINA_COST = new DoubleStat("STAMINA_COST", VersionMaterial.LIGHT_GRAY_DYE.toMaterial(), "Stamina Cost", new String[]{"Stamina spent by your weapon to be used."}, new String[]{"piercing", "slashing", "blunt", "range"}),
//             ARROW_VELOCITY = new DoubleStat("ARROW_VELOCITY", Material.ARROW, "Arrow Velocity", new String[]{"Determines how far your", "weapon can shoot.", "Default: 1.0"}, new String[]{"gem_stone", "bow", "crossbow"}),
//             ARROW_POTION_EFFECTS = new ArrowPotionEffects(),
//             PVE_DAMAGE = new DoubleStat("PVE_DAMAGE", VersionMaterial.PORKCHOP.toMaterial(), "PvE Damage", new String[]{"Additional damage against", "non human entities in %."}, new String[]{"piercing", "slashing", "blunt", "catalyst", "range", "tool", "armor", "gem_stone", "accessory"}),
//             PVP_DAMAGE = new DoubleStat("PVP_DAMAGE", VersionMaterial.SKELETON_SKULL.toMaterial(), "PvP Damage", new String[]{"Additional damage", "against players in %."}, new String[]{"piercing", "slashing", "blunt", "catalyst", "range", "tool", "armor", "gem_stone", "accessory"}),
//             BLUNT_POWER = new DoubleStat("BLUNT_POWER", Material.IRON_AXE, "Blunt Power", new String[]{"The radius of the AoE attack.", "If set to 2.0, enemies within 2 blocks", "around your target will take damage."}, new String[]{"blunt", "gem_stone"}),
//             BLUNT_RATING = new DoubleStat("BLUNT_RATING", Material.BRICK, "Blunt Rating", new String[]{"The force of the blunt attack.", "If set to 50%, enemies hit by the attack", "will take 50% of the initial damage."}, new String[]{"blunt", "gem_stone"}),
//             WEAPON_DAMAGE = new DoubleStat("WEAPON_DAMAGE", Material.IRON_SWORD, "Weapon Damage", new String[]{"Additional on-hit weapon damage in %."}),
//             SKILL_DAMAGE = new DoubleStat("SKILL_DAMAGE", Material.BOOK, "Skill Damage", new String[]{"Additional ability damage in %."}),
//             PROJECTILE_DAMAGE = new DoubleStat("PROJECTILE_DAMAGE", Material.ARROW, "Projectile Damage", new String[]{"Additional skill/weapon projectile damage."}),
//             MAGIC_DAMAGE = new DoubleStat("MAGIC_DAMAGE", Material.MAGMA_CREAM, "Magic Damage", new String[]{"Additional magic skill damage in %."}),
//             PHYSICAL_DAMAGE = new DoubleStat("PHYSICAL_DAMAGE", Material.IRON_AXE, "Physical Damage", new String[]{"Additional skill/weapon physical damage."}),
//             DEFENSE = new DoubleStat("DEFENSE", Material.SHIELD, "Defense", new String[]{"Reduces damage from any source.", "Formula can be set in MMOLib Config."}),
//             DAMAGE_REDUCTION = new DoubleStat("DAMAGE_REDUCTION", Material.IRON_CHESTPLATE, "Damage Reduction", new String[]{"Reduces damage from any source.", "In %."}),
//             FALL_DAMAGE_REDUCTION = new DoubleStat("FALL_DAMAGE_REDUCTION", Material.FEATHER, "Fall Damage Reduction", new String[]{"Reduces fall damage.", "In %."}),
//             PROJECTILE_DAMAGE_REDUCTION = new DoubleStat("PROJECTILE_DAMAGE_REDUCTION", VersionMaterial.SNOWBALL.toMaterial(), "Projectile Damage Reduction", new String[]{"Reduces projectile damage.", "In %."}),
//             PHYSICAL_DAMAGE_REDUCTION = new DoubleStat("PHYSICAL_DAMAGE_REDUCTION", Material.LEATHER_CHESTPLATE, "Physical Damage Reduction", new String[]{"Reduces physical damage.", "In %."}),
//             FIRE_DAMAGE_REDUCTION = new DoubleStat("FIRE_DAMAGE_REDUCTION", Material.BLAZE_POWDER, "Fire Damage Reduction", new String[]{"Reduces fire damage.", "In %."}),
//             MAGIC_DAMAGE_REDUCTION = new DoubleStat("MAGIC_DAMAGE_REDUCTION", Material.POTION, "Magic Damage Reduction", new String[]{"Reduce magic damage dealt by potions.", "In %."}),
//             PVE_DAMAGE_REDUCTION = new DoubleStat("PVE_DAMAGE_REDUCTION", VersionMaterial.PORKCHOP.toMaterial(), "PvE Damage Reduction", new String[]{"Reduces damage dealt by mobs.", "In %."}),
//             PVP_DAMAGE_REDUCTION = new DoubleStat("PVP_DAMAGE_REDUCTION", VersionMaterial.SKELETON_SKULL.toMaterial(), "PvP Damage Reduction", new String[]{"Reduces damage dealt by players", "In %."}),
//             UNDEAD_DAMAGE = new DoubleStat("UNDEAD_DAMAGE", VersionMaterial.SKELETON_SKULL.toMaterial(), "Undead Damage", new String[]{"Deals additional damage to undead.", "In %."}),
//             LIFESTEAL = new DoubleStat("LIFESTEAL", Material.REDSTONE, "Lifesteal", new String[]{"Percentage of damage you gain back as", "health when inflicting weapon damage."}),
//             SPELL_VAMPIRISM = new DoubleStat("SPELL_VAMPIRISM", Material.REDSTONE, "Spell Vampirism", new String[]{"Percentage of damage you gain back as", "health when inflicting skill damage."}),
// 角色扩展属性
    REQUIRED_LEVEL = new RequiredLevel(), // 所需等级
             REQUIRED_CLASS = new RequiredClass(), // 所需职业
             ATTACK_DAMAGE = new AttackDamage(), // 攻击伤害
             ATTACK_SPEED = new AttackSpeed(), // 攻击速度
             CRITICAL_STRIKE_CHANCE = new DoubleStat("CRITICAL_STRIKE_CHANCE", Material.NETHER_STAR, "暴击几率", new String[]{"暴击造成更多伤害。", "以百分比表示。"}, new String[]{"!miscellaneous", "!block", "all"}),
             CRITICAL_STRIKE_POWER = new DoubleStat("CRITICAL_STRIKE_POWER", Material.NETHER_STAR, "暴击威力", new String[]{"暴击额外伤害。", " (与默认值叠加) ", "以百分比表示。"}, new String[]{"!miscellaneous", "!block", "all"}),
             SKILL_CRITICAL_STRIKE_CHANCE = new DoubleStat("SKILL_CRITICAL_STRIKE_CHANCE", Material.NETHER_STAR, "技能暴击几率", new String[]{"增加技能暴击几率 (以百分比表示) 。"}, new String[]{"!miscellaneous", "!block", "all"}),
             SKILL_CRITICAL_STRIKE_POWER = new DoubleStat("SKILL_CRITICAL_STRIKE_POWER", Material.NETHER_STAR, "技能暴击威力", new String[]{"技能暴击额外伤害 (以百分比表示, 与默认值叠加) 。"}, new String[]{"!miscellaneous", "!block", "all"}),
             BLOCK_POWER = new DoubleStat("BLOCK_POWER", Material.IRON_HELMET, "格挡强度", new String[]{"你的护甲/盾牌阻挡伤害的百分比。默认值: 25%。"}, new String[]{"!miscellaneous", "!block", "all"}),
             BLOCK_RATING = new DoubleStat("BLOCK_RATING", Material.IRON_HELMET, "格挡几率", new String[]{"你的护甲格挡敌人攻击的几率。"}, new String[]{"!miscellaneous", "!block", "all"}),
             BLOCK_COOLDOWN_REDUCTION = new DoubleStat("BLOCK_COOLDOWN_REDUCTION", Material.IRON_HELMET, "格挡冷却缩减", new String[]{"减少格挡冷却时间的百分比。"}, new String[]{"!miscellaneous", "!block", "all"}),             
             DODGE_RATING = new DoubleStat("DODGE_RATING", Material.FEATHER, "闪避几率", new String[]{"闪避攻击的几率。完全闪避将无视攻击伤害。"}, new String[]{"!miscellaneous", "!block", "all"}),
             DODGE_COOLDOWN_REDUCTION = new DoubleStat("DODGE_COOLDOWN_REDUCTION", Material.FEATHER, "闪避冷却缩减", new String[]{"减少闪避冷却时间的百分比。"}, new String[]{"!miscellaneous", "!block", "all"}),
             PARRY_RATING = new DoubleStat("PARRY_RATING", Material.BUCKET, "招架几率", new String[]{"招架攻击的几率。招架将无视攻击伤害并击退攻击者。"}, new String[]{"!miscellaneous", "!block", "all"}),
             PARRY_COOLDOWN_REDUCTION = new DoubleStat("PARRY_COOLDOWN_REDUCTION", Material.BUCKET, "招架冷却缩减", new String[]{"减少招架冷却时间的百分比。"}, new String[]{"!miscellaneous", "!block", "all"}),
             COOLDOWN_REDUCTION = new DoubleStat("COOLDOWN_REDUCTION", Material.BOOK, "冷却缩减", new String[]{"减少物品和玩家技能的冷却时间 (百分比) 。"}),
             RANGE = new DoubleStat("RANGE", Material.STICK, "攻击范围", new String[]{"你物品攻击的射程。"}, new String[]{"staff", "whip", "wand", "musket", "gem_stone"}),
             MANA_COST = new ManaCost(), // 法力消耗
             STAMINA_COST = new DoubleStat("STAMINA_COST", VersionMaterial.LIGHT_GRAY_DYE.toMaterial(), "耐力消耗", new String[]{"武器使用时消耗的耐力。"}, new String[]{"piercing", "slashing", "blunt", "range"}),
             ARROW_VELOCITY = new DoubleStat("ARROW_VELOCITY", Material.ARROW, "箭矢速度", new String[]{"决定你的武器能射出多远。默认值: 1.0。"}, new String[]{"gem_stone", "bow", "crossbow"}),
             ARROW_POTION_EFFECTS = new ArrowPotionEffects(), // 箭矢药水效果
             PVE_DAMAGE = new DoubleStat("PVE_DAMAGE", VersionMaterial.PORKCHOP.toMaterial(), "PvE伤害", new String[]{"对非人类生物造成额外伤害 (以百分比表示) 。"}, new String[]{"piercing", "slashing", "blunt", "catalyst", "range", "tool", "armor", "gem_stone", "accessory"}),
             PVP_DAMAGE = new DoubleStat("PVP_DAMAGE", VersionMaterial.SKELETON_SKULL.toMaterial(), "PvP伤害", new String[]{"对玩家造成额外伤害 (以百分比表示) 。"}, new String[]{"piercing", "slashing", "blunt", "catalyst", "range", "tool", "armor", "gem_stone", "accessory"}),
             BLUNT_POWER = new DoubleStat("BLUNT_POWER", Material.IRON_AXE, "钝击强度", new String[]{"范围攻击的半径。若设置为2.0, 则目标周围2格的敌人将受到伤害。"}, new String[]{"blunt", "gem_stone"}),
             BLUNT_RATING = new DoubleStat("BLUNT_RATING", Material.BRICK, "钝击等级", new String[]{"钝击攻击的威力。若设置为50%, 被该攻击击中的敌人将承受初始伤害的50%。"}, new String[]{"blunt", "gem_stone"}),
             WEAPON_DAMAGE = new DoubleStat("WEAPON_DAMAGE", Material.IRON_SWORD, "武器伤害", new String[]{"额外击中武器伤害 (以百分比表示) 。"}),
             SKILL_DAMAGE = new DoubleStat("SKILL_DAMAGE", Material.BOOK, "技能伤害", new String[]{"额外技能伤害 (以百分比表示) 。"}),
             PROJECTILE_DAMAGE = new DoubleStat("PROJECTILE_DAMAGE", Material.ARROW, "抛射物伤害", new String[]{"额外技能/武器抛射物伤害。"}),
             MAGIC_DAMAGE = new DoubleStat("MAGIC_DAMAGE", Material.MAGMA_CREAM, "魔法伤害", new String[]{"额外魔法技能伤害 (以百分比表示) 。"}),
             PHYSICAL_DAMAGE = new DoubleStat("PHYSICAL_DAMAGE", Material.IRON_AXE, "物理伤害", new String[]{"额外技能/武器物理伤害。"}),
             DEFENSE = new DoubleStat("DEFENSE", Material.SHIELD, "防御", new String[]{"减少来自任何来源的伤害。公式可在MMOLib配置中设置。"}),
             DAMAGE_REDUCTION = new DoubleStat("DAMAGE_REDUCTION", Material.IRON_CHESTPLATE, "伤害减免", new String[]{"减少来自任何来源的伤害。以百分比表示。"}),
             FALL_DAMAGE_REDUCTION = new DoubleStat("FALL_DAMAGE_REDUCTION", Material.FEATHER, "坠落伤害减免", new String[]{"减少坠落伤害。以百分比表示。"}),
             PROJECTILE_DAMAGE_REDUCTION = new DoubleStat("PROJECTILE_DAMAGE_REDUCTION", VersionMaterial.SNOWBALL.toMaterial(), "抛射物伤害减免", new String[]{"减少抛射物伤害。以百分比表示。"}),
             PHYSICAL_DAMAGE_REDUCTION = new DoubleStat("PHYSICAL_DAMAGE_REDUCTION", Material.LEATHER_CHESTPLATE, "物理伤害减免", new String[]{"减少物理伤害。以百分比表示。"}),
             FIRE_DAMAGE_REDUCTION = new DoubleStat("FIRE_DAMAGE_REDUCTION", Material.BLAZE_POWDER, "火焰伤害减免", new String[]{"减少火焰伤害。以百分比表示。"}),
             MAGIC_DAMAGE_REDUCTION = new DoubleStat("MAGIC_DAMAGE_REDUCTION", Material.POTION, "魔法伤害减免", new String[]{"减少由药水造成的魔法伤害。以百分比表示。"}),
             PVE_DAMAGE_REDUCTION = new DoubleStat("PVE_DAMAGE_REDUCTION", VersionMaterial.PORKCHOP.toMaterial(), "PvE伤害减免", new String[]{"减少来自怪物的伤害。以百分比表示。"}),
             PVP_DAMAGE_REDUCTION = new DoubleStat("PVP_DAMAGE_REDUCTION", VersionMaterial.SKELETON_SKULL.toMaterial(), "PvP伤害减免", new String[]{"减少来自玩家的伤害。以百分比表示。"}),
             UNDEAD_DAMAGE = new DoubleStat("UNDEAD_DAMAGE", VersionMaterial.SKELETON_SKULL.toMaterial(), "不死族伤害", new String[]{"对不死族造成额外伤害。以百分比表示。"}),
             LIFESTEAL = new DoubleStat("LIFESTEAL", Material.REDSTONE, "吸血", new String[]{"当造成武器伤害时, 以伤害的百分比回复生命值。"}),
             SPELL_VAMPIRISM = new DoubleStat("SPELL_VAMPIRISM", Material.REDSTONE, "法术吸血", new String[]{"当造成技能伤害时, 以伤害的百分比回复生命值。"}),



//     // Extra Stats
//     UNBREAKABLE = new Unbreakable(),
//             TIER = new ItemTierStat(),
//             SET = new ItemSetStat(),
//             ARMOR = new DoubleStat("ARMOR", VersionMaterial.GOLDEN_CHESTPLATE.toMaterial(), "Armor", new String[]{"The armor given to the holder."}),
//             ARMOR_TOUGHNESS = new DoubleStat("ARMOR_TOUGHNESS", Material.DIAMOND_CHESTPLATE, "Armor Toughness", new String[]{"Armor toughness reduces damage taken."}),
//             MAX_HEALTH = new DoubleStat("MAX_HEALTH", Material.GOLDEN_APPLE, "Max Health", new String[]{"The amount of health your", "item gives to the holder."}),
//             UNSTACKABLE = new Unstackable(),
//             MAX_MANA = new DoubleStat("MAX_MANA", VersionMaterial.LAPIS_LAZULI.toMaterial(), "Max Mana", new String[]{"Adds mana to your max mana bar."}),
//             KNOCKBACK_RESISTANCE = new KnockbackResistance(),
//             MOVEMENT_SPEED = new MovementSpeed(),
//             TWO_HANDED = new BooleanStat("TWO_HANDED", Material.IRON_INGOT, "Two Handed", new String[]{"If set to true, a player will be", "significantly slower if holding two", "items, one being Two Handed."}, new String[]{"piercing", "slashing", "blunt", "catalyst", "range", "tool"}),
//             REQUIRED_BIOMES = new RequiredBiomes(),
//             DROP_ON_DEATH = new DisableDeathDrop(),
//             DURABILITY_BAR = new DurabilityBar(),
    UNBREAKABLE = new Unbreakable(),
            TIER = new ItemTierStat(),
            SET = new ItemSetStat(),
            ARMOR = new DoubleStat("ARMOR", VersionMaterial.GOLDEN_CHESTPLATE.toMaterial(), "护甲", new String[]{"赋予持有者的护甲值。"}),
            ARMOR_TOUGHNESS = new DoubleStat("ARMOR_TOUGHNESS", Material.DIAMOND_CHESTPLATE, "护甲韧性", new String[]{"护甲韧性可减少所受的伤害。"}),
            MAX_HEALTH = new DoubleStat("MAX_HEALTH", Material.GOLDEN_APPLE, "最大生命值", new String[]{"物品赋予持有者的额外生命值。"}),
            UNSTACKABLE = new Unstackable(),
            MAX_MANA = new DoubleStat("MAX_MANA", VersionMaterial.LAPIS_LAZULI.toMaterial(), "最大法力值", new String[]{"增加最大法力值的魔力。"}),
            KNOCKBACK_RESISTANCE = new KnockbackResistance(),
            MOVEMENT_SPEED = new MovementSpeed(),
            TWO_HANDED = new BooleanStat("TWO_HANDED", Material.IRON_INGOT, "双手持握", new String[]{"若设为true, 则玩家持有两个物品时", "其中一个为双手武器时会显著降低速度。"}, new String[]{"刺击", "砍击", "钝击", "催化剂", "远程", "工具"}),
            REQUIRED_BIOMES = new RequiredBiomes(),
            DROP_ON_DEATH = new DisableDeathDrop(),
            DURABILITY_BAR = new DurabilityBar(),


    // Permanent Effects
    PERM_EFFECTS = new PermanentEffects(),
            GRANTED_PERMISSIONS = new GrantedPermissions(),

    // Consumable Stats
    RESTORE_HEALTH = new RestoreHealth(),
            RESTORE_FOOD = new RestoreFood(),
            RESTORE_SATURATION = new RestoreSaturation(),
            RESTORE_MANA = new RestoreMana(),
            RESTORE_STAMINA = new RestoreStamina(),
            CAN_IDENTIFY = new CanIdentify(),
            CAN_DECONSTRUCT = new CanDeconstruct(),
            CAN_DESKIN = new CanDeskin(),
            EFFECTS = new Effects(),
            SOULBINDING_CHANCE = new SoulbindingChance(),
            SOULBOUND_BREAK_CHANCE = new SoulbindingBreakChance(),
            SOULBOUND_LEVEL = new SoulboundLevel(),
//     //  AUTO_SOULBIND = new BooleanStat("AUTO_SOULBIND", VersionMaterial.ENDER_EYE.toMaterial(), "Auto-Soulbind", new String[]{"Automatically soulbinds this item to", "a player when he acquires it."}, new String[]{"!consumable", "all"}),
//     ITEM_COOLDOWN = new DoubleStat("ITEM_COOLDOWN", Material.COOKED_CHICKEN, "Item Cooldown", new String[]{"This cooldown applies for consumables", "as well as for item commands."}, new String[]{"!armor", "!gem_stone", "!block", "all"}),
//             COOLDOWN_REFERENCE = new StringStat("COOLDOWN_REFERENCE", Material.CHICKEN, "Cooldown Reference", new String[]{"Two items with the same cooldown reference", "will share their cooldowns. This is useful", "for health or mana pots for example."}, new String[]{"!armor", "!gem_stone", "!block", "all"}),
//             VANILLA_EATING_ANIMATION = new VanillaEatingAnimation(),
//             GEM_COLOR = new GemColor(),
//             GEM_UPGRADE_SCALING = new GemUpgradeScaling(),
//             ITEM_TYPE_RESTRICTION = new ItemTypeRestriction(),
//             MAX_CONSUME = new MaxConsume(),
//             SUCCESS_RATE = new SuccessRate(),
    ITEM_COOLDOWN = new DoubleStat("ITEM_COOLDOWN", Material.COOKED_CHICKEN, "物品冷却", new String[]{"此冷却适用于消耗品", "以及物品命令。"}, new String[]{"!armor", "!gem_stone", "!block", "all"}),
            COOLDOWN_REFERENCE = new StringStat("COOLDOWN_REFERENCE", Material.CHICKEN, "冷却参考", new String[]{"具有相同冷却参考的两个物品", "将共享它们的冷却时间。这对于例如生命或法力药剂非常有用。"}, new String[]{"!armor", "!gem_stone", "!block", "all"}),
            VANILLA_EATING_ANIMATION = new VanillaEatingAnimation(),
            GEM_COLOR = new GemColor(),
            GEM_UPGRADE_SCALING = new GemUpgradeScaling(),
            ITEM_TYPE_RESTRICTION = new ItemTypeRestriction(),
            MAX_CONSUME = new MaxConsume(),
            SUCCESS_RATE = new SuccessRate(),

    // Crafting Stats
    CRAFTING = new Crafting(),
            CRAFT_PERMISSION = new CraftingPermission(),
    //CRAFT_AMOUNT = new DoubleStat("CRAFTED_AMOUNT", Material.WOODEN_AXE, "Crafted Amount", new String[]{"The stack count for", "this item when crafted."}, new String[]{"all"}),

//     // Unique Stats
//     AUTOSMELT = new BooleanStat("AUTOSMELT", Material.COAL, "Autosmelt", new String[]{"If set to true, your tool will", "automaticaly smelt mined ores."}, new String[]{"tool"}),
//             BOUNCING_CRACK = new BooleanStat("BOUNCING_CRACK", VersionMaterial.COBBLESTONE_WALL.toMaterial(), "Bouncing Crack", new String[]{"If set to true, your tool will", "also break nearby blocks."}, new String[]{"tool"}),
//             PICKAXE_POWER = new PickaxePower(),
//             CUSTOM_SOUNDS = new CustomSounds(),
//             ELEMENTS = new Elements(),
//             COMMANDS = new Commands(),
//             STAFF_SPIRIT = new StaffSpiritStat(),
//             LUTE_ATTACK_SOUND = new LuteAttackSoundStat(),
//             LUTE_ATTACK_EFFECT = new LuteAttackEffectStat(),
//             NOTE_WEIGHT = new DoubleStat("NOTE_WEIGHT", VersionMaterial.MUSIC_DISC_MALL.toMaterial(), "Note Weight", new String[]{"Defines how the projectile cast", "by your lute tilts downwards."}, new String[]{"lute"}),
//             REMOVE_ON_CRAFT = new BooleanStat("REMOVE_ON_CRAFT", Material.GLASS_BOTTLE, "Remove on Craft", new String[]{"If the item should be completely", "removed when used in a recipe,", "or if it should become an", "empty bottle or bucket."}, new String[]{"all"}, Material.POTION, Material.SPLASH_POTION, Material.LINGERING_POTION, Material.MILK_BUCKET, Material.LAVA_BUCKET, Material.WATER_BUCKET),
//             COMPATIBLE_TYPES = new CompatibleTypes(),
//             COMPATIBLE_IDS = new CompatibleIds(),
//             COMPATIBLE_MATERIALS = new CompatibleMaterials(),
//             GEM_SOCKETS = new GemSockets(),
//             RANDOM_UNSOCKET = new RandomUnsocket(),
    AUTOSMELT = new BooleanStat("AUTOSMELT", Material.COAL, "自动熔炼", new String[]{"如果设置为true, 你的工具将会", "自动熔炼挖掘到的矿石。"}, new String[]{"tool"}),
            BOUNCING_CRACK = new BooleanStat("BOUNCING_CRACK", VersionMaterial.COBBLESTONE_WALL.toMaterial(), "弹跳裂缝", new String[]{"如果设置为true, 你的工具将会", "同时破坏附近的方块。"}, new String[]{"tool"}),
            PICKAXE_POWER = new PickaxePower(),
            CUSTOM_SOUNDS = new CustomSounds(),
            ELEMENTS = new Elements(),
            COMMANDS = new Commands(),
            STAFF_SPIRIT = new StaffSpiritStat(),
            LUTE_ATTACK_SOUND = new LuteAttackSoundStat(),
            LUTE_ATTACK_EFFECT = new LuteAttackEffectStat(),
            NOTE_WEIGHT = new DoubleStat("NOTE_WEIGHT", VersionMaterial.MUSIC_DISC_MALL.toMaterial(), "音符重量", new String[]{"定义你的琴产生的投射物", "向下倾斜的程度。"}, new String[]{"lute"}),
            REMOVE_ON_CRAFT = new BooleanStat("REMOVE_ON_CRAFT", Material.GLASS_BOTTLE, "合成时移除", new String[]{"如果物品在合成时应该完全", "移除, 或者变成一个空瓶子或空桶。"}, new String[]{"所有"}, Material.POTION, Material.SPLASH_POTION, Material.LINGERING_POTION, Material.MILK_BUCKET, Material.LAVA_BUCKET, Material.WATER_BUCKET),
            COMPATIBLE_TYPES = new CompatibleTypes(),
            COMPATIBLE_IDS = new CompatibleIds(),
            COMPATIBLE_MATERIALS = new CompatibleMaterials(),
            GEM_SOCKETS = new GemSockets(),
            RANDOM_UNSOCKET = new RandomUnsocket(),


            
//     //todo CAN_UNSOCKET = new CanUnsocket(),
//     REPAIR = new RepairPower(),
//             REPAIR_PERCENT = new RepairPowerPercent(),
//             REPAIR_TYPE = new RepairReference(),
//             INEDIBLE = new BooleanStat("INEDIBLE", Material.POISONOUS_POTATO, "Inedible", new String[]{"Players won't be able to right-click this consumable.", "", "No effects of it will take place."}, new String[]{"consumable"}),
//             DISABLE_RIGHT_CLICK_CONSUME = new DisableStat("RIGHT_CLICK_CONSUME", Material.BAKED_POTATO, "Infinite Consume", new String[]{"consumable"}, "Players will be able to right-click this consumable", "and benefit from its effects, but it won't be consumed."),
//             KNOCKBACK = new DoubleStat("KNOCKBACK", VersionMaterial.IRON_HORSE_ARMOR.toMaterial(), "Knockback", new String[]{"Using this musket will knock", "the user back if positive."}, new String[]{"musket", "gem_stone"}),
//             RECOIL = new DoubleStat("RECOIL", VersionMaterial.IRON_HORSE_ARMOR.toMaterial(), "Recoil", new String[]{"Corresponds to the shooting innacuracy."}, new String[]{"musket", "gem_stone"}),
//             HANDWORN = new BooleanStat("HANDWORN", Material.STRING, "Handworn", new String[]{"This item ignores two-handedness.", "", "Basically for a ring or a glove that you", " can wear and still have your hand free", " to carry a two-handed weapon."}, new String[]{"catalyst"}),
//             AMPHIBIAN = new Amphibian(),
//todo CAN_UNSOCKET = new CanUnsocket(),
    REPAIR = new RepairPower(),
            REPAIR_PERCENT = new RepairPowerPercent(),
            REPAIR_TYPE = new RepairReference(),
            INEDIBLE = new BooleanStat("INEDIBLE", Material.POISONOUS_POTATO, "不可食用", new String[]{"玩家无法右键点击此消耗品。", "", "它的任何效果都不会发生。"}, new String[]{"consumable"}),
            DISABLE_RIGHT_CLICK_CONSUME = new DisableStat("RIGHT_CLICK_CONSUME", Material.BAKED_POTATO, "无限消耗", new String[]{"消耗品"}, "玩家将能够右键点击此消耗品", "并从其效果中受益, 但它不会被消耗。"),
            KNOCKBACK = new DoubleStat("KNOCKBACK", VersionMaterial.IRON_HORSE_ARMOR.toMaterial(), "击退", new String[]{"使用这把火枪会击退", "如果为正, 则击退用户。"}, new String[]{"musket", "gem_stone"}),
            RECOIL = new DoubleStat("RECOIL", VersionMaterial.IRON_HORSE_ARMOR.toMaterial(), "后坐力", new String[]{"对应于射击的不准确性。"}, new String[]{"musket", "gem_stone"}),
            HANDWORN = new BooleanStat("HANDWORN", Material.STRING, "手工制作", new String[]{"这个物品无视双手持有。", "", "基本上是一枚你可以佩戴的戒指或手套, ", "并且仍然可以释放手来", "携带双手武器。"}, new String[]{"catalyst"}),
            AMPHIBIAN = new Amphibian(),

//     // Abilities & Upgrading
//     ABILITIES = new Abilities(),
//             UPGRADE = new UpgradeStat(),
//             DOWNGRADE_ON_BREAK = new BooleanStat("BREAK_DOWNGRADE", Material.DAMAGED_ANVIL, "Downgrade when Broken", new String[]{"If this item's durability reaches 0,", "it will be fully repaired but also", "downgraded by one level.", "", "&cIt will only break if it cannot be", "&cdowngraded further", "", "Requires to define an &6Upgrade Template", "Required to define &6Custom Durability"}, new String[]{"piercing", "slashing", "blunt", "catalyst", "range", "tool", "armor", "consumable", "accessory"}),
//             DOWNGRADE_ON_DEATH = new BooleanStat("DEATH_DOWNGRADE", Material.DAMAGED_ANVIL, "Downgrade on Death", new String[]{"If the wearer of this item dies, it may", "downgrade (based on &6Death Downgrade", "&6Chance &7stat)", "", "Required to define an &6Upgrade Template", "Requires keep-inventory gamerule. "}, new String[]{"piercing", "slashing", "blunt", "catalyst", "range", "tool", "armor", "consumable", "accessory"}),
//             DOWNGRADE_ON_DEATH_CHANCE = new DoubleStat("DEATH_DOWNGRADE_CHANCE", Material.SKELETON_SKULL, "Death Downgrade Chance", new String[]{"Probability that an item with &cDowngrade ", "&con Death&7 will be downgraded when the", "player dies. ", "", "Exceeding 100% will for sure downgrade", "one item, and roll again to downgrade", "another (with the excess probability).", "&6The same item wont be downgraded twice."}, new String[]{"!miscellaneous", "!block", "all"}, false),
// Abilities & Upgrading
    ABILITIES = new Abilities(),
            UPGRADE = new UpgradeStat(),
            DOWNGRADE_ON_BREAK = new BooleanStat("BREAK_DOWNGRADE", Material.DAMAGED_ANVIL, "损坏时降级", new String[]{"如果这个物品的耐久度达到0,", "它将被完全修复, 但也", "降级一级。", "", "&c只有在不能进一步降级时, 它才会", "&c损坏", "", "需要定义一个&6升级模板", "需要定义&6自定义耐久度"}, new String[]{"piercing", "slashing", "blunt", "catalyst", "range", "tool", "armor", "consumable", "accessory"}),
            DOWNGRADE_ON_DEATH = new BooleanStat("DEATH_DOWNGRADE", Material.DAMAGED_ANVIL, "死亡时降级", new String[]{"如果这个物品的穿戴者死亡, 它可能", "会降级 (基于&6死亡降级", "&6概率 &7统计) ", "", "需要定义一个&6升级模板", "需要保留库存游戏规则。 "}, new String[]{"piercing", "slashing", "blunt", "catalyst", "range", "tool", "armor", "consumable", "accessory"}),
            DOWNGRADE_ON_DEATH_CHANCE = new DoubleStat("DEATH_DOWNGRADE_CHANCE", Material.SKELETON_SKULL, "死亡降级几率", new String[]{"带有&c死亡降级的物品在", "玩家死亡时被降级的", "概率。", "", "超过100%将肯定降级", "一件物品, 并再次滚动以降级", "另一件 (以超出的概率) 。", "&6同一物品不会被降级两次。"}, new String[]{"!miscellaneous", "!block", "all"}, false),



    // Unique Item Stats
    SKULL_TEXTURE = new SkullTextureStat(),
            DYE_COLOR = new DyeColor(),
            HIDE_DYE = new HideDye(),
            TRIM_MATERIAL = new TrimMaterialStat(),
            TRIM_PATTERN = new TrimPatternStat(),
            HIDE_ARMOR_TRIM = new HideTrim(),
            POTION_EFFECTS = new PotionEffects(),
            POTION_COLOR = new PotionColor(),
            SHIELD_PATTERN = new ShieldPatternStat(),
            HIDE_POTION_EFFECTS = new HidePotionEffects(),

    // Internal Stats
    SOULBOUND = new Soulbound(),
            CUSTOM_DURABILITY = new CustomDurability(),
            STORED_TAGS = new StoredTags(),
            ITEM_LEVEL = new ItemLevel(),
            BROWSER_DISPLAY_IDX = new BrowserDisplayIDX();

    /**
     * @deprecated Item damage is now {@link ItemDamage} and
     *         custom durability is now {@link CustomDurability}
     */
    @Deprecated
    public static final ItemStat DURABILITY = ITEM_DAMAGE;
}
