package net.Indyuce.mmoitems.api.item.plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.Indyuce.mmoitems.api.item.plugin.crafting.CraftingRecipeDisplay;
import net.Indyuce.mmoitems.api.item.plugin.crafting.QueueItemDisplay;
import net.Indyuce.mmoitems.api.item.plugin.crafting.UpgradingRecipeDisplay;
import net.Indyuce.mmoitems.version.VersionMaterial;

public class ConfigItem {
	private final String id;
	private final Material material;

	private String name;
	private List<String> lore;

	// generated
	private ItemStack item;

	public static final ConfigItem CONFIRM = new ConfigItem("CONFIRM", VersionMaterial.GREEN_STAINED_GLASS_PANE.toMaterial(), "&aConfirm");
	public static final ConfigItem NO_ITEM = new ConfigItem("NO_ITEM", VersionMaterial.GRAY_STAINED_GLASS_PANE.toMaterial(), "&c- No Item -");
	public static final ConfigItem NO_TYPE = new ConfigItem("NO_TYPE", VersionMaterial.GRAY_STAINED_GLASS_PANE.toMaterial(), "&c- No Type -");
	public static final ConfigItem FILL = new ConfigItem("FILL", VersionMaterial.GRAY_STAINED_GLASS_PANE.toMaterial(), "&8");
	public static final ConfigItem TYPE_DISPLAY = new ConfigItem("TYPE_DISPLAY", Material.BARRIER, "&a#type# &8(Click to browse)", "&7There are &6#recipes#&7 available recipes.");
	public static final CustomSkull PREVIOUS_PAGE = new CustomSkull("PREVIOUS_PAGE", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmQ2OWUwNmU1ZGFkZmQ4NGU1ZjNkMWMyMTA2M2YyNTUzYjJmYTk0NWVlMWQ0ZDcxNTJmZGM1NDI1YmMxMmE5In19fQ==", "&aPrevious Page");
	public static final CustomSkull NEXT_PAGE = new CustomSkull("NEXT_PAGE", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTliZjMyOTJlMTI2YTEwNWI1NGViYTcxM2FhMWIxNTJkNTQxYTFkODkzODgyOWM1NjM2NGQxNzhlZDIyYmYifX19", "&aNext Page");
	public static final CustomSkull PREVIOUS_IN_QUEUE = new CustomSkull("PREVIOUS_IN_QUEUE", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmQ2OWUwNmU1ZGFkZmQ4NGU1ZjNkMWMyMTA2M2YyNTUzYjJmYTk0NWVlMWQ0ZDcxNTJmZGM1NDI1YmMxMmE5In19fQ==", "&aPrevious");
	public static final CustomSkull NEXT_IN_QUEUE = new CustomSkull("NEXT_IN_QUEUE", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTliZjMyOTJlMTI2YTEwNWI1NGViYTcxM2FhMWIxNTJkNTQxYTFkODkzODgyOWM1NjM2NGQxNzhlZDIyYmYifX19", "&aNext");
	public static final CustomSkull BACK = new CustomSkull("BACK", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmQ2OWUwNmU1ZGFkZmQ4NGU1ZjNkMWMyMTA2M2YyNTUzYjJmYTk0NWVlMWQ0ZDcxNTJmZGM1NDI1YmMxMmE5In19fQ==", "&aBack");
	public static final CustomSkull RECIPE_LIST = new CustomSkull("RECIPE_LIST", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjMzNTk4NDM3ZTMxMzMyOWViMTQxYTEzZTkyZDliMDM0OWFhYmU1YzY0ODJhNWRkZTdiNzM3NTM2MzRhYmEifX19==", "&aAdvanced Recipes");
	public static final CraftingRecipeDisplay CRAFTING_RECIPE_DISPLAY = new CraftingRecipeDisplay();
	public static final UpgradingRecipeDisplay UPGRADING_RECIPE_DISPLAY = new UpgradingRecipeDisplay();
	public static final QueueItemDisplay QUEUE_ITEM_DISPLAY = new QueueItemDisplay();
	
	public static final ConfigItem[] values = { CONFIRM, FILL, NO_ITEM, NO_TYPE, TYPE_DISPLAY, PREVIOUS_PAGE, NEXT_PAGE, PREVIOUS_IN_QUEUE, NEXT_IN_QUEUE, BACK, RECIPE_LIST, CRAFTING_RECIPE_DISPLAY, UPGRADING_RECIPE_DISPLAY, QUEUE_ITEM_DISPLAY };

	public ConfigItem(String id, Material material) {
		this(id, material, null);
	}

	public ConfigItem(String id, Material material, String name, String... lore) {
		Validate.notNull(id, "ID cannot be null");
		Validate.notNull(material, "Material cannot be null");

		this.id = id;
		this.material = material;
		this.name = name;
		this.lore = Arrays.asList(lore);
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

		setName(config.contains("name") ? config.getString("name") : "");
		setLore(config.contains("lore") ? config.getStringList("lore") : new ArrayList<>());
		updateItem();
	}

	public void updateItem() {
		setItem(new ItemStack(material));
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', getName()));
		meta.addItemFlags(ItemFlag.values());

		if (hasLore()) {
			List<String> lore = new ArrayList<>();
			getLore().forEach(str -> lore.add(ChatColor.GRAY + ChatColor.translateAlternateColorCodes('&', str)));
			meta.setLore(lore);
		}

		item.setItemMeta(meta);
		// item = MMOItems.plugin.getNMS().getNBTItem(item).addTag(new
		// ItemTag("itemId", id)).toItem();
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
