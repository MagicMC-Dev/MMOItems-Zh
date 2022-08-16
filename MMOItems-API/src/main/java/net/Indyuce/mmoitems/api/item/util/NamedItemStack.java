package net.Indyuce.mmoitems.api.item.util;

import io.lumine.mythic.lib.MythicLib;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

// TODO: 2/13/2021 Remove this eventually.
public class NamedItemStack extends ItemStack {
	public NamedItemStack(Material material, String name) {
		super(material);

		ItemMeta meta = getItemMeta();
		meta.setDisplayName(MythicLib.plugin.parseColors(name));
		setItemMeta(meta);
	}
}
