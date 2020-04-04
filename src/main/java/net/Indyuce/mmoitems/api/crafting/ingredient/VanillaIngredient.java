package net.Indyuce.mmoitems.api.crafting.ingredient;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.Indyuce.mmoitems.MMOUtils;
import net.mmogroup.mmolib.api.MMOLineConfig;

public class VanillaIngredient extends Ingredient {
	private final Material material;

	/*
	 * display display is the item's meta display display, display corresponds
	 * to how the ingredient displays in the crafting recipe GUI item lores
	 */
	private final String displayName, display;

	public VanillaIngredient(MMOLineConfig config) {
		super("vanilla", config);

		config.validate("type");

		material = Material.valueOf(config.getString("type").toUpperCase().replace("-", "_").replace(" ", "_"));
		displayName = config.contains("name") ? ChatColor.translateAlternateColorCodes('&', config.getString("name")) : null;

		display = config.contains("display") ? config.getString("display") : MMOUtils.caseOnWords(material.name().toLowerCase().replace("_", " "));
	}

	@Override
	public String getKey() {
		return "vanilla:" + material.name().toLowerCase() + "_" + displayName;
	}

	@Override
	public String formatLoreDisplay(String string) {
		return string.replace("#item#", display).replace("#amount#", "" + getAmount());
	}

	@Override
	public ItemStack generateItemStack() {
		ItemStack item = new ItemStack(material, getAmount());
		if (displayName != null) {
			ItemMeta meta = item.getItemMeta();
			meta.setDisplayName(displayName);
			item.setItemMeta(meta);
		}
		return item;
	}
}
