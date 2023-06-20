package net.Indyuce.mmoitems.stat;

import io.lumine.mythic.lib.api.item.ItemTag;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.ReadMMOItem;
import net.Indyuce.mmoitems.stat.data.BooleanData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.BooleanStat;
import io.lumine.mythic.lib.version.VersionMaterial;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

/**
 * @deprecated Merge with other Hide- stats
 */
@Deprecated
public class HideDye extends BooleanStat {
	public HideDye() {
		super("HIDE_DYE", Material.CYAN_DYE, "Hide Dyed", new String[] { "Enable to hide the 'Dyed' tag from the item." }, new String[] { "all" },
			Material.LEATHER_HELMET, Material.LEATHER_CHESTPLATE, Material.LEATHER_LEGGINGS, Material.LEATHER_BOOTS, VersionMaterial.LEATHER_HORSE_ARMOR.toMaterial());

		try {
			ItemFlag.valueOf("HIDE_DYE");
		} catch(IllegalArgumentException exception) {
			disable();
		}
	}

	@Override
	public void whenApplied(@NotNull ItemStackBuilder item, @NotNull BooleanData data) {
		if (data.isEnabled())
			item.getMeta().addItemFlags(ItemFlag.HIDE_DYE);
	}

	/**
	 * This stat is saved not as a custom tag, but as the vanilla HideFlag itself.
	 * Alas this is an empty array
	 */
	@NotNull
	@Override
	public ArrayList<ItemTag> getAppliedNBT(@NotNull BooleanData data) { return new ArrayList<>(); }

	@Override
	public void whenLoaded(@NotNull ReadMMOItem mmoitem) {
		if (mmoitem.getNBT().getItem().getItemMeta().hasItemFlag(ItemFlag.HIDE_DYE))
			mmoitem.setData(ItemStats.HIDE_DYE, new BooleanData(true));
	}


	/**
	 * This stat is saved not as a custom tag, but as the vanilla HideFlag itself.
	 * Alas this method returns null.
	 */
	@Nullable
	@Override
	public BooleanData getLoadedNBT(@NotNull ArrayList<ItemTag> storedTags) { return null; }
}
