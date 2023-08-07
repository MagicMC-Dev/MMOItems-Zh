package net.Indyuce.mmoitems.stat;

import io.lumine.mythic.lib.api.item.ItemTag;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.ReadMMOItem;
import net.Indyuce.mmoitems.stat.data.BooleanData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.BooleanStat;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

/**
 * @deprecated Merge with other Hide- stats
 */
@Deprecated
public class HideEnchants extends BooleanStat {
	public HideEnchants() {
		super("HIDE_ENCHANTS", Material.BOOK, "隐藏附魔", new String[] { "启用以完全隐藏您的物品附魔", "您仍然可以看到发光效果" }, new String[] { "all" });
	}

	@Override
	public void whenApplied(@NotNull ItemStackBuilder item, @NotNull BooleanData data) {
		if (data.isEnabled())
			item.getMeta().addItemFlags(ItemFlag.HIDE_ENCHANTS);
	}

	@Override
	public void whenLoaded(@NotNull ReadMMOItem mmoitem) {
		if (mmoitem.getNBT().getItem().getItemMeta().hasItemFlag(ItemFlag.HIDE_ENCHANTS))
			mmoitem.setData(ItemStats.HIDE_ENCHANTS, new BooleanData(true));
	}

	/**
	 * This stat is saved not as a custom tag, but as the vanilla HideFlag itself.
	 * Alas this is an empty array
	 */
	@NotNull
	@Override
	public ArrayList<ItemTag> getAppliedNBT(@NotNull BooleanData data) { return new ArrayList<>(); }
	/**
	 * This stat is saved not as a custom tag, but as the vanilla HideFlag itself.
	 * Alas this method returns null.
	 */
	@Nullable
	@Override
	public BooleanData getLoadedNBT(@NotNull ArrayList<ItemTag> storedTags) { return null; }
}
