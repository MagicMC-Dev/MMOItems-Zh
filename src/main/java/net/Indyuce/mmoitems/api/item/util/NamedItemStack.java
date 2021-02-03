package net.Indyuce.mmoitems.api.item.util;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import io.lumine.mythic.lib.MythicLib;

public class NamedItemStack extends ItemStack {
	public NamedItemStack(Material material, String name) {
		super(material);

		ItemMeta meta = getItemMeta();
		meta.setDisplayName(MythicLib.plugin.parseColors(name));
		setItemMeta(meta);
	}
}
