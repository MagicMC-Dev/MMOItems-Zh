package net.Indyuce.mmoitems.api.item.util;

import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.item.util.crafting.CraftingRecipeDisplay;
import net.Indyuce.mmoitems.api.item.util.crafting.QueueItemDisplay;
import net.Indyuce.mmoitems.api.item.util.crafting.UpgradingRecipeDisplay;
import net.mmogroup.mmolib.MMOLib;
import net.mmogroup.mmolib.api.item.ItemTag;
import net.mmogroup.mmolib.version.VersionMaterial;
import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ConfigItem {
	private final String id;
	private final Material material;
	private final ItemStack icon;

	// updated when the plugin reloads
	private String name;
	private List<String> lore;

	// generated
	private ItemStack item;

	public static final ConfigItem CONFIRM = new ConfigItem("CONFIRM", VersionMaterial.GREEN_STAINED_GLASS_PANE.toMaterial(), "&aConfirm");
	public static final ConfigItem FILL = new ConfigItem("FILL", VersionMaterial.GRAY_STAINED_GLASS_PANE.toMaterial(), "&8");
	public static final CustomSkull PREVIOUS_PAGE = new CustomSkull("PREVIOUS_PAGE", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmQ2OWUwNmU1ZGFkZmQ4NGU1ZjNkMWMyMTA2M2YyNTUzYjJmYTk0NWVlMWQ0ZDcxNTJmZGM1NDI1YmMxMmE5In19fQ==", "&aPrevious Page");
	public static final CustomSkull NEXT_PAGE = new CustomSkull("NEXT_PAGE", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTliZjMyOTJlMTI2YTEwNWI1NGViYTcxM2FhMWIxNTJkNTQxYTFkODkzODgyOWM1NjM2NGQxNzhlZDIyYmYifX19", "&aNext Page");
	public static final CustomSkull PREVIOUS_IN_QUEUE = new CustomSkull("PREVIOUS_IN_QUEUE", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmQ2OWUwNmU1ZGFkZmQ4NGU1ZjNkMWMyMTA2M2YyNTUzYjJmYTk0NWVlMWQ0ZDcxNTJmZGM1NDI1YmMxMmE5In19fQ==", "&aPrevious");
	public static final CustomSkull NEXT_IN_QUEUE = new CustomSkull("NEXT_IN_QUEUE", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTliZjMyOTJlMTI2YTEwNWI1NGViYTcxM2FhMWIxNTJkNTQxYTFkODkzODgyOWM1NjM2NGQxNzhlZDIyYmYifX19", "&aNext");
	public static final CustomSkull BACK = new CustomSkull("BACK", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmQ2OWUwNmU1ZGFkZmQ4NGU1ZjNkMWMyMTA2M2YyNTUzYjJmYTk0NWVlMWQ0ZDcxNTJmZGM1NDI1YmMxMmE5In19fQ==", "&aBack");
	public static final CraftingRecipeDisplay CRAFTING_RECIPE_DISPLAY = new CraftingRecipeDisplay();
	public static final UpgradingRecipeDisplay UPGRADING_RECIPE_DISPLAY = new UpgradingRecipeDisplay();
	public static final QueueItemDisplay QUEUE_ITEM_DISPLAY = new QueueItemDisplay();

	public static final ConfigItem[] values = { CONFIRM, FILL, PREVIOUS_PAGE, NEXT_PAGE, PREVIOUS_IN_QUEUE, NEXT_IN_QUEUE, BACK, CRAFTING_RECIPE_DISPLAY, UPGRADING_RECIPE_DISPLAY, QUEUE_ITEM_DISPLAY };

	public ConfigItem(String id, Material material) {
		this(id, material, null);
	}

	public ConfigItem(String id, Material material, String name, String... lore) {
		Validate.notNull(id, "ID cannot be null");
		Validate.notNull(material, "Material cannot be null");

		this.id = id;
		this.material = material;
		this.icon = new ItemStack(material);
		this.name = name;
		this.lore = Arrays.asList(lore);
	}

	/*
	 * used as util to load an item stack from a config
	 */
	public ConfigItem(ConfigurationSection config) {
		Validate.notNull(config, "Config cannot be null");
		id = config.getName();

		Validate.isTrue(config.contains("material"), "Could not find material");

		icon = MMOUtils.readIcon(config.getString("material"));

		material = icon.getType();
		name = config.getString("name", "");
		lore = config.getStringList("lore");

		updateItem();
	}

	public String getId() {
		return id;
	}

	public void setup(ConfigurationSection config) {
		config.set("name", getName());
		config.set("lore", getLore());
	}

	public void update(ConfigurationSection config) {
		Validate.notNull(config, "Config cannot be null");

		setName(config.getString("name", ""));
		setLore(config.contains("lore") ? config.getStringList("lore") : new ArrayList<>());
		updateItem();
	}

	public void updateItem() {
		setItem(icon);
		if (material == Material.AIR)
			return;

		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(MMOLib.plugin.parseColors(getName()));
		meta.addItemFlags(ItemFlag.values());

		if (hasLore()) {
			List<String> lore = new ArrayList<>();
			getLore().forEach(str -> lore.add(ChatColor.GRAY + MMOLib.plugin.parseColors(str)));
			meta.setLore(lore);
		}

		item.setItemMeta(meta);
		item = MMOLib.plugin.getVersion().getWrapper().getNBTItem(item).addTag(new ItemTag("ItemId", id)).toItem();
	}

	public Material getMaterial() {
		return material;
	}

	public String getName() {
		return name;
	}

	public List<String> getLore() {
		return lore;
	}

	public boolean hasLore() {
		return lore != null;
	}

	public ItemStack getItem() {
		return item;
	}

	public ItemStack getNewItem() {
		return item.clone();
	}

	protected void setName(String name) {
		this.name = name;
	}

	protected void setLore(List<String> lore) {
		this.lore = lore;
	}

	protected void setItem(ItemStack item) {
		this.item = item;
	}
}
