package net.Indyuce.mmoitems.stat;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.api.item.MMOItem;
import net.Indyuce.mmoitems.api.item.NBTItem;
import net.Indyuce.mmoitems.api.item.build.MMOItemBuilder;
import net.Indyuce.mmoitems.stat.data.StatData;
import net.Indyuce.mmoitems.stat.type.StringStat;
import net.Indyuce.mmoitems.version.VersionMaterial;

public class Advanced_Crafting_Recipe_Permission extends StringStat {
	public Advanced_Crafting_Recipe_Permission() {
		super(new ItemStack(VersionMaterial.OAK_SIGN.toMaterial()), "Advanced Recipe Permission", new String[] { "The permission needed to craft this item.", "Changing this value requires &o/mi reload adv-recipes&7." }, "advanced-craft-permission", new String[] { "all" });
	}

	@Override
	public boolean whenLoaded(MMOItem item, ConfigurationSection config) {
		return true;
	}

	@Override
	public void whenLoaded(MMOItem mmoitem, NBTItem item) {
	}

	@Override
	public boolean whenApplied(MMOItemBuilder item, StatData data) {
		return true;
	}
}