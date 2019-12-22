package net.Indyuce.mmoitems.api.util;

import org.bukkit.inventory.ItemStack;

import net.mmogroup.mmolib.version.VersionMaterial;

public class IsSimilar {
	public static boolean check(ItemStack i1, ItemStack i2) {
		if(i1.getType() == VersionMaterial.PLAYER_HEAD.toMaterial()) {
			return i1.toString().equals(i2.toString());
		}

		return i1.isSimilar(i2);
	}
}
