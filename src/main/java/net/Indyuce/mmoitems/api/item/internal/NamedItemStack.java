package net.Indyuce.mmoitems.api.item.internal;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.mmogroup.mmolib.MMOLib;

public class NamedItemStack extends ItemStack {
	public NamedItemStack(Material material, String name) {
		super(material);

		ItemMeta meta = getItemMeta();
		meta.setDisplayName(MMOLib.plugin.parseColors(name));
		setItemMeta(meta);
	}
}
