package net.Indyuce.mmoitems.api.util;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.mmogroup.mmolib.version.VersionMaterial;

public class IsSimilar {
	public static boolean check(ItemStack i1, ItemStack i2) {
		/**
		 * Not the most optimal code,
		 * but it works for now.
		 */
		if(i1.getType() == VersionMaterial.PLAYER_HEAD.toMaterial() &&
			i2.getType() == VersionMaterial.PLAYER_HEAD.toMaterial()) {
			ItemMeta meta1 = i1.getItemMeta();
			ItemMeta meta2 = i2.getItemMeta();
			
			if(meta1.hasDisplayName() && meta2.hasDisplayName())
				return meta1.getDisplayName().equalsIgnoreCase(meta2.getDisplayName());
		}

		return i1.isSimilar(i2);
	}
}
