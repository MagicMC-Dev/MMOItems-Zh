package net.Indyuce.mmoitems.stat.type;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.api.item.MMOItem;
import net.Indyuce.mmoitems.api.item.build.MMOItemBuilder;
import net.Indyuce.mmoitems.stat.data.StatData;
import net.Indyuce.mmoitems.version.nms.ItemTag;

public class DisableStat extends BooleanStat {
	private String tag;

	public DisableStat(Material material, String path, String name, String... lore) {
		super(new ItemStack(material), name, lore, "disable-" + path, new String[] { "all" });
		this.tag = path.toUpperCase().replace("-", "_");
	}

	public DisableStat(Material material, String path, String name, Material[] materials, String... lore) {
		super(new ItemStack(material), name, lore, "disable-" + path, new String[] { "all" },  materials);
		this.tag = path.toUpperCase().replace("-", "_");
	}

	public DisableStat(Material material, String path, String name, String[] types, String... lore) {
		super(new ItemStack(material), name, lore, "disable-" + path, types);
		this.tag = path.toUpperCase().replace("-", "_");
	}

	@Override
	public boolean whenLoaded(MMOItem item, ConfigurationSection config) {
		item.setData(this, new BooleanData(config.getBoolean("disable-" + tag.toLowerCase().replace("_", "-"))));
		return true;
	}

	@Override
	public boolean whenApplied(MMOItemBuilder item, StatData data) {
		if (((BooleanData) data).isEnabled())
			item.addItemTag(new ItemTag("MMOITEMS_DISABLE_" + tag, true));
		return true;
	}
}
