package net.Indyuce.mmoitems.stat;

import io.lumine.mythic.lib.api.item.ItemTag;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.ReadMMOItem;
import net.Indyuce.mmoitems.stat.data.StringData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.StringStat;
import io.lumine.mythic.lib.version.VersionMaterial;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class CraftingPermission extends StringStat {
	public CraftingPermission() {
		super("CRAFT_PERMISSION", VersionMaterial.OAK_SIGN.toMaterial(), "Crafting Recipe Permission",
				new String[] { "The permission needed to craft this item.", "Changing this value requires &o/mi reload recipes&7." },
				new String[] { "all" });

		disable();
	}

	/**
	 * This stat is not saved onto items. This method is empty.
	 */
	@Override
	public void whenLoaded(@NotNull ReadMMOItem mmoitem) { }

	/**
	 * This stat is not saved onto items. This method always returns null
	 */
	@Nullable
	@Override
	public StatData getLoadedNBT(@NotNull ArrayList<ItemTag> storedTags) { return null; }

	/**
	 * This stat is not saved onto items. This method is empty.
	 */
	@Override
	public void whenApplied(@NotNull ItemStackBuilder item, @NotNull StatData data) { }

	/**
	 * This stat is not saved onto items. This method returns an empty array.
	 */
	@NotNull
	@Override public ArrayList<ItemTag> getAppliedNBT(@NotNull StatData data) { return new ArrayList<>(); }
}