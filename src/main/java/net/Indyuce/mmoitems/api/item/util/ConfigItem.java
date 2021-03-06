package net.Indyuce.mmoitems.api.item.util;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.item.ItemTag;
import net.Indyuce.mmoitems.MMOUtils;
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
	private final ItemStack icon;

	// updated when the plugin reloads
	private String name;
	private List<String> lore;

	// generated
	private ItemStack item;

	public ConfigItem(String id, Material material) {
		this(id, material, null);
	}

	public ConfigItem(String id, Material material, String name, String... lore) {
		Validate.notNull(id, "ID cannot be null");
		Validate.notNull(material, "Material cannot be null");

		this.id = id;
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
		if (icon.getType() == Material.AIR)
			return;

		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(MythicLib.plugin.parseColors(getName()));
		meta.addItemFlags(ItemFlag.values());

		if (hasLore()) {
			List<String> lore = new ArrayList<>();
			getLore().forEach(str -> lore.add(ChatColor.GRAY + MythicLib.plugin.parseColors(str)));
			meta.setLore(lore);
		}

		item.setItemMeta(meta);
		item = MythicLib.plugin.getVersion().getWrapper().getNBTItem(item).addTag(new ItemTag("ItemId", id)).toItem();
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
