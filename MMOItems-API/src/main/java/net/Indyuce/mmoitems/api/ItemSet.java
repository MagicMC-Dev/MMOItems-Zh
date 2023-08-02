package net.Indyuce.mmoitems.api;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.stat.data.AbilityData;
import net.Indyuce.mmoitems.stat.data.ParticleData;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.Indyuce.mmoitems.util.MMOUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

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

        Validate.isTrue(config.isConfigurationSection("bonuses"), "找不到物品套装奖励");

        for (int j = 2; j <= itemLimit; j++)
            if (config.getConfigurationSection("bonuses").contains(String.valueOf(j))) {
                final String bonusesKey = "bonuses.%d".formatted(j);
                final SetBonuses bonuses = new SetBonuses();
                final ConfigurationSection bonusesSection = config.getConfigurationSection(bonusesKey);
                Validate.notNull(bonusesSection, "项集 '%s' 不是有效的配置节。".formatted(id));

                // Add permissions
                for (String perm : bonusesSection.getStringList("granted-permissions"))
                    bonuses.addPermission(perm);

                for (String key : bonusesSection.getKeys(false))
                    if (!key.equals("granted-permissions")) {
                        try {
                            String format = key.toUpperCase().replace("-", "_").replace(" ", "_");

                            // ability
                            if (key.startsWith("ability-")) {
                                final ConfigurationSection section = config.getConfigurationSection("%s.%s".formatted(bonusesKey, key));
                                Validate.notNull(section, "能力 '%s' 不是有效的配置部分。".formatted(key));
                                bonuses.addAbility(new AbilityData(section));
                                continue;
                            }

                            // potion effect
                            if (key.startsWith("potion-")) {
                                PotionEffectType potionEffectType = PotionEffectType.getByName(format.substring("potion-".length()));
                                Validate.notNull(potionEffectType, "无法从 '%s' 加载药水效果类型".formatted(format));
                                bonuses.addPotionEffect(new PotionEffect(potionEffectType, MMOUtils.getEffectDuration(potionEffectType),
                                        config.getInt("%s.%s".formatted(bonusesKey, key)) - 1, true, false));
                                continue;
                            }

                            // particle effect
                            if (key.startsWith("particle-")) {
                                final ConfigurationSection section = config.getConfigurationSection("bonuses.%d.%s".formatted(j, key));
                                Validate.notNull(section, "粒子效果 '%s '不是有效的配置部分。".formatted(key));
                                bonuses.addParticle(new ParticleData(section));
                                continue;
                            }

                            // stat
                            ItemStat<?, ?> stat = MMOItems.plugin.getStats().get(format);
                            Validate.notNull(stat, "找不到名为 '%s '的统计数据".formatted(format));
                            bonuses.addStat(stat, config.getDouble("bonuses.%d.%s".formatted(j, key)));
                        } catch (IllegalArgumentException exception) {
                            throw new IllegalArgumentException("无法加载设置的奖金 '%s ': %s".formatted(key, exception.getMessage()));
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

    @NotNull
    public SetBonuses getBonuses(int items) {
        final SetBonuses bonuses = new SetBonuses();
        for (int j = 2; j <= Math.min(items, itemLimit); j++) {
            final @Nullable SetBonuses extra = this.bonuses.get(j);
            if (extra != null)
                bonuses.merge(extra);
        }
        return bonuses;
    }

    public List<String> getLoreTag() {
        return loreTag;
    }

    public static class SetBonuses {
        private final Map<ItemStat<?, ?>, Double> stats = new HashMap<>();
        private final Map<PotionEffectType, PotionEffect> permEffects = new HashMap<>();
        private final List<AbilityData> abilities = new ArrayList<>();
        private final List<ParticleData> particles = new ArrayList<>();
        private final ArrayList<String> permissions = new ArrayList<>();

        public void addStat(ItemStat<?, ?> stat, double value) {
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

        public void addPermission(@NotNull String permission) {
            permissions.add(permission);
        }

        public boolean hasStat(ItemStat<?, ?> stat) {
            return stats.containsKey(stat);
        }

        public double getStat(ItemStat<?, ?> stat) {
            return stats.get(stat);
        }

        public Map<ItemStat<?, ?>, Double> getStats() {
            return stats;
        }

        public Collection<PotionEffect> getPotionEffects() {
            return permEffects.values();
        }

        public List<ParticleData> getParticles() {
            return particles;
        }

        public List<AbilityData> getAbilities() {
            return abilities;
        }

        @NotNull
        public ArrayList<String> getPermissions() {
            return permissions;
        }

        public void merge(SetBonuses bonuses) {
            bonuses.getStats().forEach((stat, value) -> stats.put(stat, (stats.containsKey(stat) ? stats.get(stat) : 0) + value));

            for (PotionEffect effect : bonuses.getPotionEffects())
                if (!permEffects.containsKey(effect.getType()) || permEffects.get(effect.getType()).getAmplifier() < effect.getAmplifier())
                    permEffects.put(effect.getType(), effect);

            abilities.addAll(bonuses.abilities);
            particles.addAll(bonuses.particles);
            permissions.addAll(bonuses.permissions);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemSet itemSet = (ItemSet) o;
        return id.equals(itemSet.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
