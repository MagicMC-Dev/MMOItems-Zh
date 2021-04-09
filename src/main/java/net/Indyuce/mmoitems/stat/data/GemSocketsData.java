package net.Indyuce.mmoitems.stat.data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.lumine.mythic.lib.api.util.Ref;
import io.lumine.mythic.lib.api.util.ui.SilentNumbers;
import net.Indyuce.mmoitems.api.interaction.GemStone;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import org.apache.commons.lang.Validate;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.item.build.MMOItemBuilder;
import net.Indyuce.mmoitems.stat.data.random.RandomStatData;
import net.Indyuce.mmoitems.stat.data.type.Mergeable;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GemSocketsData implements StatData, Mergeable, RandomStatData {
	@NotNull private final Set<GemstoneData> gems = new HashSet<>();
	@NotNull private final List<String> emptySlots;

	public GemSocketsData(@NotNull List<String> emptySlots) {
		this.emptySlots = emptySlots;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof GemSocketsData)) { return false; }
		if (((GemSocketsData) obj).getEmptySlots().size() != getEmptySlots().size()) { return false; }
		if (((GemSocketsData) obj).getGemstones().size() != getGemstones().size()) { return false; }
		if (!SilentNumbers.hasAll(((GemSocketsData) obj).getEmptySlots(), getEmptySlots())) { return false; }

		for (GemstoneData objGem : ((GemSocketsData) obj).getGemstones()) {

			if (objGem == null) { continue; }

			// Validate with ours
			boolean unmatched = true;
			for (GemstoneData thisGem : getGemstones()) {

				// Test match
				if (objGem.equals(thisGem)) {
					unmatched = false;
					break; }
			}
			if (unmatched) { return false; } }

		// All equal
		return true;
	}

	/**
	 * Attempts to find a slot of the same color of this gem within the item.
	 * <p></p>
	 * To know the color of the socket pass the same argument to {@link #getEmptySocket(String)}
	 * which checks in the same order as this method for the first success.
	 */
	public boolean canReceive(@NotNull String gem) {
		return getEmptySocket(gem) != null;
	}

	/**
	 * Get the first emtpty gem socket that matches this color
	 * @return <code>null</code> if none matched.
	 */
	@Nullable public String getEmptySocket(@NotNull String gem) {
		for (String slot : emptySlots)
			if (gem.equals("") || slot.equals(MMOItems.plugin.getConfig().getString("gem-sockets.uncolored")) || gem.equals(slot))
				return slot;
		return null;
	}

	public void add(GemstoneData gem) {
		gems.add(gem);
	}

	public void apply(String gem, GemstoneData gemstone) {
		emptySlots.remove(getEmptySocket(gem));
		gems.add(gemstone);
	}

	public void addEmptySlot(String slot) {
		emptySlots.add(slot);
	}

	@NotNull public List<String> getEmptySlots() {
		return emptySlots;
	}

	@NotNull public Set<GemstoneData> getGemstones() {
		return gems;
	}

	public JsonObject toJson() {
		JsonObject object = new JsonObject();

		JsonArray empty = new JsonArray();
		getEmptySlots().forEach(empty::add);
		object.add("EmptySlots", empty);

		JsonArray array = new JsonArray();
		gems.forEach(gem -> array.add(gem.toJson()));
		object.add("Gemstones", array);

		return object;
	}

	@Override
	public void merge(StatData data) {
		Validate.isTrue(data instanceof GemSocketsData, "Cannot merge two different stat data types");
		emptySlots.addAll(((GemSocketsData) data).emptySlots);
	}

	@Override
	public @NotNull StatData cloneData() {

		// Start Fresh
		GemSocketsData ret = new GemSocketsData(emptySlots);

		// Add Gems
		for (GemstoneData g : getGemstones()) { ret.add(g); }

		return ret;
	}

	@Override
	public boolean isClear() { return getGemstones().size() == 0 && getEmptySlots().size() == 0; }

	@Override
	public StatData randomize(MMOItemBuilder builder) {
		return new GemSocketsData(new ArrayList<>(emptySlots));
	}
}
