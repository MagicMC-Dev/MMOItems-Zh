package net.Indyuce.mmoitems.api;

import java.util.*;

import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.stat.data.AbilityData;
import net.Indyuce.mmoitems.stat.data.ParticleData;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import org.jetbrains.annotations.NotNull;

public class ItemSet {
	private final Map<Integer, SetBonuses> bonuses = new HashMap<>();
	private final List<String> loreTag;
	private final String name, id;

	/**
	 * Arbitrary constant that only determines the maximum amount of items in a
	 * set e.g if set to 11 you can't create buffs that apply when a player
	 * wears at least 11 items of the same set. Has to be higher than 5 for
	 * CUSTOM INVENTORY plugins but it does not have to be tremendously high
	 */
	private static final int itemLimit = 10;

	public ItemSet(ConfigurationSection config) {
		this.id = config.getName().toUpperCase().replace("-", "_");
		this.loreTag = config.getStringList("lore-tag");
		this.name = config.getString("name");

		Validate.isTrue(config.contains("bonuses"), "Could not find item set bonuses");

		for (int j = 2; j <= itemLimit; j++)
			if (config.getConfigurationSection("bonuses").contains(String.valueOf(j))) {
				SetBonuses bonuses = new SetBonuses();

				// Add permissions
				for (String perm : config.getConfigurationSection("bonuses." + j).getStringList("granted-permissions")) { bonuses.addPermission(perm); }

				for (String key : config.getConfigurationSection("bonuses." + j).getKeys(false)) {

					try {
						String format = key.toUpperCase().replace("-", "_").replace(" ", "_");

						// ability
						if (key.startsWith("ability-")) {
							bonuses.addAbility(new AbilityData(config.getConfigurationSection("bonuses." + j + "." + key)));
							continue;
						}

						// potion effect
						if (key.startsWith("potion-")) {
							PotionEffectType potionEffectType = PotionEffectType.getByName(format.substring("potion-".length()));
							Validate.notNull(potionEffectType, "Could not load potion effect type from '" + format + "'");
							bonuses.addPotionEffect(new PotionEffect(potionEffectType, MMOUtils.getEffectDuration(potionEffectType),
									config.getInt("bonuses." + j + "." + key) - 1, true, false));
							continue;
						}

						// particle effect
						if (key.startsWith("particle-")) {
							bonuses.addParticle(new ParticleData(config.getConfigurationSection("bonuses." + j + "." + key)));
							continue;
						}

						// stat
						ItemStat stat = MMOItems.plugin.getStats().get(format);
						Validate.notNull(stat, "Could not find stat called '" + format + "'");
						bonuses.addStat(stat, config.getDouble("bonuses." + j + "." + key));

					} catch (IllegalArgumentException exception) {
						throw new IllegalArgumentException("Could not load set bonus '" + key + "': " + exception.getMessage());
					}
				}

				this.bonuses.put(j, bonuses);
			}
	}

	public String getName() {
		return name;
	}

	public String getId() {
		return id;
	}

	public SetBonuses getBonuses(int items) {
		SetBonuses bonuses = new SetBonuses();
		for (int j = 2; j <= Math.min(items, itemLimit); j++)
			if (this.bonuses.containsKey(j))
				bonuses.merge(this.bonuses.get(j));
		return bonuses;
	}

	public List<String> getLoreTag() {
		return loreTag;
	}

	public static class SetBonuses {
		private final Map<ItemStat, Double> stats = new HashMap<>();
		private final Map<PotionEffectType, PotionEffect> permEffects = new HashMap<>();
		private final Set<AbilityData> abilities = new HashSet<>();
		private final Set<ParticleData> particles = new HashSet<>();
		private final ArrayList<String> permissions = new ArrayList<>();

		public void addStat(ItemStat stat, double value) {
			stats.put(stat, value);
		}

		public void addPotionEffect(PotionEffect effect) {
			permEffects.put(effect.getType(), effect);
		}

		public void addAbility(AbilityData ability) {
			abilities.add(ability);
		}

		public void addParticle(ParticleData particle) {
			particles.add(particle);
		}

		public void addPermission(@NotNull String permission) { permissions.add(permission); }

		public boolean hasStat(ItemStat stat) {
			return stats.containsKey(stat);
		}

		public double getStat(ItemStat stat) {
			return stats.get(stat);
		}

		public Map<ItemStat, Double> getStats() {
			return stats;
		}

		public Collection<PotionEffect> getPotionEffects() {
			return permEffects.values();
		}

		public Set<ParticleData> getParticles() {
			return particles;
		}

		public Set<AbilityData> getAbilities() {
			return abilities;
		}

		@NotNull public ArrayList<String> getPermissions() { return permissions; }

		public void merge(SetBonuses bonuses) {
			bonuses.getStats().forEach((stat, value) -> stats.put(stat, (stats.containsKey(stat) ? stats.get(stat) : 0) + value));

			for (PotionEffect effect : bonuses.getPotionEffects())
				if (!permEffects.containsKey(effect.getType()) || permEffects.get(effect.getType()).getAmplifier() < effect.getAmplifier())
					permEffects.put(effect.getType(), effect);

			abilities.addAll(bonuses.getAbilities());

			permissions.addAll(bonuses.getPermissions());
		}
	}
}
