package net.Indyuce.mmoitems.stat.data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.Indyuce.mmoitems.api.interaction.GemStone;
import org.apache.commons.lang.Validate;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.item.build.MMOItemBuilder;
import net.Indyuce.mmoitems.stat.data.random.RandomStatData;
import net.Indyuce.mmoitems.stat.data.type.Mergeable;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import org.jetbrains.annotations.NotNull;

public class GemSocketsData implements StatData, Mergeable, RandomStatData {
	private final Set<GemstoneData> gems = new HashSet<>();
	private final List<String> emptySlots;

	public GemSocketsData(List<String> emptySlots) {
		this.emptySlots = emptySlots;
	}

	public boolean canReceive(String gem) {
		return getEmptySocket(gem) != null;
	}

	public String getEmptySocket(String gem) {
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

	public List<String> getEmptySlots() {
		return emptySlots;
	}

	public Set<GemstoneData> getGemstones() {
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
	public StatData randomize(MMOItemBuilder builder) {
		return new GemSocketsData(new ArrayList<>(emptySlots));
	}
}
