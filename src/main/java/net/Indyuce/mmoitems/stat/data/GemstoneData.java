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
import net.Indyuce.mmoitems.api.item.LiveMMOItem;
import net.Indyuce.mmoitems.stat.type.ItemStat;

public class GemstoneData {
	private final Set<AbilityData> abilities = new HashSet<>();
	private final List<PotionEffectData> effects = new ArrayList<>();
	private final Map<ItemStat, Double> stats = new HashMap<>();
	// private ParticleData particle;
	private final String name;

	/*
	 * This constructor is not really performance friendly. It should only be
	 * used when applying gem stones to keep max performance.
	 */
	public GemstoneData(JsonObject object) {
		object.getAsJsonObject("Stats").entrySet()
				.forEach(entry -> this.stats.put(MMOItems.plugin.getStats().get(entry.getKey()), entry.getValue().getAsDouble()));
		object.getAsJsonArray("Abilities").forEach(element -> this.abilities.add(new AbilityData(element.getAsJsonObject())));
		object.getAsJsonObject("Effects").entrySet()
				.forEach(entry -> this.effects.add(new PotionEffectData(PotionEffectType.getByName(entry.getKey()), entry.getValue().getAsInt())));
		name = object.get("Name").getAsString();

		// if (object.has("Particles"))
		// particle = new
		// ParticleData(object.getAsJsonObject("Particles")).toEffect();
	}

	public GemstoneData(LiveMMOItem mmoitem) {
		if (mmoitem.hasData(ItemStat.ABILITIES))
			((AbilityListData) mmoitem.getData(ItemStat.ABILITIES)).getAbilities().forEach(data -> abilities.add(data));
		if (mmoitem.hasData(ItemStat.PERM_EFFECTS))
			((PotionEffectListData) mmoitem.getData(ItemStat.PERM_EFFECTS)).getEffects().forEach(data -> effects.add(data));
		// TODO
		// for (ItemStat stat : MMOItems.plugin.getStats().getDoubleStats())
		// if (mmoitem.hasData(stat))
		// stats.put(stat, ((DoubleData) mmoitem.getData(stat)).getMin());
		name = MMOUtils.getDisplayName(mmoitem.getItem().getItem());
	}

	public GemstoneData(String name) {
		this.name = name;
	}

	public void addAbility(AbilityData ability) {
		abilities.add(ability);
	}

	public void addPermanentEffect(PotionEffectData effect) {
		effects.add(effect);
	}

	public void setStat(ItemStat stat, double value) {
		stats.put(stat, value);
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