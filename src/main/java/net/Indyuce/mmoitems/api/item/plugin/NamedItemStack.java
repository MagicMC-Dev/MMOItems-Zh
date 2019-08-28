package net.Indyuce.mmoitems.api.item.plugin;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class NamedItemStack extends ItemStack {
	public NamedItemStack(Material material, String name) {
		super(material);

		ItemMeta meta = getItemMeta();
		meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
		setItemMeta(meta);
	}
}
