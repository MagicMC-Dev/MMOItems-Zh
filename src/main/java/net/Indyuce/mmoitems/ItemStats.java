package net.Indyuce.mmoitems;

import net.Indyuce.mmoitems.stat.Abilities;
import net.Indyuce.mmoitems.stat.Armor;
import net.Indyuce.mmoitems.stat.ArmorToughness;
import net.Indyuce.mmoitems.stat.ArrowParticles;
import net.Indyuce.mmoitems.stat.ArrowPotionEffects;
import net.Indyuce.mmoitems.stat.AttackDamage;
import net.Indyuce.mmoitems.stat.AttackSpeed;
import net.Indyuce.mmoitems.stat.Commands;
import net.Indyuce.mmoitems.stat.CompatibleTypes;
import net.Indyuce.mmoitems.stat.Crafting;
import net.Indyuce.mmoitems.stat.CustomModelData;
import net.Indyuce.mmoitems.stat.CustomSounds;
import net.Indyuce.mmoitems.stat.DisableAdvancedEnchantments;
import net.Indyuce.mmoitems.stat.DisplayName;
import net.Indyuce.mmoitems.stat.DyeColor;
import net.Indyuce.mmoitems.stat.Effects;
import net.Indyuce.mmoitems.stat.Elements;
import net.Indyuce.mmoitems.stat.Enchants;
import net.Indyuce.mmoitems.stat.GemColor;
import net.Indyuce.mmoitems.stat.GemSockets;
import net.Indyuce.mmoitems.stat.GrantedPermissions;
import net.Indyuce.mmoitems.stat.HideDye;
import net.Indyuce.mmoitems.stat.HideEnchants;
import net.Indyuce.mmoitems.stat.HidePotionEffects;
import net.Indyuce.mmoitems.stat.Inedible;
import net.Indyuce.mmoitems.stat.ItemDamage;
import net.Indyuce.mmoitems.stat.ItemLevel;
import net.Indyuce.mmoitems.stat.ItemParticles;
import net.Indyuce.mmoitems.stat.ItemSetStat;
import net.Indyuce.mmoitems.stat.ItemTierStat;
import net.Indyuce.mmoitems.stat.ItemTypeRestriction;
import net.Indyuce.mmoitems.stat.KnockbackResistance;
import net.Indyuce.mmoitems.stat.Lore;
import net.Indyuce.mmoitems.stat.LoreFormat;
import net.Indyuce.mmoitems.stat.LostWhenBroken;
import net.Indyuce.mmoitems.stat.LuteAttackEffectStat;
import net.Indyuce.mmoitems.stat.LuteAttackSoundStat;
import net.Indyuce.mmoitems.stat.MaterialStat;
import net.Indyuce.mmoitems.stat.MaxHealth;
import net.Indyuce.mmoitems.stat.MaximumDurability;
import net.Indyuce.mmoitems.stat.MovementSpeed;
import net.Indyuce.mmoitems.stat.NBTTags;
import net.Indyuce.mmoitems.stat.PermanentEffects;
import net.Indyuce.mmoitems.stat.Permission;
import net.Indyuce.mmoitems.stat.PickaxePower;
import net.Indyuce.mmoitems.stat.PotionColor;
import net.Indyuce.mmoitems.stat.PotionEffects;
import net.Indyuce.mmoitems.stat.RequiredClass;
import net.Indyuce.mmoitems.stat.RequiredLevel;
import net.Indyuce.mmoitems.stat.RevisionID;
import net.Indyuce.mmoitems.stat.ShieldPatternStat;
import net.Indyuce.mmoitems.stat.SkullTextureStat;
import net.Indyuce.mmoitems.stat.Soulbound;
import net.Indyuce.mmoitems.stat.SoulboundLevel;
import net.Indyuce.mmoitems.stat.StaffSpiritStat;
import net.Indyuce.mmoitems.stat.StoredTags;
import net.Indyuce.mmoitems.stat.SuccessRate;
import net.Indyuce.mmoitems.stat.Unbreakable;
import net.Indyuce.mmoitems.stat.Unstackable;
import net.Indyuce.mmoitems.stat.UpgradeStat;
import net.Indyuce.mmoitems.stat.VanillaEatingAnimation;
import net.Indyuce.mmoitems.stat.block.BlockID;
import net.Indyuce.mmoitems.stat.block.GenTemplate;
import net.Indyuce.mmoitems.stat.block.MaxXP;
import net.Indyuce.mmoitems.stat.block.MinXP;
import net.Indyuce.mmoitems.stat.block.RequiredPower;
import net.Indyuce.mmoitems.stat.type.BooleanStat;
import net.Indyuce.mmoitems.stat.type.DisableStat;
import net.Indyuce.mmoitems.stat.type.DoubleStat;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.Indyuce.mmoitems.stat.type.StringStat;
import net.mmogroup.mmolib.version.VersionMaterial;
import org.bukkit.Material;

/**
 * A central file for the plugin since new stats are added ALL the time.
 * It is also much safer to initialize class objects OUTSIDE of the field type class.
 */
@SuppressWarnings("unused")
public class ItemStats {
	public static final ItemStat
			// Main Item Stats
			REVISION_ID = new RevisionID(),
			MATERIAL = new MaterialStat(),
			DURABILITY = new ItemDamage(),
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
			MIN_XP = new MinXP(),
			MAX_XP = new MaxXP(),
			GEN_TEMPLATE = new GenTemplate(),

	// Misc Stats
	DISPLAYED_TYPE = new StringStat("DISPLAYED_TYPE", VersionMaterial.OAK_SIGN.toMaterial(), "Displayed Type", new String[]{"This option will only affect the", "type displayed on the item lore."}, new String[]{"all"}),
			ENCHANTS = new Enchants(),
			HIDE_ENCHANTS = new HideEnchants(),
			PERMISSION = new Permission(),
			ITEM_PARTICLES = new ItemParticles(),
			ARROW_PARTICLES = new ArrowParticles(),

	// Disable Interaction Stats
	DISABLE_INTERACTION = new DisableStat("INTERACTION", VersionMaterial.GRASS_BLOCK.toMaterial(), "Disable Interaction", new String[]{"!block", "all"}, "Disable any unwanted interaction:", "block placement, item use..."),
			DISABLE_CRAFTING = new DisableStat("CRAFTING", VersionMaterial.CRAFTING_TABLE.toMaterial(), "Disable Crafting", "Players can't use this item while crafting."), DISABLE_SMELTING = new DisableStat("SMELTING", Material.FURNACE, "Disable Smelting", "Players can't use this item in furnaces."),
			DISABLE_SMITHING = new DisableStat("SMITHING", Material.DAMAGED_ANVIL, "Disable Smithing", "Players can't smith this item in smithing tables."),
			DISABLE_ENCHANTING = new DisableStat("ENCHANTING", VersionMaterial.ENCHANTING_TABLE.toMaterial(), "Disable Enchanting", new String[]{"!block", "all"}, "Players can't enchant this item."),
			DISABLE_ADVANCED_ENCHANTS = new DisableAdvancedEnchantments(),
			DISABLE_REPAIRING = new DisableStat("REPAIRING", Material.ANVIL, "Disable Repairing", new String[]{"!block", "all"}, "Players can't use this item in anvils."),
			DISABLE_ARROW_SHOOTING = new DisableStat("ARROW_SHOOTING", Material.ARROW, "Disable Arrow Shooting", new Material[]{Material.ARROW}, "Players can't shoot this", "item using a bow."),
			DISABLE_ATTACK_PASSIVE = new DisableStat("ATTACK_PASSIVE", Material.BARRIER, "Disable Attack Passive", new String[]{"piercing", "slashing", "blunt"}, "Disables the blunt/slashing/piercing", "passive effects on attacks."),
			DISABLE_RIGHT_CLICK_CONSUME = new DisableStat("RIGHT_CLICK_CONSUME", Material.BARRIER, "Disable Right Click Consume", new String[]{"consumable"}, "This item will not be consumed", "when eaten by players."),

	// RPG Stats
	REQUIRED_LEVEL = new RequiredLevel(),
			REQUIRED_CLASS = new RequiredClass(),
			ATTACK_DAMAGE = new AttackDamage(),
			ATTACK_SPEED = new AttackSpeed(),
			CRITICAL_STRIKE_CHANCE = new DoubleStat("CRITICAL_STRIKE_CHANCE", Material.NETHER_STAR, "Critical Strike Chance", new String[]{"Critical Strikes deal more damage.", "In % chance."}, new String[]{"!miscellaneous", "!block", "all"}),
			CRITICAL_STRIKE_POWER = new DoubleStat("CRITICAL_STRIKE_POWER", Material.NETHER_STAR, "Critical Strike Power", new String[]{"The extra damage weapon crits deals.", "(Stacks with default value)", "In %."}, new String[]{"!miscellaneous", "!block", "all"}),
			BLOCK_POWER = new DoubleStat("BLOCK_POWER", Material.IRON_HELMET, "Block Power", new String[]{"The % of the damage your", "armor/shield can block.", "Default: 25%"}, new String[]{"!miscellaneous", "!block", "all"}),
			BLOCK_RATING = new DoubleStat("BLOCK_RATING", Material.IRON_HELMET, "Block Rating", new String[]{"The chance your piece of armor", "has to block any entity attack."}, new String[]{"!miscellaneous", "!block", "all"}),
			BLOCK_COOLDOWN_REDUCTION = new DoubleStat("BLOCK_COOLDOWN_REDUCTION", Material.IRON_HELMET, "Block Cooldown Reduction", new String[]{"Reduces the blocking cooldown (%)."}, new String[]{"!miscellaneous", "!block", "all"}),
			DODGE_RATING = new DoubleStat("DODGE_RATING", Material.FEATHER, "Dodge Rating", new String[]{"The chance to dodge an attack.", "Dodging completely negates", "the attack damage."}, new String[]{"!miscellaneous", "!block", "all"}),
			DODGE_COOLDOWN_REDUCTION = new DoubleStat("DODGE_COOLDOWN_REDUCTION", Material.FEATHER, "Dodge Cooldown Reduction", new String[]{"Reduces the dodging cooldown (%)."}, new String[]{"!miscellaneous", "!block", "all"}),
			PARRY_RATING = new DoubleStat("PARRY_RATING", Material.BUCKET, "Parry Rating", new String[]{"The chance to parry an attack.", "Parrying negates the damage", "and knocks the attacker back."}, new String[]{"!miscellaneous", "!block", "all"}),
			PARRY_COOLDOWN_REDUCTION = new DoubleStat("PARRY_COOLDOWN_REDUCTION", Material.BUCKET, "Parry Cooldown Reduction", new String[]{"Reduces the parrying cooldown (%)."}, new String[]{"!miscellaneous", "!block", "all"}),
			COOLDOWN_REDUCTION = new DoubleStat("COOLDOWN_REDUCTION", Material.BOOK, "Cooldown Reduction", new String[]{"Reduces cooldowns of item and player skills (%)."}),
			RANGE = new DoubleStat("RANGE", Material.STICK, "Range", new String[]{"The range of your item attacks."}, new String[]{"staff", "whip", "wand", "musket"}),
			MANA_COST = new DoubleStat("MANA_COST", VersionMaterial.LAPIS_LAZULI.toMaterial(), "Mana Cost", new String[]{"Mana spent by your weapon to be used."}, new String[]{"piercing", "slashing", "blunt", "range"}),
			STAMINA_COST = new DoubleStat("STAMINA_COST", VersionMaterial.LIGHT_GRAY_DYE.toMaterial(), "Stamina Cost", new String[]{"Stamina spent by your weapon to be used."}, new String[]{"piercing", "slashing", "blunt", "range"}),
			ARROW_VELOCITY = new DoubleStat("ARROW_VELOCITY", Material.ARROW, "Arrow Velocity", new String[]{"Determins how far your", "weapon can shoot.", "Default: 1.0"}, new String[]{"bow", "crossbow"}),
			ARROW_POTION_EFFECTS = new ArrowPotionEffects(),
			PVE_DAMAGE = new DoubleStat("PVE_DAMAGE", VersionMaterial.PORKCHOP.toMaterial(), "PvE Damage", new String[]{"Additional damage against", "non human entities in %."}, new String[]{"piercing", "slashing", "blunt", "offhand", "range", "tool", "armor", "gem_stone", "accessory"}),
			PVP_DAMAGE = new DoubleStat("PVP_DAMAGE", VersionMaterial.SKELETON_SKULL.toMaterial(), "PvP Damage", new String[]{"Additional damage", "against players in %."}, new String[]{"piercing", "slashing", "blunt", "offhand", "range", "tool", "armor", "gem_stone", "accessory"}),
			BLUNT_POWER = new DoubleStat("BLUNT_POWER", Material.IRON_AXE, "Blunt Power", new String[]{"The radius of the AoE attack.", "If set to 2.0, enemies within 2 blocks", "around your target will take damage."}, new String[]{"blunt", "gem_stone"}),
			BLUNT_RATING = new DoubleStat("BLUNT_RATING", Material.BRICK, "Blunt Rating", new String[]{"The force of the blunt attack.", "If set to 50%, enemies hit by the attack", "will take 50% of the initial damage."}, new String[]{"blunt", "gem_stone"}),
			WEAPON_DAMAGE = new DoubleStat("WEAPON_DAMAGE", Material.IRON_SWORD, "Weapon Damage", new String[]{"Additional on-hit weapon damage in %."}),
			SKILL_DAMAGE = new DoubleStat("SKILL_DAMAGE", Material.BOOK, "Skill Damage", new String[]{"Additional ability damage in %."}),
			PROJECTILE_DAMAGE = new DoubleStat("PROJECTILE_DAMAGE", Material.ARROW, "Projectile Damage", new String[]{"Additional skill/weapon projectile damage."}),
			MAGIC_DAMAGE = new DoubleStat("MAGIC_DAMAGE", Material.MAGMA_CREAM, "Magic Damage", new String[]{"Additional magic skill damage in %."}),
			PHYSICAL_DAMAGE = new DoubleStat("PHYSICAL_DAMAGE", Material.IRON_AXE, "Physical Damage", new String[]{"Additional skill/weapon physical damage."}),
			DEFENSE = new DoubleStat("DEFENSE", Material.SHIELD, "Defense", new String[]{"Reduces damage from any source.", "Formula can be set in MMOLib Config."}),
			DAMAGE_REDUCTION = new DoubleStat("DAMAGE_REDUCTION", Material.IRON_CHESTPLATE, "Damage Reduction", new String[]{"Reduces damage from any source.", "In %."}),
			FALL_DAMAGE_REDUCTION = new DoubleStat("FALL_DAMAGE_REDUCTION", Material.FEATHER, "Fall Damage Reduction", new String[]{"Reduces fall damage.", "In %."}),
			PROJECTILE_DAMAGE_REDUCTION = new DoubleStat("PROJECTILE_DAMAGE_REDUCTION", VersionMaterial.SNOWBALL.toMaterial(), "Projectile Damage Reduction", new String[]{"Reduces projectile damage.", "In %."}),
			PHYSICAL_DAMAGE_REDUCTION = new DoubleStat("PHYSICAL_DAMAGE_REDUCTION", Material.LEATHER_CHESTPLATE, "Physical Damage Reduction", new String[]{"Reduces physical damage.", "In %."}),
			FIRE_DAMAGE_REDUCTION = new DoubleStat("FIRE_DAMAGE_REDUCTION", Material.BLAZE_POWDER, "Fire Damage Reduction", new String[]{"Reduces fire damage.", "In %."}),
			MAGIC_DAMAGE_REDUCTION = new DoubleStat("MAGIC_DAMAGE_REDUCTION", Material.POTION, "Magic Damage Reduction", new String[]{"Reduce magic damage dealt by potions.", "In %."}),
			PVE_DAMAGE_REDUCTION = new DoubleStat("PVE_DAMAGE_REDUCTION", VersionMaterial.PORKCHOP.toMaterial(), "PvE Damage Reduction", new String[]{"Reduces damage dealt by mobs.", "In %."}),
			PVP_DAMAGE_REDUCTION = new DoubleStat("PVP_DAMAGE_REDUCTION", VersionMaterial.SKELETON_SKULL.toMaterial(), "PvP Damage Reduction", new String[]{"Reduces damage dealt by players", "In %."}),
			UNDEAD_DAMAGE = new DoubleStat("UNDEAD_DAMAGE", VersionMaterial.SKELETON_SKULL.toMaterial(), "Undead Damage", new String[]{"Deals additional damage to undead.", "In %."}),

	// Extra Stats
	UNBREAKABLE = new Unbreakable(),
			TIER = new ItemTierStat(),
			SET = new ItemSetStat(),
			ARMOR = new Armor(),
			ARMOR_TOUGHNESS = new ArmorToughness(),
			MAX_HEALTH = new MaxHealth(),
			UNSTACKABLE = new Unstackable(),
			MAX_MANA = new DoubleStat("MAX_MANA", VersionMaterial.LAPIS_LAZULI.toMaterial(), "Max Mana", new String[]{"Adds mana to your max mana bar."}),
			KNOCKBACK_RESISTANCE = new KnockbackResistance(),
			MOVEMENT_SPEED = new MovementSpeed(),
			TWO_HANDED = new BooleanStat("TWO_HANDED", Material.IRON_INGOT, "Two Handed", new String[]{"If set to true, a player will be", "significantly slower if holding two", "items, one being Two Handed."}, new String[]{"piercing", "slashing", "blunt", "offhand", "range", "tool"}),

	// Permanent Effects
	PERM_EFFECTS = new PermanentEffects(),
			GRANTED_PERMISSIONS = new GrantedPermissions(),

	// Consumable Stats
	RESTORE_HEALTH = new DoubleStat("RESTORE_HEALTH", VersionMaterial.RED_DYE.toMaterial(), "Health Restoration", new String[]{"Health given when consumed."}, new String[]{"consumable"}),
			RESTORE_FOOD = new DoubleStat("RESTORE_FOOD", VersionMaterial.PORKCHOP.toMaterial(), "Food Restoration", new String[]{"Food units given when consumed."}, new String[]{"consumable"}),
			RESTORE_SATURATION = new DoubleStat("RESTORE_SATURATION", Material.GOLDEN_CARROT, "Saturation Restoration", new String[]{"Saturation given when consumed."}, new String[]{"consumable"}),
			RESTORE_MANA = new DoubleStat("RESTORE_MANA", VersionMaterial.LAPIS_LAZULI.toMaterial(), "Restore Mana", new String[]{"The amount of mana", "your consumable restores."}, new String[]{"consumable"}),
			RESTORE_STAMINA = new DoubleStat("RESTORE_STAMINA", VersionMaterial.LIGHT_GRAY_DYE.toMaterial(), "Restore Stamina", new String[]{"The amount of stamina/power", "your consumable restores."}, new String[]{"consumable"}),
			CAN_IDENTIFY = new BooleanStat("CAN_IDENTIFY", Material.PAPER, "Can Identify?", new String[]{"Players can identify & make their", "item usable using this consumable."}, new String[]{"consumable"}),
			CAN_DECONSTRUCT = new BooleanStat("CAN_DECONSTRUCT", Material.PAPER, "Can Deconstruct?", new String[]{"Players can deconstruct their item", "using this consumable, creating", "another random item."}, new String[]{"consumable"}),
			CAN_DESKIN = new BooleanStat("CAN_DESKIN", Material.LEATHER, "Can Deskin?", new String[]{"Players can deskin their item", "and get their skin back", "from the item."}, new String[]{"consumable"}),
			EFFECTS = new Effects(),
			SOULBINDING_CHANCE = new DoubleStat("SOULBINDING_CHANCE", VersionMaterial.ENDER_EYE.toMaterial(), "Soulbinding Chance", new String[]{"Defines the chance your item has to", "link another item to your soul,", "preventing other players from using it."}, new String[]{"consumable"}),
			SOULBOUND_BREAK_CHANCE = new DoubleStat("SOULBOUND_BREAK_CHANCE", VersionMaterial.ENDER_EYE.toMaterial(), "Soulbound Break Chance", new String[]{"The chance of breaking an item's", "soulbound when drag & drop'd on it.", "This chance is lowered depending", "on the soulbound's level."}, new String[]{"consumable"}),
			SOULBOUND_LEVEL = new SoulboundLevel(),
			AUTO_SOULBIND = new BooleanStat("AUTO_SOULBIND", VersionMaterial.ENDER_EYE.toMaterial(), "Auto-Soulbind", new String[]{"Automatically soulbinds this item to", "a player when he acquires it."}, new String[]{"!consumable", "all"}),
			ITEM_COOLDOWN = new DoubleStat("ITEM_COOLDOWN", Material.COOKED_CHICKEN, "Item Cooldown", new String[]{"This cooldown applies for consumables", "as well as for item commands."}, new String[]{"!armor", "!gem_stone", "!block", "all"}),
			VANILLA_EATING_ANIMATION = new VanillaEatingAnimation(),
			INEDIBLE = new Inedible(),
			GEM_COLOR = new GemColor(),
			ITEM_TYPE_RESTRICTION = new ItemTypeRestriction(),
			MAX_CONSUME = new DoubleStat("MAX_CONSUME", Material.BLAZE_POWDER, "Max Consume", new String[]{"Max amount of usage before", "item disappears."}, new String[]{"consumable"}),
			SUCCESS_RATE = new SuccessRate(),
			COMPATIBLE_TYPES = new CompatibleTypes(),

	// Crafting Stats
	CRAFTING = new Crafting(),
			CRAFT_PERMISSION = new StringStat("CRAFT_PERMISSION", VersionMaterial.OAK_SIGN.toMaterial(), "Crafting Recipe Permission", new String[]{"The permission needed to craft this item.", "Changing this value requires &o/mi reload recipes&7."}, new String[]{"all"}),
			CRAFT_AMOUNT = new DoubleStat("CRAFTED_AMOUNT", Material.WOODEN_AXE, "Crafted Amount", new String[]{"The stack count for", "this item when crafted."}, new String[]{"all"}),

	// Unique Stats
	AUTOSMELT = new BooleanStat("AUTOSMELT", Material.COAL, "Autosmelt", new String[]{"If set to true, your tool will", "automaticaly smelt mined ores."}, new String[]{"tool"}),
			BOUNCING_CRACK = new BooleanStat("BOUNCING_CRACK", VersionMaterial.COBBLESTONE_WALL.toMaterial(), "Bouncing Crack", new String[]{"If set to true, your tool will", "also break nearby blocks."}, new String[]{"tool"}),
			PICKAXE_POWER = new PickaxePower(),
			CUSTOM_SOUNDS = new CustomSounds(),
			ELEMENTS = new Elements(),
			COMMANDS = new Commands(),
			STAFF_SPIRIT = new StaffSpiritStat(),
			LUTE_ATTACK_SOUND = new LuteAttackSoundStat(),
			LUTE_ATTACK_EFFECT = new LuteAttackEffectStat(),
			NOTE_WEIGHT = new DoubleStat("NOTE_WEIGHT", VersionMaterial.MUSIC_DISC_MALL.toMaterial(), "Note Weight", new String[]{"Defines how the projectile cast", "by your lute tilts downwards."}, new String[]{"lute"}),
			REMOVE_ON_CRAFT = new BooleanStat("REMOVE_ON_CRAFT", Material.GLASS_BOTTLE, "Remove on Craft", new String[]{"If the item should be completely", "removed when used in a recipe,", "or if it should become an", "empty bottle or bucket."}, new String[]{"all"}, Material.POTION, Material.SPLASH_POTION, Material.LINGERING_POTION, Material.MILK_BUCKET, Material.LAVA_BUCKET, Material.WATER_BUCKET),
			GEM_SOCKETS = new GemSockets(),
			REPAIR = new DoubleStat("REPAIR", Material.ANVIL, "Repair", new String[]{"The amount of durability your item", "can repair when set an item."}, new String[]{"consumable"}),
	// REPAIR_MATERIAL = new RepairMaterial(),
	KNOCKBACK = new DoubleStat("KNOCKBACK", VersionMaterial.IRON_HORSE_ARMOR.toMaterial(), "Knockback", new String[]{"Using this musket will knock", "the user back if positive."}, new String[]{"musket"}),
			RECOIL = new DoubleStat("RECOIL", VersionMaterial.IRON_HORSE_ARMOR.toMaterial(), "Recoil", new String[]{"Corresponds to the shooting innacuracy."}, new String[]{"musket"}),

	// Abilities & Upgrading
	ABILITIES = new Abilities(),
			UPGRADE = new UpgradeStat(),

	// Unique Item Stats
	SKULL_TEXTURE = new SkullTextureStat(),
			DYE_COLOR = new DyeColor(),
			HIDE_DYE = new HideDye(),
			POTION_EFFECTS = new PotionEffects(),
			POTION_COLOR = new PotionColor(),
			SHIELD_PATTERN = new ShieldPatternStat(),
			HIDE_POTION_EFFECTS = new HidePotionEffects(),

	// Internal Stats
	SOULBOUND = new Soulbound(),
			STORED_TAGS = new StoredTags(),
			ITEM_LEVEL = new ItemLevel();
}
