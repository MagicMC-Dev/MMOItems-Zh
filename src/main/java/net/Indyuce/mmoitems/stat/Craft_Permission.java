package net.Indyuce.mmoitems.stat;

import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.api.item.MMOItem;
import net.Indyuce.mmoitems.api.item.build.MMOItemBuilder;
import net.Indyuce.mmoitems.stat.data.StatData;
import net.Indyuce.mmoitems.stat.type.StringStat;
import net.mmogroup.mmolib.api.item.NBTItem;
import net.mmogroup.mmolib.version.VersionMaterial;

public class Craft_Permission extends StringStat {
	public Craft_Permission() {
		super("CRAFT_PERMISSION", new ItemStack(VersionMaterial.OAK_SIGN.toMaterial()), "Crafting Recipe Permission", new String[] { "The permission needed to craft this item.", "Changing this value requires &o/mi reload recipes&7." }, new String[] { "all" });

		disable();
	}

	@Override
	public StatData whenInitialized(MMOItem item, Object object) {
		return null;
	}

	@Override
	public void whenLoaded(MMOItem mmoitem, NBTItem item) {
	}

	@Override
	public boolean whenApplied(MMOItemBuilder item, StatData data) {
		return true;
	}
}