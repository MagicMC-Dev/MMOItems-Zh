package net.Indyuce.mmoitems.stat.type;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ConfigFile;
import net.Indyuce.mmoitems.api.item.MMOItem;
import net.Indyuce.mmoitems.api.item.build.MMOItemBuilder;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.Abilities;
import net.Indyuce.mmoitems.stat.Armor;
import net.Indyuce.mmoitems.stat.Armor_Toughness;
import net.Indyuce.mmoitems.stat.Arrow_Particles;
import net.Indyuce.mmoitems.stat.Attack_Damage;
import net.Indyuce.mmoitems.stat.Attack_Speed;
import net.Indyuce.mmoitems.stat.Commands;
import net.Indyuce.mmoitems.stat.Compatible_Types;
import net.Indyuce.mmoitems.stat.Craft_Permission;
import net.Indyuce.mmoitems.stat.Crafting;
import net.Indyuce.mmoitems.stat.CustomSounds;
import net.Indyuce.mmoitems.stat.Custom_Model_Data;
import net.Indyuce.mmoitems.stat.Disable_AdvancedEnchantments;
import net.Indyuce.mmoitems.stat.Display_Name;
import net.Indyuce.mmoitems.stat.Dye_Color;
import net.Indyuce.mmoitems.stat.Effects;
import net.Indyuce.mmoitems.stat.Elements;
import net.Indyuce.mmoitems.stat.Enchants;
import net.Indyuce.mmoitems.stat.Gem_Color;
import net.Indyuce.mmoitems.stat.Gem_Sockets;
import net.Indyuce.mmoitems.stat.Hide_Enchants;
import net.Indyuce.mmoitems.stat.Hide_Potion_Effects;
import net.Indyuce.mmoitems.stat.Inedible;
import net.Indyuce.mmoitems.stat.Item_Particles;
import net.Indyuce.mmoitems.stat.Item_Set;
import net.Indyuce.mmoitems.stat.Item_Tier;
import net.Indyuce.mmoitems.stat.Item_Type_Restriction;
import net.Indyuce.mmoitems.stat.Knockback_Resistance;
import net.Indyuce.mmoitems.stat.Lore;
import net.Indyuce.mmoitems.stat.Lost_when_Broken;
import net.Indyuce.mmoitems.stat.Lute_Attack_Effect;
import net.Indyuce.mmoitems.stat.Lute_Attack_Sound;
import net.Indyuce.mmoitems.stat.MaterialStat;
import net.Indyuce.mmoitems.stat.Max_Health;
import net.Indyuce.mmoitems.stat.Maximum_Durability;
import net.Indyuce.mmoitems.stat.Movement_Speed;
import net.Indyuce.mmoitems.stat.NBT_Tags;
import net.Indyuce.mmoitems.stat.Perm_Effects;
import net.Indyuce.mmoitems.stat.Permission;
import net.Indyuce.mmoitems.stat.Pickaxe_Power;
import net.Indyuce.mmoitems.stat.Potion_Color;
import net.Indyuce.mmoitems.stat.Potion_Effects;
import net.Indyuce.mmoitems.stat.Required_Class;
import net.Indyuce.mmoitems.stat.Required_Level;
import net.Indyuce.mmoitems.stat.Restore;
import net.Indyuce.mmoitems.stat.Shield_Pattern;
import net.Indyuce.mmoitems.stat.Skull_Texture;
import net.Indyuce.mmoitems.stat.Soulbound;
import net.Indyuce.mmoitems.stat.Soulbound_Level;
import net.Indyuce.mmoitems.stat.Staff_Spirit;
import net.Indyuce.mmoitems.stat.Unbreakable;
import net.Indyuce.mmoitems.stat.Upgrade_Stat;
import net.Indyuce.mmoitems.stat.Vanilla_Eating_Animation;
import net.Indyuce.mmoitems.stat.data.StatData;
import net.Indyuce.mmoitems.version.durability.stat.DefaultDurability;
import net.Indyuce.mmoitems.version.durability.stat.LegacyDurability;
import net.mmogroup.mmolib.MMOLib;
import net.mmogroup.mmolib.api.item.NBTItem;
import net.mmogroup.mmolib.version.VersionMaterial;

public abstract class ItemStat {
	public static final ItemStat MATERIAL = new MaterialStat(), DURABILITY = MMOLib.plugin.getVersion().isBelowOrEqual(1, 12) ? new LegacyDurability() : new DefaultDurability(), CUSTOM_MODEL_DATA = new Custom_Model_Data(), MAX_CUSTOM_DURABILITY = new Maximum_Durability(), WILL_BREAK = new Lost_when_Broken();
	public static final ItemStat NAME = new Display_Name(), LORE = new Lore(), NBT_TAGS = new NBT_Tags();

	public static final ItemStat DISPLAYED_TYPE = new StringStat(new ItemStack(VersionMaterial.OAK_SIGN.toMaterial()), "Displayed Type", new String[] { "This option will only affect the", "type displayed on the item lore." }, "displayed-type", new String[] { "all" });
	public static final ItemStat ENCHANTS = new Enchants(), HIDE_ENCHANTS = new Hide_Enchants(), PERMISSION = new Permission(), ITEM_PARTICLES = new Item_Particles(), ARROW_PARTICLES = new Arrow_Particles();
	public static final ItemStat DISABLE_INTERACTION = new DisableStat(VersionMaterial.GRASS_BLOCK.toMaterial(), "interaction", "Disable Interaction", "Disable any unwanted interaction:", "block placement, item use...");
	public static final ItemStat DISABLE_CRAFTING = new DisableStat(VersionMaterial.CRAFTING_TABLE.toMaterial(), "crafting", "Disable Crafting", "Players can't use this item while crafting.");
	public static final ItemStat DISABLE_SMELTING = new DisableStat(Material.FURNACE, "smelting", "Disable Smelting", "Players can't use this item in furnaces.");
	public static final ItemStat DISABLE_ENCHANTING = new DisableStat(VersionMaterial.ENCHANTING_TABLE.toMaterial(), "enchanting", "Disable Enchanting", "Players can't enchant this item."), DISABLE_ADVANCED_ENCHANTS = new Disable_AdvancedEnchantments();
	public static final ItemStat DISABLE_REPAIRING = new DisableStat(Material.ANVIL, "repairing", "Disable Repairing", "Players can't use this item in anvils.");
	public static final ItemStat DISABLE_ARROW_SHOOTING = new DisableStat(Material.ARROW, "arrow-shooting", "Disable Arrow Shooting", new Material[] { Material.ARROW }, "Players can't shoot this", "item using a bow.");
	public static final ItemStat DISABLE_ATTACK_PASSIVE = new DisableStat(Material.BARRIER, "attack-passive", "Disable Attack Passive", new String[] { "piercing", "slashing", "blunt" }, "Disables the blunt/slashing/piercing", "passive effects on attacks.");

	public static final ItemStat PVE_DEFENSE = new DoubleStat(new ItemStack(Material.BOOK), "PvE Defense", new String[] {"Additional Defense against","non human entities in %."}, "pve-defense", new String[] {"armor"});
	public static final ItemStat PVP_DEFENSE = new DoubleStat(new ItemStack(Material.BOOK), "PvP Defense", new String[] {"Additional Defense against","human entities in %."}, "pvp-defense", new String[] {"armor"});
	public static final ItemStat REQUIRED_LEVEL = new Required_Level(), REQUIRED_CLASS = new Required_Class(), ATTACK_DAMAGE = new Attack_Damage(), ATTACK_SPEED = new Attack_Speed();
	public static final ItemStat CRITICAL_STRIKE_CHANCE = new DoubleStat(new ItemStack(Material.NETHER_STAR), "Critical Strike Chance", new String[] { "Critical Strikes deal more damage.", "In % chance." }, "critical-strike-chance", new String[] { "!miscellaneous", "all" });
	public static final ItemStat CRITICAL_STRIKE_POWER = new DoubleStat(new ItemStack(Material.NETHER_STAR), "Critical Strike Power", new String[] { "The extra damage weapon crits deals.", "(Stacks with default value)", "In %." }, "critical-strike-power", new String[] { "!miscellaneous", "all" });
	public static final ItemStat BLOCK_POWER = new DoubleStat(new ItemStack(Material.IRON_HELMET), "Block Power", new String[] { "The % of the damage your", "armor/shield can block.", "Default: 25%" }, "block-power", new String[] { "!miscellaneous", "all" });
	public static final ItemStat BLOCK_RATING = new DoubleStat(new ItemStack(Material.IRON_HELMET), "Block Rating", new String[] { "The chance your piece of armor", "has to block any entity attack." }, "block-rating", new String[] { "!miscellaneous", "all" });
	public static final ItemStat BLOCK_COOLDOWN_REDUCTION = new DoubleStat(new ItemStack(Material.IRON_HELMET), "Block Cooldown Reduction", new String[] { "Reduces the blocking cooldown (%)." }, "block-cooldown-reduction", new String[] { "!miscellaneous", "all" });
	public static final ItemStat DODGE_RATING = new DoubleStat(new ItemStack(Material.FEATHER), "Dodge Rating", new String[] { "The chance to dodge an attack.", "Dodging completely negates", "the attack damage." }, "dodge-rating", new String[] { "!miscellaneous", "all" });
	public static final ItemStat DODGE_COOLDOWN_REDUCTION = new DoubleStat(new ItemStack(Material.FEATHER), "Dodge Cooldown Reduction", new String[] { "Reduces the dodging cooldown (%)." }, "dodge-cooldown-reduction", new String[] { "!miscellaneous", "all" });
	public static final ItemStat PARRY_RATING = new DoubleStat(new ItemStack(Material.BUCKET), "Parry Rating", new String[] { "The chance to parry an attack.", "Parrying negates the damage", "and knocks the attacker back." }, "parry-rating", new String[] { "!miscellaneous", "all" });
	public static final ItemStat PARRY_COOLDOWN_REDUCTION = new DoubleStat(new ItemStack(Material.BUCKET), "Parry Cooldown Reduction", new String[] { "Reduces the parrying cooldown (%)." }, "parry-cooldown-reduction", new String[] { "!miscellaneous", "all" });
	public static final ItemStat RANGE = new DoubleStat(new ItemStack(Material.STICK), "Range", new String[] { "The range of your item attacks." }, "range", new String[] { "staff", "whip", "wand", "musket" });
	public static final ItemStat MANA_COST = new DoubleStat(VersionMaterial.LAPIS_LAZULI.toItem(), "Mana Cost", new String[] { "Mana spent by your weapon to be used." }, "mana-cost", new String[] { "piercing", "slashing", "blunt", "range" });
	public static final ItemStat STAMINA_COST = new DoubleStat(VersionMaterial.LIGHT_GRAY_DYE.toItem(), "Stamina Cost", new String[] { "Stamina spent by your weapon to be used." }, "stamina-cost", new String[] { "piercing", "slashing", "blunt", "range" });
	public static final ItemStat ARROW_VELOCITY = new DoubleStat(new ItemStack(Material.ARROW), "Arrow Velocity", new String[] { "Determins how far your", "crossbow can shoot.", "Default: 1.0" }, "arrow-velocity", new String[] { "bow", "crossbow" });
	public static final ItemStat PVE_DAMAGE = new DoubleStat(new ItemStack(VersionMaterial.PORKCHOP.toMaterial()), "PvE Damage", new String[] { "Additional damage against", "non human entities in %." }, "pve-damage", new String[] { "piercing", "slashing", "blunt", "offhand", "range", "tool", "armor", "gem_stone", "accessory" });
	public static final ItemStat PVP_DAMAGE = new DoubleStat(new ItemStack(VersionMaterial.SKELETON_SKULL.toMaterial()), "PvP Damage", new String[] { "Additional damage", "against players in %." }, "pvp-damage", new String[] { "piercing", "slashing", "blunt", "offhand", "range", "tool", "armor", "gem_stone", "accessory" });
	public static final ItemStat BLUNT_POWER = new DoubleStat(new ItemStack(Material.IRON_AXE), "Blunt Power", new String[] { "The radius of the AoE attack.", "If set to 2.0, enemies within 2 blocks", "around your target will take damage." }, "blunt-power", new String[] { "blunt", "gem_stone" });
	public static final ItemStat BLUNT_RATING = new DoubleStat(new ItemStack(Material.BRICK), "Blunt Rating", new String[] { "The force of the blunt attack.", "If set to 50%, enemies hit by the attack", "will take 50% of the initial damage." }, "blunt-rating", new String[] { "blunt", "gem_stone" });
	public static final ItemStat WEAPON_DAMAGE = new DoubleStat(new ItemStack(Material.IRON_SWORD), "Weapon Damage", new String[] { "Additional on-hit weapon damage in %." }, "weapon-damage");
	public static final ItemStat SKILL_DAMAGE = new DoubleStat(new ItemStack(Material.BOOK), "Skill Damage", new String[] { "Additional ability damage in %." }, "skill-damage");
	public static final ItemStat PROJECTILE_DAMAGE = new DoubleStat(new ItemStack(Material.ARROW), "Projectile Damage", new String[] { "Additional skill/weapon projectile damage." }, "projectile-damage");
	public static final ItemStat MAGIC_DAMAGE = new DoubleStat(new ItemStack(Material.BOOK), "Magic Damage", new String[] { "Additional magic skill damage in %." }, "magic-damage");
	public static final ItemStat PHYSICAL_DAMAGE = new DoubleStat(new ItemStack(Material.IRON_AXE), "Physical Damage", new String[] { "Additional skill/weapon physical damage." }, "physical-damage");
	public static final ItemStat DAMAGE_REDUCTION = new DoubleStat(new ItemStack(Material.IRON_CHESTPLATE), "Damage Reduction", new String[] { "Reduces damage from any source.", "In %." }, "damage-reduction");
	public static final ItemStat FALL_DAMAGE_REDUCTION = new DoubleStat(new ItemStack(Material.FEATHER), "Fall Damage Reduction", new String[] { "Reduces fall damage.", "In %." }, "fall-damage-reduction");
	public static final ItemStat PROJECTILE_DAMAGE_REDUCTION = new DoubleStat(new ItemStack(Material.IRON_CHESTPLATE), "Projectile Damage Reduction", new String[] { "Reduces projectile damage.", "In %." }, "projectile-damage-reduction");
	public static final ItemStat PHYSICAL_DAMAGE_REDUCTION = new DoubleStat(new ItemStack(Material.LEATHER_CHESTPLATE), "Physical Damage Reduction", new String[] { "Reduces physical damage.", "In %." }, "physical-damage-reduction");
	public static final ItemStat FIRE_DAMAGE_REDUCTION = new DoubleStat(new ItemStack(Material.BLAZE_POWDER), "Fire Damage Reduction", new String[] { "Reduces fire damage.", "In %." }, "fire-damage-reduction");
	public static final ItemStat MAGIC_DAMAGE_REDUCTION = new DoubleStat(new ItemStack(Material.POTION), "Magic Damage Reduction", new String[] { "Reduce magic damage dealt by potions.", "In %." }, "magic-damage-reduction");
	public static final ItemStat UNDEAD_DAMAGE = new DoubleStat(new ItemStack(VersionMaterial.SKELETON_SKULL.toMaterial()), "Undead Damage", new String[] { "Deals additional damage to undead.", "In %." }, "undead-damage");
	public static final ItemStat REGENERATION = new DoubleStat(new ItemStack(Material.BREAD), "Regeneration", new String[] { "Increases natural/magic health regen.", "In %." }, "regeneration");
	public static final ItemStat UNBREAKABLE = new Unbreakable(), TIER = new Item_Tier(), SET = new Item_Set(), ARMOR = new Armor(), ARMOR_TOUGHNESS = new Armor_Toughness(), MAX_HEALTH = new Max_Health();
	public static final ItemStat MAX_MANA = new DoubleStat(VersionMaterial.LAPIS_LAZULI.toItem(), "Max Mana", new String[] { "Adds mana to your max mana bar." }, "max-mana");
	public static final ItemStat KNOCKBACK_RESISTANCE = new Knockback_Resistance(), MOVEMENT_SPEED = new Movement_Speed();

	public static final ItemStat TWO_HANDED = new BooleanStat(new ItemStack(Material.IRON_INGOT), "Two Handed", new String[] { "If set to true, a player will be", "significantly slower if holding two", "items, one being Two Handed." }, "two-handed", new String[] { "piercing", "slashing", "blunt", "offhand", "range", "tool" });

	public static final ItemStat RESTORE = new Restore();
	public static final ItemStat RESTORE_MANA = new DoubleStat(VersionMaterial.LAPIS_LAZULI.toItem(), "Restore Mana", new String[] { "The amount of mana", "your consumable restores." }, "restore-mana", new String[] { "consumable" });
	public static final ItemStat RESTORE_STAMINA = new DoubleStat(VersionMaterial.LIGHT_GRAY_DYE.toItem(), "Restore Stamina", new String[] { "The amount of stamina/power", "your consumable restores." }, "restore-stamina", new String[] { "consumable" });
	public static final ItemStat CAN_IDENTIFY = new BooleanStat(new ItemStack(Material.PAPER), "Can Identify?", new String[] { "Players can identify & make their", "item usable using this consumable." }, "can-identify", new String[] { "consumable" });
	public static final ItemStat CAN_DECONSTRUCT = new BooleanStat(new ItemStack(Material.PAPER), "Can Deconstruct?", new String[] { "Players can deconstruct their item", "using this consumable, creating", "another random item." }, "can-deconstruct", new String[] { "consumable" });
	public static final ItemStat EFFECTS = new Effects(), PERM_EFFECTS = new Perm_Effects();
	public static final ItemStat SOULBINDING_CHANCE = new DoubleStat(new ItemStack(VersionMaterial.ENDER_EYE.toMaterial()), "Soulbinding Chance", new String[] { "Defines the chance your item has to", "link another item to your soul,", "preventing other players from using it." }, "soulbinding-chance", new String[] { "consumable" });
	public static final ItemStat SOULBOUND_BREAK_CHANCE = new DoubleStat(new ItemStack(VersionMaterial.ENDER_EYE.toMaterial()), "Soulbound Break Chance", new String[] { "The chance of breaking an item's", "soulbound when drag & drop'd on it.", "This chance is lowered depending", "on the soulbound's level." }, "soulbound-break-chance", new String[] { "consumable" });
	public static final ItemStat SOULBOUND_LEVEL = new Soulbound_Level();
	public static final ItemStat ITEM_COOLDOWN = new DoubleStat(new ItemStack(Material.COOKED_CHICKEN), "Item Cooldown", new String[] { "This cooldown applies for consumables", "as well as for item commands." }, "item-cooldown", new String[] { "!armor", "!gem_stone", "all" });
	public static final ItemStat DISABLE_RIGHT_CLICK_CONSUME = new DisableStat(Material.BARRIER, "right-click-consume", "Disable Right Click Consume", new String[] { "consumable" }, "This item will not be consumed", "when eaten by players.");
	public static final ItemStat VANILLA_EATING_ANIMATION = new Vanilla_Eating_Animation(), INEDIBLE = new Inedible(), GEM_COLOR = new Gem_Color(), ITEM_TYPE_RESTRICTION = new Item_Type_Restriction();
	public static final ItemStat MAX_CONSUME = new DoubleStat(new ItemStack(Material.BLAZE_POWDER), "Max Consume", new String[] {"Max amount of usage before", "item disappears."}, "max-consume", new String[] {"consumable"});

	public static final ItemStat SUCCESS_RATE = new DoubleStat(new ItemStack(Material.EMERALD), "Success Rate", new String[] { "The chance of your gem to successfully", "apply onto an item. This value is 100%", "by default. If it is not successfully", "applied, the gem stone will be lost." }, "success-rate", new String[] { "gem_stone", "skin" });
	public static final ItemStat COMPATIBLE_TYPES = new Compatible_Types();
	
	public static final ItemStat CRAFTING = new Crafting(), CRAFT_PERMISSION = new Craft_Permission();
	public static final ItemStat AUTOSMELT = new BooleanStat(new ItemStack(Material.COAL), "Autosmelt", new String[] { "If set to true, your tool will", "automaticaly smelt mined ores." }, "autosmelt", new String[] { "tool" });
	public static final ItemStat BOUNCING_CRACK = new BooleanStat(new ItemStack(VersionMaterial.COBBLESTONE_WALL.toMaterial()), "Bouncing Crack", new String[] { "If set to true, your tool will", "also break nearby blocks." }, "bouncing-crack", new String[] { "tool" });
	public static final ItemStat PICKAXE_POWER = new Pickaxe_Power();
	public static final ItemStat CUSTOM_SOUNDS = new CustomSounds();
	public static final ItemStat ELEMENTS = new Elements();
	public static final ItemStat COMMANDS = new Commands(), STAFF_SPIRIT = new Staff_Spirit(), LUTE_ATTACK_SOUND = new Lute_Attack_Sound(), LUTE_ATTACK_EFFECT = new Lute_Attack_Effect();
	public static final ItemStat NOTE_WEIGHT = new DoubleStat(new ItemStack(VersionMaterial.MUSIC_DISC_MALL.toMaterial()), "Note Weight", new String[] { "Defines how the projectile cast", "by your lute tilts downwards." }, "note-weight", new String[] { "lute" });
	public static final ItemStat GEM_SOCKETS = new Gem_Sockets();
	public static final ItemStat REPAIR = new DoubleStat(new ItemStack(Material.ANVIL), "Repair", new String[] { "The amount of durability your item", "can repair when set an item." }, "repair", new String[] { "consumable" });

	public static final ItemStat KNOCKBACK = new DoubleStat(new ItemStack(VersionMaterial.IRON_HORSE_ARMOR.toMaterial()), "Knockback", new String[] { "Using this musket will knock", "the user back if positive." }, "knockback", new String[] { "musket" });
	public static final ItemStat RECOIL = new DoubleStat(new ItemStack(VersionMaterial.IRON_HORSE_ARMOR.toMaterial()), "Recoil", new String[] { "Corresponds to the shooting innacuracy." }, "recoil", new String[] { "musket" });

	public static final ItemStat ABILITIES = new Abilities(), UPGRADE = new Upgrade_Stat();
	public static final ItemStat SKULL_TEXTURE = new Skull_Texture(), DYE_COLOR = new Dye_Color(), POTION_EFFECTS = new Potion_Effects(), POTION_COLOR = new Potion_Color(), SHIELD_PATTERN = new Shield_Pattern(), HIDE_POTION_EFFECTS = new Hide_Potion_Effects();

	/*
	 * internal stats
	 */
	public static final Soulbound SOULBOUND = new Soulbound();

	private String id;
	private final String name, path;
	private final ItemStack item;

	private final String[] lore, compatibleTypes;
	private final List<Material> compatibleMaterials;

	/*
	 * the stat can be enabled or not, depending on the server version to
	 * prevent from displaying useless editable stats in the edition menu.
	 */
	private boolean enabled = true;

	public ItemStat(String name, String[] lore, String path, String[] types) {
		this(new ItemStack(Material.BARRIER), name, lore, path, types);
	}

	public ItemStat(ItemStack item, String name, String[] lore, String path, String[] types, Material... materials) {
		this.item = item;
		this.lore = lore == null ? new String[0] : lore;
		this.compatibleTypes = types == null ? new String[0] : types;
		this.path = path;
		this.name = name;
		this.compatibleMaterials = materials == null ? new ArrayList<>() : Arrays.asList(materials);
	}

	/*
	 * reads stat data from a configuration section and applies it to the item
	 * stack after having generated the corresponding stat data class instance
	 */
	public abstract boolean whenLoaded(MMOItem item, ConfigurationSection config);

	/*
	 * applies a stat onto an mmoitem builder instance
	 */
	public abstract boolean whenApplied(MMOItemBuilder item, StatData data);

	/*
	 * when the stat item is clicked in the item edition menu
	 */
	public abstract boolean whenClicked(EditionInventory inv, InventoryClickEvent event);

	/*
	 * when entering input using the chat edition feature from the item edition
	 * menu
	 */
	public abstract boolean whenInput(EditionInventory inv, ConfigFile config, String message, Object... info);

	/*
	 * when loading mmoitem data from an ItemStack
	 */
	public abstract void whenLoaded(MMOItem mmoitem, NBTItem item);

	/*
	 * displays the current stat state/value in the item edition GUI, the lore
	 * corresponds to the GUI stat item
	 */
	public abstract void whenDisplayed(List<String> lore, FileConfiguration config, String id);

	public String getName() {
		return name;
	}

	public String getId() {
		return id;
	}

	@Deprecated
	public String name() {
		return id;
	}

	/*
	 * TODO in a distant future, remove path field and make it depend only on
	 * path to reduce confusion.
	 */
	public String getPath() {
		return path;
	}

	public String getNBTPath() {
		return "MMOITEMS_" + path.toUpperCase().replace("-", "_");
	}

	public ItemStack getItem() {
		return item.clone();
	}

	public boolean isEnabled() {
		return enabled;
	}

	public boolean isInternal() {
		return this instanceof InternalStat;
	}

	public String[] getLore() {
		return lore;
	}

	public String[] getCompatibleTypes() {
		return compatibleTypes;
	}

	public void setId(String id) {
		this.id = id;
	}

	public boolean hasValidMaterial(ItemStack item) {
		if (compatibleMaterials.size() == 0)
			return true;

		for (Material dm : compatibleMaterials)
			if (item.getType() == dm)
				return true;
		return false;
	}

	public void setItemType(Material material) {
		item.setType(material);
	}

	public void disable() {
		enabled = false;
	}

	public void addCompatibleMaterial(Material... values) {
		for (Material dm : values)
			compatibleMaterials.add(dm);
	}

	public String format(double value, String... replace) {
		String format = translate().replace("<plus>", value > 0 ? "+" : "");
		for (int j = 0; j < replace.length; j += 2)
			format = format.replace(replace[j], replace[j + 1]);
		return format;
	}

	public String translate() {
		String str = MMOItems.plugin.getLanguage().getStatFormat(path);
		return str == null ? "<TranslationNotFound:" + path + ">" : str;
	}

	@Override
	public boolean equals(Object obj) {
		return obj != null && obj instanceof ItemStat && ((ItemStat) obj).getId().equals(getId());
	}

	public static String translate(String path) {
		String str = MMOItems.plugin.getLanguage().getStatFormat(path);
		return str == null ? "<TranslationNotFound:" + path + ">" : str;
	}
}