package net.Indyuce.mmoitems.stat.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.potion.PotionEffectType;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.item.MMOItem;
import net.Indyuce.mmoitems.api.item.NBTItem;
import net.Indyuce.mmoitems.stat.Abilities.AbilityListData;
import net.Indyuce.mmoitems.stat.type.DoubleStat.DoubleData;
import net.Indyuce.mmoitems.stat.type.ItemStat;

public class GemSocketsData extends StatData {
	private Set<GemstoneData> gems = new HashSet<>();
	private List<String> slots;

	/*
	 * used when the MMOItem is generated. when the item has already been
	 * generated, the direct item slot amount isloaded.
	 */
	private StringListData unloadedSlots;
	private boolean loaded;

	public GemSocketsData(List<String> slots) {
		this.slots = slots;
		this.loaded = true;
	}

	public GemSocketsData(StringListData unloadedSlots) {
		this.unloadedSlots = unloadedSlots;
		this.loaded = false;
	}

	public boolean canReceive(String gem) {
		return getEmptySocket(gem) != null;
	}

	public String getEmptySocket(String gem) {
		for (String slot : slots)
			if (gem.equals("") || slot.equals(MMOItems.plugin.getConfig().getString("gem-sockets.uncolored")) || gem.equals(slot))
				return slot;
		return null;
	}

	public void add(GemstoneData gem) {
		gems.add(gem);
	}

	public void apply(String gem, GemstoneData gemstone) {
		slots.remove(getEmptySocket(gem));
		gems.add(gemstone);
	}

	public List<String> getEmptySlots() {
		return loaded ? slots : unloadedSlots.getList();
	}

	public Set<GemstoneData> getGemstones() {
		return gems;
	}

	public JsonObject toJson() {
		JsonObject object = new JsonObject();

		JsonArray empty = new JsonArray();
		getEmptySlots().forEach(slot -> empty.add(slot));
		object.add("EmptySlots", empty);

		JsonArray array = new JsonArray();
		gems.forEach(gem -> array.add(gem.toJson()));
		object.add("Gemstones", array);

		return object;
	}

	public GemstoneData newGemstone(JsonObject object) {
		return new GemstoneData(object);
	}

	public GemstoneData newGemstone(NBTItem nbtItem, MMOItem mmoitem) {
		return new GemstoneData(nbtItem, mmoitem);
	}

	public class GemstoneData {
		private Set<AbilityData> abilities = new HashSet<>();
		private List<PotionEffectData> effects = new ArrayList<>();
		private Map<ItemStat, Double> stats = new HashMap<>();
		// private ParticleData particle;
		private String name;

		/*
		 * This constructor is not really performance friendly. It should only
		 * be used when applying gem stones to keep max performance.
		 */
		public GemstoneData(JsonObject object) {
			object.getAsJsonObject("Stats").entrySet().forEach(entry -> this.stats.put(MMOItems.plugin.getStats().get(entry.getKey()), entry.getValue().getAsDouble()));
			object.getAsJsonArray("Abilities").forEach(element -> this.abilities.add(new AbilityData(element.getAsJsonObject())));
			object.getAsJsonObject("Effects").entrySet().forEach(entry -> this.effects.add(new PotionEffectData(PotionEffectType.getByName(entry.getKey()), entry.getValue().getAsInt())));
			name = object.get("Name").getAsString();

			// if (object.has("Particles"))
			// particle = new
			// ParticleData(object.getAsJsonObject("Particles")).toEffect();
		}

		public GemstoneData(NBTItem nbtItem, MMOItem mmoitem) {
			if (mmoitem.hasData(ItemStat.ABILITIES))
				abilities = ((AbilityListData) mmoitem.getData(ItemStat.ABILITIES)).getAbilities();
			if (mmoitem.hasData(ItemStat.PERM_EFFECTS))
				effects = ((EffectListData) mmoitem.getData(ItemStat.PERM_EFFECTS)).getEffects();
			for (ItemStat stat : MMOItems.plugin.getStats().getDoubleStats())
				if (mmoitem.hasData(stat))
					stats.put(stat, ((DoubleData) mmoitem.getData(stat)).getMin());
			name = MMOUtils.getDisplayName(nbtItem.getItem());
		}

		// public boolean hasParticle() {
		// return particle != null;
		// }

		public String getName() {
			return name;
		}

		public JsonObject toJson() {
			JsonObject object = new JsonObject();
			object.addProperty("Name", name);

			JsonObject stats = new JsonObject();
			for (ItemStat stat : this.stats.keySet())
				stats.addProperty(stat.getId(), this.stats.get(stat));
			object.add("Stats", stats);

			JsonArray abilities = new JsonArray();
			this.abilities.forEach(ability -> abilities.add(ability.toJson()));
			object.add("Abilities", abilities);

			JsonObject effects = new JsonObject();
			this.effects.forEach(effect -> effects.addProperty(effect.getType().getName(), effect.getLevel()));
			object.add("Effects", effects);

			// if (particle != null)
			// object.add("Particles", particle.toJson());

			return object;
		}
	}
}
