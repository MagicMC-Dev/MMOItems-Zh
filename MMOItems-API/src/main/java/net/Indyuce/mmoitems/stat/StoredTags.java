package net.Indyuce.mmoitems.stat;

import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.ReadMMOItem;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.data.StoredTagsData;
import net.Indyuce.mmoitems.stat.data.random.RandomStatData;
import net.Indyuce.mmoitems.stat.type.GemStoneStat;
import net.Indyuce.mmoitems.stat.type.InternalStat;
import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.version.VersionMaterial;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import org.apache.commons.lang.NotImplementedException;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * This is another fictive item stat that makes sure that all
 * the NBT tags from the previous item are transferred towards
 * the new item.
 */
public class StoredTags extends ItemStat<RandomStatData<StoredTagsData>, StoredTagsData> implements InternalStat, GemStoneStat {
	public StoredTags() {
		super("STORED_TAGS", VersionMaterial.OAK_SIGN.toMaterial(), "Stored Tags",
				new String[] { "You found a secret dev easter egg", "introduced during the 2020 epidemic!" }, new String[] { "all" });
	}

	@Nullable
	@Override
	public RandomStatData<StoredTagsData> whenInitialized(Object object) {
		throw new NotImplementedException();
	}

	@Override
	public void whenClicked(@NotNull EditionInventory inv, @NotNull InventoryClickEvent event) {
		throw new NotImplementedException();
	}

	@Override
	public void whenInput(@NotNull EditionInventory inv, @NotNull String message, Object... info) {
		throw new NotImplementedException();
	}

	@Override
	public void whenDisplayed(List<String> lore, Optional<RandomStatData<StoredTagsData>> statData) {
		throw new NotImplementedException();
	}

	@Override
	public void whenApplied(@NotNull ItemStackBuilder item, @NotNull StoredTagsData data) {

		// Just that
		item.addItemTag(getAppliedNBT(data));
	}

	@NotNull
	@Override
	public ArrayList<ItemTag> getAppliedNBT(@NotNull StoredTagsData data) {

		// Collect all tags here
		return new ArrayList<>(data.getTags());
	}

	@Override
	public void whenLoaded(@NotNull ReadMMOItem mmoitem) { mmoitem.setData(ItemStats.STORED_TAGS, new StoredTagsData(mmoitem.getNBT())); }

	@Nullable
	@Override
	public StoredTagsData getLoadedNBT(@NotNull ArrayList<ItemTag> storedTags) { return new StoredTagsData(storedTags); }

	@NotNull
	@Override
	public StoredTagsData getClearStatData() { return new StoredTagsData(new ArrayList<>()); }
}
