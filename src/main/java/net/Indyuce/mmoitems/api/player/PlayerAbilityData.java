package net.Indyuce.mmoitems.api.player;

import java.util.HashMap;
import java.util.Map;

import net.Indyuce.mmoitems.comp.mythicmobs.MythicMobsAbility;
import net.Indyuce.mmoitems.stat.data.AbilityData;

public class PlayerAbilityData {

	/*
	 * MythicMobs skill damage is handled via math formula which can retrieve
	 * PAPI placeholders. when a skill is cast, all skill modifiers are cached
	 * into that map: 1- for easier and faster access 2- it removes interference
	 * for example when stats are calculating not when the spell is cast but
	 * rather when the spell hits
	 */
	private final Map<String, CachedModifier> cache = new HashMap<>();

	public double getCachedModifier(String name) {
		return cache.containsKey(name) ? cache.get(name).getValue() : 0;
	}

	public void cacheModifiers(MythicMobsAbility ability, AbilityData data) {
		for (String modifier : data.getModifiers())
			cacheModifier(ability, modifier, data.getModifier(modifier));
	}

	public void cacheModifier(MythicMobsAbility ability, String name, double value) {
		cache.put(ability.getInternalName() + "." + name, new CachedModifier(value));
	}

	public void refresh() {
		cache.values().removeIf(CachedModifier::isTimedOut);
	}

	public static class CachedModifier {
		private final long date = System.currentTimeMillis();
		private final double value;

		public CachedModifier(double value) {
			this.value = value;
		}

		public boolean isTimedOut() {
			return date + 1000 * 60 < System.currentTimeMillis();
		}

		public double getValue() {
			return value;
		}
	}
}
