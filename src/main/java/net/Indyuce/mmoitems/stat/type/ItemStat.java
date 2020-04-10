package net.Indyuce.mmoitems.stat.type;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ConfigFile;
import net.Indyuce.mmoitems.api.item.MMOItem;
import net.Indyuce.mmoitems.api.item.build.MMOItemBuilder;
import net.Indyuce.mmoitems.api.itemgen.RandomStatData;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.Abilities;
import net.Indyuce.mmoitems.stat.Armor;
import net.Indyuce.mmoitems.stat.ArmorToughness;
import net.Indyuce.mmoitems.stat.ArrowParticles;
import net.Indyuce.mmoitems.stat.AttackDamage;
import net.Indyuce.mmoitems.stat.AttackSpeed;
import net.Indyuce.mmoitems.stat.Commands;
import net.Indyuce.mmoitems.stat.CompatibleTypes;
import net.Indyuce.mmoitems.stat.CraftingPermission;
import net.Indyuce.mmoitems.stat.Crafting;
import net.Indyuce.mmoitems.stat.CustomSounds;
import net.Indyuce.mmoitems.stat.CustomModelData;
import net.Indyuce.mmoitems.stat.DefaultDurability;
import net.Indyuce.mmoitems.stat.DisableAdvancedEnchantments;
import net.Indyuce.mmoitems.stat.DisplayName;
import net.Indyuce.mmoitems.stat.DyeColor;
import net.Indyuce.mmoitems.stat.Effects;
import net.Indyuce.mmoitems.stat.Elements;
import net.Indyuce.mmoitems.stat.Enchants;
import net.Indyuce.mmoitems.stat.GemColor;
import net.Indyuce.mmoitems.stat.GemSockets;
import net.Indyuce.mmoitems.stat.HideEnchants;
import net.Indyuce.mmoitems.stat.HidePotionEffects;
import net.Indyuce.mmoitems.stat.Inedible;
import net.Indyuce.mmoitems.stat.ItemParticles;
import net.Indyuce.mmoitems.stat.ItemSetStat;
import net.Indyuce.mmoitems.stat.ItemTierStat;
import net.Indyuce.mmoitems.stat.ItemTypeRestriction;
import net.Indyuce.mmoitems.stat.KnockbackResistance;
import net.Indyuce.mmoitems.stat.LegacyDurability;
import net.Indyuce.mmoitems.stat.Lore;
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
import net.Indyuce.mmoitems.stat.RepairMaterial;
import net.Indyuce.mmoitems.stat.RequiredClass;
import net.Indyuce.mmoitems.stat.RequiredLevel;
import net.Indyuce.mmoitems.stat.Restore;
import net.Indyuce.mmoitems.stat.ShieldPatternStat;
import net.Indyuce.mmoitems.stat.SkullTextureStat;
import net.Indyuce.mmoitems.stat.Soulbound;
import net.Indyuce.mmoitems.stat.SoulboundLevel;
import net.Indyuce.mmoitems.stat.StaffSpiritStat;
import net.Indyuce.mmoitems.stat.StoredTags;
import net.Indyuce.mmoitems.stat.SuccessRate;
import net.Indyuce.mmoitems.stat.Unbreakable;
import net.Indyuce.mmoitems.stat.UpgradeStat;
import net.Indyuce.mmoitems.stat.VanillaEatingAnimation;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.mmogroup.mmolib.MMOLib;
import net.mmogroup.mmolib.api.item.NBTItem;
import net.mmogroup.mmolib.version.VersionMaterial;

public abstract class ItemStat {
	public static final ItemStat MATERIAL = new MaterialStat(),
			DURABILITY = MMOLib.plugin.getVersion().isBelowOrEqual(1, 12) ? new LegacyDurability() : new DefaultDurability(),
			CUSTOM_MODEL_DATA = new CustomModelData(), MAX_DURABILITY = new MaximumDurability(), WILL_BREAK = new LostWhenBroken();
	public static final ItemStat NAME = new DisplayName(), LORE = new Lore(), NBT_TAGS = new NBTTags();

	public static final ItemStat DISPLAYED_TYPE = new StringStat("DISPLAYED_TYPE", VersionMaterial.OAK_SIGN.toItem(), "Displayed Type",
			new String[] { "This option will only affect the", "type displayed on the item lore." }, new String[] { "all" });
	public static final ItemStat ENCHANTS = new Enchants(), HIDE_ENCHANTS = new HideEnchants(), PERMISSION = new Permission(),
			ITEM_PARTICLES = new ItemParticles(), ARROW_PARTICLES = new ArrowParticles();
	public static final ItemStat DISABLE_INTERACTION = new DisableStat("INTERACTION", VersionMaterial.GRASS_BLOCK.toMaterial(), "Disable Interaction",
			"Disable any unwanted interaction:", "block placement, item use...");
	public static final ItemStat DISABLE_CRAFTING = new DisableStat("CRAFTING", VersionMaterial.CRAFTING_TABLE.toMaterial(), "Disable Crafting",
			"Players can't use this item while crafting.");
	public static final ItemStat DISABLE_SMELTING = new DisableStat("SMELTING", Material.FURNACE, "Disable Smelting",
			"Players can't use this item in furnaces.");
	public static final ItemStat DISABLE_ENCHANTING = new DisableStat("ENCHANTING", VersionMaterial.ENCHANTING_TABLE.toMaterial(),
			"Disable Enchanting", "Players can't enchant this item."), DISABLE_ADVANCED_ENCHANTS = new DisableAdvancedEnchantments();
	public static final ItemStat DISABLE_REPAIRING = new DisableStat("REPAIRING", Material.ANVIL, "Disable Repairing",
			"Players can't use this item in anvils.");
	public static final ItemStat DISABLE_ARROW_SHOOTING = new DisableStat("ARROW_SHOOTING", Material.ARROW, "Disable Arrow Shooting",
			new Material[] { Material.ARROW }, "Players can't shoot this", "item using a bow.");
	public static final ItemStat DISABLE_ATTACK_PASSIVE = new DisableStat("ATTACK_PASSIVE", Material.BARRIER, "Disable Attack Passive",
			new String[] { "piercing", "slashing", "blunt" }, "Disables the blunt/slashing/piercing", "passive effects on attacks.");
	public static final ItemStat DISABLE_RIGHT_CLICK_CONSUME = new DisableStat("RIGHT_CLICK_CONSUME", Material.BARRIER, "Disable Right Click Consume",
			new String[] { "consumable" }, "This item will not be consumed", "when eaten by players.");

	public static final ItemStat REQUIRED_LEVEL = new RequiredLevel(), REQUIRED_CLASS = new RequiredClass(), ATTACK_DAMAGE = new AttackDamage(),
			ATTACK_SPEED = new AttackSpeed();
	public static final ItemStat CRITICAL_STRIKE_CHANCE = new DoubleStat("CRITICAL_STRIKE_CHANCE", new ItemStack(Material.NETHER_STAR),
			"Critical Strike Chance", new String[] { "Critical Strikes deal more damage.", "In % chance." },
			new String[] { "!miscellaneous", "all" });
	public static final ItemStat CRITICAL_STRIKE_POWER = new DoubleStat("CRITICAL_STRIKE_POWER", new ItemStack(Material.NETHER_STAR),
			"Critical Strike Power", new String[] { "The extra damage weapon crits deals.", "(Stacks with default value)", "In %." },
			new String[] { "!miscellaneous", "all" });
	public static final ItemStat BLOCK_POWER = new DoubleStat("BLOCK_POWER", new ItemStack(Material.IRON_HELMET), "Block Power",
			new String[] { "The % of the damage your", "armor/shield can block.", "Default: 25%" }, new String[] { "!miscellaneous", "all" });
	public static final ItemStat BLOCK_RATING = new DoubleStat("BLOCK_RATING", new ItemStack(Material.IRON_HELMET), "Block Rating",
			new String[] { "The chance your piece of armor", "has to block any entity attack." }, new String[] { "!miscellaneous", "all" });
	public static final ItemStat BLOCK_COOLDOWN_REDUCTION = new DoubleStat("BLOCK_COOLDOWN_REDUCTION", new ItemStack(Material.IRON_HELMET),
			"Block Cooldown Reduction", new String[] { "Reduces the blocking cooldown (%)." }, new String[] { "!miscellaneous", "all" });
	public static final ItemStat DODGE_RATING = new DoubleStat("DODGE_RATING", new ItemStack(Material.FEATHER), "Dodge Rating",
			new String[] { "The chance to dodge an attack.", "Dodging completely negates", "the attack damage." },
			new String[] { "!miscellaneous", "all" });
	public static final ItemStat DODGE_COOLDOWN_REDUCTION = new DoubleStat("DODGE_COOLDOWN_REDUCTION", new ItemStack(Material.FEATHER),
			"Dodge Cooldown Reduction", new String[] { "Reduces the dodging cooldown (%)." }, new String[] { "!miscellaneous", "all" });
	public static final ItemStat PARRY_RATING = new DoubleStat("PARRY_RATING", new ItemStack(Material.BUCKET), "Parry Rating",
			new String[] { "The chance to parry an attack.", "Parrying negates the damage", "and knocks the attacker back." },
			new String[] { "!miscellaneous", "all" });
	public static final ItemStat PARRY_COOLDOWN_REDUCTION = new DoubleStat("PARRY_COOLDOWN_REDUCTION", new ItemStack(Material.BUCKET),
			"Parry Cooldown Reduction", new String[] { "Reduces the parrying cooldown (%)." }, new String[] { "!miscellaneous", "all" });
	public static final ItemStat RANGE = new DoubleStat("RANGE", new ItemStack(Material.STICK), "Range",
			new String[] { "The range of your item attacks." }, new String[] { "staff", "whip", "wand", "musket" });
	public static final ItemStat MANA_COST = new DoubleStat("MANA_COST", VersionMaterial.LAPIS_LAZULI.toItem(), "Mana Cost",
			new String[] { "Mana spent by your weapon to be used." }, new String[] { "piercing", "slashing", "blunt", "range" });
	public static final ItemStat STAMINA_COST = new DoubleStat("STAMINA_COST", VersionMaterial.LIGHT_GRAY_DYE.toItem(), "Stamina Cost",
			new String[] { "Stamina spent by your weapon to be used." }, new String[] { "piercing", "slashing", "blunt", "range" });
	public static final ItemStat ARROW_VELOCITY = new DoubleStat("ARROW_VELOCITY", new ItemStack(Material.ARROW), "Arrow Velocity",
			new String[] { "Determins how far your", "crossbow can shoot.", "Default: 1.0" }, new String[] { "bow", "crossbow" });
	public static final ItemStat PVE_DAMAGE = new DoubleStat("PVE_DAMAGE", VersionMaterial.PORKCHOP.toItem(), "PvE Damage",
			new String[] { "Additional damage against", "non human entities in %." },
			new String[] { "piercing", "slashing", "blunt", "offhand", "range", "tool", "armor", "gem_stone", "accessory" });
	public static final ItemStat PVP_DAMAGE = new DoubleStat("PVP_DAMAGE", VersionMaterial.SKELETON_SKULL.toItem(), "PvP Damage",
			new String[] { "Additional damage", "against players in %." },
			new String[] { "piercing", "slashing", "blunt", "offhand", "range", "tool", "armor", "gem_stone", "accessory" });
	public static final ItemStat BLUNT_POWER = new DoubleStat("BLUNT_POWER", new ItemStack(Material.IRON_AXE), "Blunt Power",
			new String[] { "The radius of the AoE attack.", "If set to 2.0, enemies within 2 blocks", "around your target will take damage." },
			new String[] { "blunt", "gem_stone" });
	public static final ItemStat BLUNT_RATING = new DoubleStat("BLUNT_RATING", new ItemStack(Material.BRICK), "Blunt Rating",
			new String[] { "The force of the blunt attack.", "If set to 50%, enemies hit by the attack", "will take 50% of the initial damage." },
			new String[] { "blunt", "gem_stone" });
	public static final ItemStat WEAPON_DAMAGE = new DoubleStat("WEAPON_DAMAGE", new ItemStack(Material.IRON_SWORD), "Weapon Damage",
			new String[] { "Additional on-hit weapon damage in %." });
	public static final ItemStat SKILL_DAMAGE = new DoubleStat("SKILL_DAMAGE", new ItemStack(Material.BOOK), "Skill Damage",
			new String[] { "Additional ability damage in %." });
	public static final ItemStat PROJECTILE_DAMAGE = new DoubleStat("PROJECTILE_DAMAGE", new ItemStack(Material.ARROW), "Projectile Damage",
			new String[] { "Additional skill/weapon projectile damage." });
	public static final ItemStat MAGIC_DAMAGE = new DoubleStat("MAGIC_DAMAGE", new ItemStack(Material.BOOK), "Magic Damage",
			new String[] { "Additional magic skill damage in %." });
	public static final ItemStat PHYSICAL_DAMAGE = new DoubleStat("PHYSICAL_DAMAGE", new ItemStack(Material.IRON_AXE), "Physical Damage",
			new String[] { "Additional skill/weapon physical damage." });
	public static final ItemStat DAMAGE_REDUCTION = new DoubleStat("DAMAGE_REDUCTION", new ItemStack(Material.IRON_CHESTPLATE), "Damage Reduction",
			new String[] { "Reduces damage from any source.", "In %." });
	public static final ItemStat FALL_DAMAGE_REDUCTION = new DoubleStat("FALL_DAMAGE_REDUCTION", new ItemStack(Material.FEATHER),
			"Fall Damage Reduction", new String[] { "Reduces fall damage.", "In %." });
	public static final ItemStat PROJECTILE_DAMAGE_REDUCTION = new DoubleStat("PROJECTILE_DAMAGE_REDUCTION", new ItemStack(Material.IRON_CHESTPLATE),
			"Projectile Damage Reduction", new String[] { "Reduces projectile damage.", "In %." });
	public static final ItemStat PHYSICAL_DAMAGE_REDUCTION = new DoubleStat("PHYSICAL_DAMAGE_REDUCTION", new ItemStack(Material.LEATHER_CHESTPLATE),
			"Physical Damage Reduction", new String[] { "Reduces physical damage.", "In %." });
	public static final ItemStat FIRE_DAMAGE_REDUCTION = new DoubleStat("FIRE_DAMAGE_REDUCTION", new ItemStack(Material.BLAZE_POWDER),
			"Fire Damage Reduction", new String[] { "Reduces fire damage.", "In %." });
	public static final ItemStat MAGIC_DAMAGE_REDUCTION = new DoubleStat("MAGIC_DAMAGE_REDUCTION", new ItemStack(Material.POTION),
			"Magic Damage Reduction", new String[] { "Reduce magic damage dealt by potions.", "In %." });
	public static final ItemStat PVE_DAMAGE_REDUCTION = new DoubleStat("PVE_DAMAGE_REDUCTION", VersionMaterial.PORKCHOP.toItem(),
			"PvE Damage Reduction", new String[] { "Reduces damage dealt by mobs.", "In %." });
	public static final ItemStat PVP_DAMAGE_REDUCTION = new DoubleStat("PVP_DAMAGE_REDUCTION", VersionMaterial.SKELETON_SKULL.toItem(),
			"PvP Damage Reduction", new String[] { "Reduces damage dealt by players", "In %." });
	public static final ItemStat UNDEAD_DAMAGE = new DoubleStat("UNDEAD_DAMAGE", VersionMaterial.SKELETON_SKULL.toItem(), "Undead Damage",
			new String[] { "Deals additional damage to undead.", "In %." });
	public static final ItemStat UNBREAKABLE = new Unbreakable(), TIER = new ItemTierStat(), SET = new ItemSetStat(), ARMOR = new Armor(),
			ARMOR_TOUGHNESS = new ArmorToughness(), MAX_HEALTH = new MaxHealth();
	public static final ItemStat MAX_MANA = new DoubleStat("MAX_MANA", VersionMaterial.LAPIS_LAZULI.toItem(), "Max Mana",
			new String[] { "Adds mana to your max mana bar." });
	public static final ItemStat KNOCKBACK_RESISTANCE = new KnockbackResistance(), MOVEMENT_SPEED = new MovementSpeed();

	public static final ItemStat TWO_HANDED = new BooleanStat("TWO_HANDED", new ItemStack(Material.IRON_INGOT), "Two Handed",
			new String[] { "If set to true, a player will be", "significantly slower if holding two", "items, one being Two Handed." },
			new String[] { "piercing", "slashing", "blunt", "offhand", "range", "tool" });

	public static final ItemStat RESTORE = new Restore();
	public static final ItemStat RESTORE_MANA = new DoubleStat("RESTORE_MANA", VersionMaterial.LAPIS_LAZULI.toItem(), "Restore Mana",
			new String[] { "The amount of mana", "your consumable restores." }, new String[] { "consumable" });
	public static final ItemStat RESTORE_STAMINA = new DoubleStat("RESTORE_STAMINA", VersionMaterial.LIGHT_GRAY_DYE.toItem(), "Restore Stamina",
			new String[] { "The amount of stamina/power", "your consumable restores." }, new String[] { "consumable" });
	public static final ItemStat CAN_IDENTIFY = new BooleanStat("CAN_IDENTIFY", new ItemStack(Material.PAPER), "Can Identify?",
			new String[] { "Players can identify & make their", "item usable using this consumable." }, new String[] { "consumable" });
	public static final ItemStat CAN_DECONSTRUCT = new BooleanStat("CAN_DECONSTRUCT", new ItemStack(Material.PAPER), "Can Deconstruct?",
			new String[] { "Players can deconstruct their item", "using this consumable, creating", "another random item." },
			new String[] { "consumable" });
	public static final ItemStat EFFECTS = new Effects(), PERM_EFFECTS = new PermanentEffects();
	public static final ItemStat SOULBINDING_CHANCE = new DoubleStat("SOULBINDING_CHANCE", VersionMaterial.ENDER_EYE.toItem(), "Soulbinding Chance",
			new String[] { "Defines the chance your item has to", "link another item to your soul,", "preventing other players from using it." },
			new String[] { "consumable" });
	public static final ItemStat SOULBOUND_BREAK_CHANCE = new DoubleStat(
			"SOULBOUND_BREAK_CHANCE", VersionMaterial.ENDER_EYE.toItem(), "Soulbound Break Chance", new String[] { "The chance of breaking an item's",
					"soulbound when drag & drop'd on it.", "This chance is lowered depending", "on the soulbound's level." },
			new String[] { "consumable" });
	public static final ItemStat SOULBOUND_LEVEL = new SoulboundLevel();
	public static final ItemStat ITEM_COOLDOWN = new DoubleStat("ITEM_COOLDOWN", new ItemStack(Material.COOKED_CHICKEN), "Item Cooldown",
			new String[] { "This cooldown applies for consumables", "as well as for item commands." },
			new String[] { "!armor", "!gem_stone", "all" });
	public static final ItemStat VANILLA_EATING_ANIMATION = new VanillaEatingAnimation(), INEDIBLE = new Inedible(), GEM_COLOR = new GemColor(),
			ITEM_TYPE_RESTRICTION = new ItemTypeRestriction();
	public static final ItemStat MAX_CONSUME = new DoubleStat("MAX_CONSUME", new ItemStack(Material.BLAZE_POWDER), "Max Consume",
			new String[] { "Max amount of usage before", "item disappears." }, new String[] { "consumable" });

	public static final ItemStat SUCCESS_RATE = new SuccessRate();
	public static final ItemStat COMPATIBLE_TYPES = new CompatibleTypes();

	public static final ItemStat CRAFTING = new Crafting(), CRAFT_PERMISSION = new CraftingPermission();
	public static final ItemStat AUTOSMELT = new BooleanStat("AUTOSMELT", new ItemStack(Material.COAL), "Autosmelt",
			new String[] { "If set to true, your tool will", "automaticaly smelt mined ores." }, new String[] { "tool" });
	public static final ItemStat BOUNCING_CRACK = new BooleanStat("BOUNCING_CRACK", VersionMaterial.COBBLESTONE_WALL.toItem(), "Bouncing Crack",
			new String[] { "If set to true, your tool will", "also break nearby blocks." }, new String[] { "tool" });
	public static final ItemStat PICKAXE_POWER = new PickaxePower();
	public static final ItemStat CUSTOM_SOUNDS = new CustomSounds();
	public static final ItemStat ELEMENTS = new Elements();
	public static final ItemStat COMMANDS = new Commands(), STAFF_SPIRIT = new StaffSpiritStat(), LUTE_ATTACK_SOUND = new LuteAttackSoundStat(),
			LUTE_ATTACK_EFFECT = new LuteAttackEffectStat();
	public static final ItemStat NOTE_WEIGHT = new DoubleStat("NOTE_WEIGHT", VersionMaterial.MUSIC_DISC_MALL.toItem(), "Note Weight",
			new String[] { "Defines how the projectile cast", "by your lute tilts downwards." }, new String[] { "lute" });
	public static final ItemStat REMOVE_ON_CRAFT = new BooleanStat("REMOVE_ON_CRAFT", new ItemStack(Material.GLASS_BOTTLE), "Remove on Craft",
			new String[] { "If the item should be completely", "removed when used in a recipe,", "or if it should become an",
					"empty bottle or bucket." },
			new String[] { "all" }, Material.POTION, Material.SPLASH_POTION, Material.LINGERING_POTION, Material.MILK_BUCKET, Material.LAVA_BUCKET,
			Material.WATER_BUCKET);
	public static final ItemStat GEM_SOCKETS = new GemSockets();
	public static final ItemStat REPAIR = new DoubleStat("REPAIR", new ItemStack(Material.ANVIL), "Repair",
			new String[] { "The amount of durability your item", "can repair when set an item." }, new String[] { "consumable" });
	public static final ItemStat REPAIR_MATERIAL = new RepairMaterial();

	public static final ItemStat KNOCKBACK = new DoubleStat("KNOCKBACK", VersionMaterial.IRON_HORSE_ARMOR.toItem(), "Knockback",
			new String[] { "Using this musket will knock", "the user back if positive." }, new String[] { "musket" });
	public static final ItemStat RECOIL = new DoubleStat("RECOIL", VersionMaterial.IRON_HORSE_ARMOR.toItem(), "Recoil",
			new String[] { "Corresponds to the shooting innacuracy." }, new String[] { "musket" });

	public static final ItemStat ABILITIES = new Abilities(), UPGRADE = new UpgradeStat();
	public static final ItemStat SKULL_TEXTURE = new SkullTextureStat(), DYE_COLOR = new DyeColor(), POTION_EFFECTS = new PotionEffects(),
			POTION_COLOR = new PotionColor(), SHIELD_PATTERN = new ShieldPatternStat(), HIDE_POTION_EFFECTS = new HidePotionEffects();

	/*
	 * internal stats
	 */
	public static final Soulbound SOULBOUND = new Soulbound();
	public static final ItemStat STORED_TAGS = new StoredTags();

	private final String id, name;
	private final ItemStack item;

	private final String[] lore, compatibleTypes;
	private final List<Material> compatibleMaterials;

	/*
	 * the stat can be enabled or not, depending on the server version to
	 * prevent from displaying useless editable stats in the edition menu.
	 */
	private boolean enabled = true;

	public ItemStat(String id, ItemStack item, String name, String[] lore, String[] types, Material... materials) {
		this.id = id;
		this.item = item;
		this.lore = lore == null ? new String[0] : lore;
		this.compatibleTypes = types == null ? new String[0] : types;
		this.name = name;
		this.compatibleMaterials = Arrays.asList(materials);
	}

	/*
	 * reads stat data from a configuration section and applies it to the item
	 * stack after having generated the corresponding stat data class instance
	 */
	public abstract StatData whenInitialized(Object object);

	/*
	 * any item stat which can be used in the item generator. this method reads
	 * from a config file stat data which is cached to later generate a random
	 * item
	 */
	public abstract RandomStatData whenInitializedGeneration(Object object);

	/*
	 * applies a stat onto an mmoitem builder instance
	 */
	public abstract void whenApplied(MMOItemBuilder item, StatData data);

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
	// TODO change it to List<String> lore, MMOItem mmoitem, makes no sense to
	// check config.
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

	public String getPath() {
		return id.toLowerCase().replace("_", "-");
	}

	public String getNBTPath() {
		return "MMOITEMS_" + id;
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

	public boolean hasValidMaterial(ItemStack item) {
		return compatibleMaterials.size() == 0 || compatibleMaterials.contains(item.getType());
	}

	public void disable() {
		enabled = false;
	}

	public String format(double value, String... replace) {
		String format = translate().replace("<plus>", value > 0 ? "+" : "");
		for (int j = 0; j < replace.length; j += 2)
			format = format.replace(replace[j], replace[j + 1]);
		return format;
	}

	public String translate() {
		String str = MMOItems.plugin.getLanguage().getStatFormat(getPath());
		return str == null ? "<TranslationNotFound:" + getPath() + ">" : str;
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