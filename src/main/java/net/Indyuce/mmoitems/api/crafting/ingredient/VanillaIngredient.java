package net.Indyuce.mmoitems.api.crafting.ingredient;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.player.RPGPlayer;
import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.MMOLineConfig;

public class VanillaIngredient extends Ingredient {
	private final Material material;

	/**
	 * displayName is the itemMeta display name; display corresponds to how the
	 * ingredient displays in the crafting recipe GUI item lore
	 */
	private final String displayName, display;

	public VanillaIngredient(MMOLineConfig config) {
		super("vanilla", config);

		config.validate("type");

		material = Material.valueOf(config.getString("type").toUpperCase().replace("-", "_").replace(" ", "_"));
		displayName = config.contains("name") ? MythicLib.plugin.parseColors(config.getString("name")) : null;

		display = config.contains("display") ? config.getString("display") : MMOUtils.caseOnWords(material.name().toLowerCase().replace("_", " "));
	}

	@Override
	public String getKey() {
		return "vanilla:" + material.name().toLowerCase() + "_" + displayName;
	}

	@Override
	public String formatDisplay(String string) {
		return string.replace("#item#", display).replace("#amount#", "" + getAmount());
	}

	@Override
	public ItemStack generateItemStack(RPGPlayer player) {
		ItemStack item = new ItemStack(material, getAmount());
		if (displayName != null) {
			ItemMeta meta = item.getItemMeta();
			meta.setDisplayName(displayName);
			item.setItemMeta(meta);
		}
		return item;
	}
}
