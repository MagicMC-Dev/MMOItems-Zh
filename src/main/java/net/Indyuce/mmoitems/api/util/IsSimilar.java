package net.Indyuce.mmoitems.api.util;

import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.version.VersionMaterial;

public class IsSimilar {
	public static boolean check(ItemStack i1, ItemStack i2) {
		if(i1.getType() == VersionMaterial.PLAYER_HEAD.toMaterial()) {
			System.out.println("IS SIMILAR: head");
			return i1.toString().equals(i2.toString());
		}
		
		System.out.println("IS SIMILAR: non head");
		return i1.isSimilar(i2);
	}
}
