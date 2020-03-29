package net.Indyuce.mmoitems.api;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;

import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.stat.data.AbilityData;
import net.Indyuce.mmoitems.stat.data.ParticleData;
import net.Indyuce.mmoitems.stat.type.ItemStat;

public class ItemSet {
	private final Map<Integer, SetBonuses> bonuses = new HashMap<>();
	private final List<String> loreTag;
	private final String name, id;

	public ItemSet(ConfigurationSection section) {
		this.id = section.getName().toUpperCase().replace("-", "_");
		this.loreTag = section.getStringList("lore-tag");
		this.name = ChatColor.translateAlternateColorCodes('&', section.getString("name"));

		if (section.contains("bonuses"))
			for (int j = 2; j < 7; j++) {
				if (!section.getConfigurationSection("bonuses").contains("" + j))
					continue;

				SetBonuses bonuses = new SetBonuses();

				for (String key : section.getConfigurationSection("bonuses." + j).getKeys(false))
					try {
						String format = key.toUpperCase().replace("-", "_").replace(" ", "_");

						// ability
						if (key.startsWith("ability-")) {
							bonuses.addAbility(new AbilityData(section.getConfigurationSection("bonuses." + j + "." + key)));
							continue;
						}

						// potion effect
						if (key.startsWith("potion-")) {
							PotionEffectType potionEffectType = PotionEffectType.getByName(format.substring("potion-".length()));
							Validate.notNull(potionEffectType, "Could not load potion effect type from '" + format + "'");
							bonuses.addPotionEffect(new PotionEffect(potionEffectType, MMOUtils.getEffectDuration(potionEffectType), section.getInt("bonuses." + j + "." + key) - 1, true, false));
							continue;
						}

						// particle effect
						if (key.startsWith("particle-")) {
							bonuses.addParticle(new ParticleData(null, section.getConfigurationSection("bonuses." + j + "." + key)));
							continue;
						}

						// stat
						ItemStat stat = MMOItems.plugin.getStats().get(format);
						if (stat != null) {
							bonuses.addStat(stat, section.getDouble("bonuses." + j + "." + key));
							continue;
						}

						MMOItems.plugin.getLogger().log(Level.WARNING, "Could not load set bonus '" + id + "." + key + "'.");
					} catch (IllegalArgumentException exception) {
						MMOItems.plugin.getLogger().log(Level.WARNING, "Could not load set bonus '" + id + "." + key + "': " + exception.getMessage());
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
		for (int j = 2; j <= Math.min(items, 6); j++)
			if (this.bonuses.containsKey(j))
				bonuses.add(this.bonuses.get(j));
		return bonuses;
	}

	public List<String> getLoreTag() {
		return loreTag;
	}

	public class SetBonuses {
		private Map<ItemStat, Double> stats = new HashMap<>();
		private Map<PotionEffectType, PotionEffect> permEffects = new HashMap<>();
		private Set<AbilityData> abilities = new HashSet<>();
		private Set<ParticleData> particles = new HashSet<>();

		public SetBonuses() {
		}

		public SetBonuses(SetBonuses bonuses) {
			this.stats = new HashMap<>(bonuses.stats);
			this.permEffects = new HashMap<>(bonuses.permEffects);
			this.abilities = new HashSet<>(bonuses.abilities);
			this.particles = new HashSet<>(bonuses.particles);
		}

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

		public double getStat(ItemStat stat) {
			return stats.containsKey(stat) ? stats.get(stat) : 0;
		}

		public Set<Entry<ItemStat, Double>> getStats() {
			return stats.entrySet();
		}

		public Collection<PotionEffect> getPotionEffects() {
			return permEffects.values();
		}

		public Set<AbilityData> getAbilities() {
			return abilities;
		}

		public void add(SetBonuses bonuses) {
			bonuses.getStats().forEach(stat -> stats.put(stat.getKey(), (stats.containsKey(stat.getKey()) ? stats.get(stat.getKey()) : 0) + stat.getValue()));

			for (PotionEffect effect : bonuses.getPotionEffects())
				if (!permEffects.containsKey(effect.getType()) || permEffects.get(effect.getType()).getAmplifier() < effect.getAmplifier())
					permEffects.put(effect.getType(), effect);

			bonuses.getAbilities().forEach(ability -> abilities.add(ability));
		}
	}
}
