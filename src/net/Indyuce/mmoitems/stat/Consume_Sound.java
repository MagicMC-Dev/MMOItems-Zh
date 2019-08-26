package net.Indyuce.mmoitems.stat;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.api.item.build.MMOItemBuilder;
import net.Indyuce.mmoitems.stat.data.StatData;
import net.Indyuce.mmoitems.stat.type.StringStat;
import net.Indyuce.mmoitems.version.nms.ItemTag;

public class Consume_Sound extends StringStat {
	public Consume_Sound() {
		super(new ItemStack(Material.NOTE_BLOCK), "Consume Sound", new String[] { "The sound played when", "eating the consumable." }, "consume-sound", new String[] { "consumable" });
	}

	@Override
	public boolean whenApplied(MMOItemBuilder item, StatData data) {
		item.addItemTag(new ItemTag("MMOITEMS_CONSUME_SOUND", ((StringData) data).toString().toUpperCase().replace("-", "_").replace(" ", "_")));
		return true;
	}
}
