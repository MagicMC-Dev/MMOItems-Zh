package net.Indyuce.mmoitems.api;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.item.util.identify.UnidentifiedItem;
import net.Indyuce.mmoitems.manager.TypeManager;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.mmogroup.mmolib.MMOLib;

@SuppressWarnings("unused")
public class Type {

	// slashing
	public static final Type SWORD = new Type(TypeSet.SLASHING, "SWORD", true, true, true, EquipmentSlot.MAIN_HAND);

	// piercing
	public static final Type DAGGER = new Type(TypeSet.PIERCING, "DAGGER", true, true, true, EquipmentSlot.MAIN_HAND);
	public static final Type SPEAR = new Type(TypeSet.PIERCING, "SPEAR", true, true, true, EquipmentSlot.MAIN_HAND);

	// blunt
	public static final Type HAMMER = new Type(TypeSet.BLUNT, "HAMMER", true, true, true, EquipmentSlot.MAIN_HAND);
	public static final Type GAUNTLET = new Type(TypeSet.BLUNT, "GAUNTLET", true, true, true, EquipmentSlot.MAIN_HAND);

	// range
	public static final Type WHIP = new Type(TypeSet.RANGE, "WHIP", true, false, true, EquipmentSlot.MAIN_HAND);
	public static final Type STAFF = new Type(TypeSet.RANGE, "STAFF", true, false, true, EquipmentSlot.MAIN_HAND);
	public static final Type BOW = new Type(TypeSet.RANGE, "BOW", true, false, false, EquipmentSlot.BOTH_HANDS);
	public static final Type CROSSBOW = new Type(TypeSet.RANGE, "CROSSBOW", false, true, false, EquipmentSlot.BOTH_HANDS);
	public static final Type MUSKET = new Type(TypeSet.RANGE, "MUSKET", true, false, false, EquipmentSlot.BOTH_HANDS);
	public static final Type LUTE = new Type(TypeSet.RANGE, "LUTE", true, false, true, EquipmentSlot.BOTH_HANDS);

	// offhand
	public static final Type CATALYST = new Type(TypeSet.OFFHAND, "CATALYST", false, false, true, EquipmentSlot.BOTH_HANDS);
	public static final Type OFF_CATALYST = new Type(TypeSet.OFFHAND, "OFF_CATALYST", false, false, true, EquipmentSlot.OFF_HAND);

	// any
	public static final Type ORNAMENT = new Type(TypeSet.EXTRA, "ORNAMENT", false, false, true, EquipmentSlot.ANY);

	// extra
	public static final Type ARMOR = new Type(TypeSet.EXTRA, "ARMOR", false, false, true, EquipmentSlot.ARMOR);
	public static final Type TOOL = new Type(TypeSet.EXTRA, "TOOL", false, false, true, EquipmentSlot.MAIN_HAND);
	public static final Type CONSUMABLE = new Type(TypeSet.EXTRA, "CONSUMABLE", false, false, true, EquipmentSlot.MAIN_HAND);
	public static final Type MISCELLANEOUS = new Type(TypeSet.EXTRA, "MISCELLANEOUS", false, false, true, EquipmentSlot.MAIN_HAND);
	public static final Type GEM_STONE = new Type(TypeSet.EXTRA, "GEM_STONE", false, false, true, EquipmentSlot.OTHER);
	public static final Type SKIN = new Type(TypeSet.EXTRA, "SKIN", false, false, true, EquipmentSlot.OTHER);
	public static final Type ACCESSORY = new Type(TypeSet.EXTRA, "ACCESSORY", false, false, true, EquipmentSlot.ACCESSORY);
	public static final Type BLOCK = new Type(TypeSet.EXTRA, "BLOCK", false, false, false, EquipmentSlot.OTHER);

	private final String id;
	private String name;
	private final TypeSet set;

	/*
	 * the 'weapon' boolean is used for item type restrictions for gem stones to
	 * easily check if the item is a weapon. 'melee' defines if the item can
	 * actually be used during a melee entity attack, if it can't it usually has
	 * some unique attack effect. 'rightClickSpecial' defines what items need to
	 * be right/left clicked to cast a right-click ability. e.g bows need to be
	 * left clicked since the right click is used to fire arrows.
	 */
	private final boolean weapon, melee, rightClickSpecial;
	private final EquipmentSlot equipType;

	/*
	 * used to display the item in the item explorer and in the item recipes
	 * list in the advanced workbench. can also be edited using the config
	 * files.
	 */
	private ItemStack item;

	/*
	 * any type can have a subtype which basically dictates what the item type
	 * does.
	 */
	private Type parent;

	private UnidentifiedItem unidentifiedTemplate;

	/*
	 * list of stats which can be applied onto an item which has this type. This
	 * improves performance when generating an item by a significant amount.
	 */
	private final List<ItemStat> available = new ArrayList<>();

	public Type(TypeSet set, String id, boolean weapon, boolean melee, boolean rightClickSpecial, EquipmentSlot equipType) {
		this.set = set;
		this.id = id.toUpperCase().replace("-", "_").replace(" ", "_");
		this.equipType = equipType;

		this.weapon = weapon;
		this.melee = melee;
		this.rightClickSpecial = rightClickSpecial;
	}

	public Type(TypeManager manager, ConfigurationSection config) {
		id = config.getName().toUpperCase().replace("-", "_").replace(" ", "_");

		parent = manager.get(config.getString("parent").toUpperCase().replace("-", "_").replace(" ", "_"));
		set = parent.set;
		weapon = parent.weapon;
		melee = parent.melee;
		rightClickSpecial = parent.rightClickSpecial;
		equipType = parent.equipType;
	}

	public void load(ConfigurationSection config) {
		Validate.notNull(config, "Could not find config for " + getId());

		name = config.getString("name");
		Validate.notNull(name, "Could not read name");

		item = read(config.getString("display"));
		Validate.notNull(item, "Could not read item");

		(unidentifiedTemplate = new UnidentifiedItem(this)).update(config.getConfigurationSection("unident-item"));
	}

	@Deprecated
	public String name() {
		return id;
	}

	public String getId() {
		return id;
	}

	public TypeSet getItemSet() {
		return set;
	}

	public boolean isWeapon() {
		return weapon;
	}

	public boolean isMeleeWeapon() {
		return melee;
	}

	public boolean isSpecialActionOnRightClick() {
		return rightClickSpecial;
	}

	public String getName() {
		return name;
	}

	public EquipmentSlot getEquipmentType() {
		return equipType;
	}

	public ItemStack getItem() {
		return item.clone();
	}

	public boolean isSubtype() {
		return parent != null;
	}

	public Type getParent() {
		return parent;
	}

	public boolean corresponds(Type type) {
		return equals(type) || (isSubtype() && getParent().equals(type));
	}

	public boolean corresponds(TypeSet set) {
		return getItemSet() == set;
	}

	/**
	 * @return The collection of all stats which can be applied onto this
	 *         specific item type. This list is cached when types are being
	 *         loaded and is a PRETTY GOOD performance improvement.
	 */
	public List<ItemStat> getAvailableStats() {
		return available;
	}

	/**
	 * @return Finds the /item config file corresponding to the item type and
	 *         loads it
	 */
	public ConfigFile getConfigFile() {
		return new ConfigFile("/item", getId().toLowerCase());
	}

	public UnidentifiedItem getUnidentifiedTemplate() {
		return unidentifiedTemplate;
	}

	/**
	 * @param  stat The stat to check
	 * @return      If the stat can be handled by this type of item
	 */
	@Deprecated
	public boolean canHave(ItemStat stat) {
		return stat.isCompatible(this);
	}

	@Override
	public boolean equals(Object object) {
		return object instanceof Type && ((Type) object).id.equals(id);
	}

	private ItemStack read(String str) {
		Validate.notNull(str, "Input must not be null");

		String[] split = str.split(":");
		Material material = Material.valueOf(split[0]);
		return split.length > 1 ? MMOLib.plugin.getVersion().getWrapper().textureItem(material, Integer.parseInt(split[1])) : new ItemStack(material);
	}

	/**
	 * 
	 * @param      item The item to retrieve the type from
	 * @return          The type of the item.
	 * @deprecated      Really heavy method because it instantiates an NBTItem
	 *                  (reads through all the item NBT data), looks for the
	 *                  type tag and does a type map lookup. Use
	 *                  NBTItem#get(ItemStack) first and then NBTItem#getType()
	 */
	@Deprecated
	public static Type get(ItemStack item) {
		return Type.get(MMOLib.plugin.getVersion().getWrapper().getNBTItem(item).getType());
	}

	/**
	 * Used in command executors and completions for easier manipulation
	 * 
	 * @param  id The type id
	 * @return    The type or null if it couldn't be found
	 */
	public static @Nullable Type get(String id) {
		if (id == null)
			return null;

		String format = id.toUpperCase().replace("-", "_").replace(" ", "_");
		return MMOItems.plugin.getTypes().has(format) ? MMOItems.plugin.getTypes().get(format) : null;
	}

	/**
	 * Used in command executors and completions for easier manipulation
	 * 
	 * @param  id The type id
	 * @return    If a registered type with this ID could be found
	 */
	public static boolean isValid(String id) {
		return id != null && MMOItems.plugin.getTypes().has(id.toUpperCase().replace("-", "_").replace(" ", "_"));
	}

	public enum EquipmentSlot {

		/*
		 * can only apply stats in armor
		 */
		ARMOR,

		/*
		 * can't apply stats in vanilla slots
		 */
		ACCESSORY,

		/*
		 * cannot apply its stats anywhere
		 */
		OTHER,

		/*
		 * always apply its stats. may only be used by EquippedItems, and not
		 * Types since default types do not use it and extra types keep their
		 * parent equipment slot
		 */
		ANY,

		MAIN_HAND,
		OFF_HAND,
		BOTH_HANDS;

		public boolean isHand() {
			return this == MAIN_HAND || this == OFF_HAND || this == BOTH_HANDS;
		}
	}
}
