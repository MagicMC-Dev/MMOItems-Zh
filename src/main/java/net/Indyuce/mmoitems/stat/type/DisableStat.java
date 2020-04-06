package net.Indyuce.mmoitems.stat.type;

import org.bukkit.Material;
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.Validate;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.api.item.MMOItem;
import net.Indyuce.mmoitems.api.item.build.MMOItemBuilder;
import net.Indyuce.mmoitems.stat.data.StatData;
import net.mmogroup.mmolib.api.item.ItemTag;

public class DisableStat extends BooleanStat {
	public DisableStat(String id, Material material, String name, String... lore) {
		super("DISABLE_" + id, new ItemStack(material), name, lore, new String[] { "all" });
	}

	public DisableStat(String id, Material material, String name, Material[] materials, String... lore) {
		super("DISABLE_" + id, new ItemStack(material), name, lore, new String[] { "all" }, materials);
	}

	public DisableStat(String id, Material material, String name, String[] types, String... lore) {
		super("DISABLE_" + id, new ItemStack(material), name, lore, types);
	}

	@Override
	public StatData whenInitialized(MMOItem item, Object object) {
		Validate.isTrue(object instanceof Boolean, "Must specify true/false");
		return new BooleanData((boolean) object);
	}

	@Override
	public boolean whenApplied(MMOItemBuilder item, StatData data) {
		if (((BooleanData) data).isEnabled())
			item.addItemTag(new ItemTag("MMOITEMS_" + getId(), true));
		return true;
	}
}
