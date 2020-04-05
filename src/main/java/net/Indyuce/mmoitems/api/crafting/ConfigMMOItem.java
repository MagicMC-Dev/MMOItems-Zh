package net.Indyuce.mmoitems.api.crafting;

import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.item.MMOItem;

public class ConfigMMOItem {
	private final MMOItem mmoitem;
	private final int amount;

	private ItemStack preview;

	public ConfigMMOItem(ConfigurationSection config) {
		Validate.notNull(config, "Could not read MMOItem config");
		
		String typeFormat = config.getString("type"), id = config.getString("id");
		Validate.notNull(typeFormat, "Type format must not be null");
		Validate.notNull(id, "ID must not be null");

		Type type = MMOItems.plugin.getTypes().get(format(config.getString("type")));
		Validate.notNull(type, typeFormat + " does not correspond to any item type.");

		Validate.notNull(mmoitem = MMOItems.plugin.getItems().getMMOItem(type, id), "Could not load MMOItem");
		this.amount = Math.max(1, config.getInt("amount"));
	}

	public ConfigMMOItem(MMOItem mmoitem, int amount) {
		Validate.notNull(mmoitem, "Could not register recipe output");

		this.mmoitem = mmoitem;
		this.amount = Math.max(1, amount);
	}

	public ItemStack generate() {
		ItemStack item = mmoitem.newBuilder().build();
		item.setAmount(amount);
		return item;
	}

	public Type getType() {
		return mmoitem.getType();
	}

	public String getId() {
		return mmoitem.getId();
	}

	/*
	 * reduce startup calculations so that item is calculated the first time it
	 * needs to be displayed
	 */
	public ItemStack getPreview() {
		return preview == null ? (preview = mmoitem.newBuilder().build()).clone() : preview.clone();
	}

	public int getAmount() {
		return amount;
	}

	private static String format(String str) {
		return str.toUpperCase().replace("-", "_").replace(" ", "_");
	}
}
