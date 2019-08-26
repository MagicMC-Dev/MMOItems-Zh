package net.Indyuce.mmoitems.api;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.MMOItems;

public class AdvancedRecipe {

	/*
	 * must not save the item as an itemStack otherwise the item will be the
	 * same, random stats will be useless
	 */
	private Type type;
	private String id;

	/*
	 * only saves once the itemstack for a preview, so spamming doesn't decrease
	 * performance generating the item. once the item is clicked, a new item
	 * will be generated and will replace the clicked one
	 */
	private ItemStack preview;

	/*
	 * amounts of ingredients that are needed. the amount data is NOT stored in
	 * the key of the advanced recipes map. the key here represents the item
	 * format (not recipe)
	 */
	private Map<Integer, Integer> amountData = new HashMap<>();

	// permission needed for the recipe
	private String permission = "";

	// parsed recipe format
	private String parsed;

	public AdvancedRecipe(Type type, String id) {
		this.type = type;
		this.id = id;
	}

	public Type getItemType() {
		return type;
	}

	public String getItemId() {
		return id;
	}

	public ItemStack getPreviewItem() {
		return preview;
	}

	public ItemStack generateNewItem() {
		return MMOItems.plugin.getItems().getItem(type, id);
	}

	public void setPreviewItem(ItemStack value) {
		preview = value;
	}

	public int getAmount(int index) {
		return amountData.get(index);
	}

	public boolean isParsed(String parsed) {
		return parsed.equals(this.parsed);
	}

	public void setParsed(String value) {
		parsed = value;
	}

	public String getParsed() {
		return parsed;
	}

	public boolean hasPermission(Player player) {
		return permission == null || permission.equals("") || player.hasPermission(permission);
	}

	public void setPermission(String value) {
		permission = value;
	}

	public void setAmount(int slot, int amount) {
		amountData.put(slot, amount);
	}
}
