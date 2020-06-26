package net.Indyuce.mmoitems.api.item.plugin;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.asangarin.hexcolors.ColorParse;

public class NamedItemStack extends ItemStack {
	public NamedItemStack(Material material, String name) {
		super(material);

		ItemMeta meta = getItemMeta();
		meta.setDisplayName(new ColorParse('&', name).toChatColor());
		setItemMeta(meta);
	}
}
