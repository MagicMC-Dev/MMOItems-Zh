package net.Indyuce.mmoitems.stat;

import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.inventory.meta.Damageable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.lumine.mythic.lib.api.item.ItemTag;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.ReadMMOItem;
import net.Indyuce.mmoitems.stat.data.DoubleData;
import net.Indyuce.mmoitems.stat.data.random.RandomStatData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.DoubleStat;
import net.Indyuce.mmoitems.stat.type.GemStoneStat;

public class ItemDamage extends DoubleStat implements GemStoneStat {
	public ItemDamage() {
		super("DURABILITY", Material.FISHING_ROD, "Item Damage",
				new String[] { "Default item damage. This does &cNOT", "impact the item's max durability." }, new String[] { "!block", "all" });
	}

	@Override
	public void whenApplied(@NotNull ItemStackBuilder item, @NotNull StatData data) {
		if (item.getMeta() instanceof Damageable)
			((Damageable) item.getMeta()).setDamage((int) ((DoubleData) data).getValue());
	}
	@Override
	public void whenPreviewed(@NotNull ItemStackBuilder item, @NotNull StatData currentData, @NotNull RandomStatData templateData) throws IllegalArgumentException { whenApplied(item, currentData);}
	/**
	 * This stat is saved not as a custom tag, but as the vanilla HideFlag itself.
	 * Alas this is an empty array
	 */
	@NotNull
	@Override
	public ArrayList<ItemTag> getAppliedNBT(@NotNull StatData data) { return new ArrayList<>(); }

	@Override
	public void whenLoaded(@NotNull ReadMMOItem mmoitem) {
		if (mmoitem.getNBT().getItem().getItemMeta() instanceof Damageable)
			mmoitem.setData(ItemStats.DURABILITY, new DoubleData(((Damageable) mmoitem.getNBT().getItem().getItemMeta()).getDamage()));
	}

	/**
	 * This stat is saved not as a custom tag, but as the vanilla HideFlag itself.
	 * Alas this method returns null.
	 */
	@Nullable
	@Override
	public StatData getLoadedNBT(@NotNull ArrayList<ItemTag> storedTags) { return null; }
}
