package net.Indyuce.mmoitems.stat.data;

import java.util.*;

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
			if (gem.equals("") || slot.equals(getUncoloredGemSlot()) || gem.equals(slot))
				return slot;
		return null;
	}

	@NotNull public static String getUncoloredGemSlot() { String s = MMOItems.plugin.getConfig().getString("gem-sockets.uncolored"); return s == null ? "Uncolored" : s; }

	public void add(GemstoneData gem) {
		gems.add(gem);
	}

	public void apply(String gem, GemstoneData gemstone) { emptySlots.remove(getEmptySocket(gem)); gems.add(gemstone); }

	public void addEmptySlot(@NotNull String slot) {
		emptySlots.add(slot);
	}

	@NotNull public List<String> getEmptySlots() {
		return emptySlots;
	}

	@NotNull public Set<GemstoneData> getGemstones() {
		return gems;
	}

	public void removeGem(@NotNull UUID gem) {

		// Find
		GemstoneData d = null;
		for (GemstoneData data : getGemstones()) {
			if (data.getHistoricUUID().equals(gem)) {
				//GEM//MMOItems.log("\u00a7b*\u00a77 Found gem to unregister: \u00a7a" + data.getName());
				d = data; break; }}

		// Remove
		gems.remove(d);
	}

	/**
	 * Removes such gem from this GemSocketsData, if it exists.
	 *
	 * @param data The Data from which to remove the gem
	 * @param gemUUID The Gem to remove
	 * @param socket The socket color to replace the gem with, <code>null</code> for no socket.
	 * @return Whether a gem was removed from the data.
	 */
	public static boolean removeGemFrom(@NotNull GemSocketsData data, @NotNull UUID gemUUID, @Nullable String socket) {

		boolean removal = false;
		for (GemstoneData gem : data.getGemstones()) {

			// Is it the one we are searching for?
			if (gem.getHistoricUUID().equals(gemUUID)) {

				// Found it, restore the socket and we're done.
				if (socket != null) { data.addEmptySlot(socket); }

				// Remove
				removal = true; break; } }

		// Its time.
		if (removal) { data.removeGem(gemUUID); }

		return removal;
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

		//MRG//MMOItems.log("\u00a73||| \u00a77Merging slots; Original:");
		//MRG//for (String str : SilentNumbers.transcribeList(emptySlots, (s) -> "\u00a73|+ \u00a77" + ((String) s))) { MMOItems.log(str); }
		//MRG//MMOItems.log("\u00a73||| \u00a77Including");
		//MRG//for (String str : SilentNumbers.transcribeList(((GemSocketsData) data).emptySlots, (s) -> "\u00a73|+ \u00a77" + ((String) s))) { MMOItems.log(str); }

		// Combine both actual gems, and empty slots
		emptySlots.addAll(((GemSocketsData) data).emptySlots);
		gems.addAll(((GemSocketsData) data).getGemstones());

		//MRG//MMOItems.log("\u00a73||| \u00a7aResult");
		//MRG//for (String str : SilentNumbers.transcribeList(emptySlots, (s) -> "\u00a73|+ \u00a77" + ((String) s))) { MMOItems.log(str); }
		//MRG//MMOItems.log("\u00a73||| \u00a7a---------------------------------------");
	}

	@Override
	public @NotNull StatData cloneData() {

		// Start Fresh
		GemSocketsData ret = new GemSocketsData(new ArrayList<>(emptySlots));

		// Add Gems
		for (GemstoneData g : getGemstones()) { ret.add(g.cloneGem()); }

		return ret;
	}

	@Override
	public boolean isClear() { return getGemstones().size() == 0 && getEmptySlots().size() == 0; }

	@Override
	public StatData randomize(MMOItemBuilder builder) {
		return new GemSocketsData(new ArrayList<>(emptySlots));
	}
}
