package net.Indyuce.mmoitems.api.crafting;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.crafting.CraftingStatus.CraftingQueue.CraftingInfo;
import net.Indyuce.mmoitems.api.crafting.recipe.CraftingRecipe;
import net.Indyuce.mmoitems.api.crafting.recipe.Recipe;
import net.Indyuce.mmoitems.api.player.PlayerData;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;
import java.util.logging.Level;

public class CraftingStatus {

    /*
     * saves data about items being constructed in specific stations. players
     * must go back to the station GUI and claim their item once it's ready
     */
    private final Map<String, CraftingQueue> queues = new HashMap<>();

    public void load(PlayerData data, ConfigurationSection config) {
        String name = data.isOnline() ? data.getPlayer().getName() : "Unknown Player";

        for (String stationId : config.getKeys(false)) {
            if (!MMOItems.plugin.getCrafting().hasStation(stationId)) {
                MMOItems.plugin.getLogger().log(Level.WARNING,
                        "An error occurred while trying to load crafting station recipe data of '" + name + "': "
                                + "could not find crafting station with ID '" + stationId
                                + "', make sure you backup that player data file before the user logs off.");
                continue;
            }

            CraftingStation station = MMOItems.plugin.getCrafting().getStation(stationId);
            CraftingQueue queue = new CraftingQueue(station);
            queues.put(stationId, queue);

            for (String recipeConfigId : config.getConfigurationSection(stationId).getKeys(false)) {
                String recipeId = config.getString(stationId + "." + recipeConfigId + ".recipe");
                if (recipeId == null || !station.hasRecipe(recipeId)) {
                    MMOItems.plugin.getLogger().log(Level.WARNING,
                            "An error occurred while trying to load crafting station recipe data of '" + name + "': "
                                    + "could not find recipe with ID '" + recipeId
                                    + "', make sure you backup that player data file before the user logs off.");
                    continue;
                }

                Recipe recipe = station.getRecipe(recipeId);
                if (!(recipe instanceof CraftingRecipe)) {
                    MMOItems.plugin.getLogger().log(Level.WARNING, "An error occurred while trying to load crafting station recipe data of '"
                            + name + "': " + "recipe '" + recipe.getId() + "' is not a CRAFTING recipe.");
                    continue;
                }

                queue.add((CraftingRecipe) recipe, config.getLong(stationId + "." + recipeConfigId + ".started"),
                        config.getLong(stationId + "." + recipeConfigId + ".delay"));
            }
        }
    }

    public void save(ConfigurationSection config) {
        for (String station : queues.keySet()) {
            CraftingQueue queue = queues.get(station);

            for (CraftingInfo craft : queue.getCrafts()) {
                config.set(station + ".recipe-" + craft.getUniqueId().toString() + ".recipe", craft.getRecipe().getId());
                config.set(station + ".recipe-" + craft.getUniqueId().toString() + ".started", craft.started);
                config.set(station + ".recipe-" + craft.getUniqueId().toString() + ".delay", craft.delay);
            }
        }
    }

    public CraftingQueue getQueue(CraftingStation station) {
        if (!queues.containsKey(station.getId()))
            queues.put(station.getId(), new CraftingQueue(station));
        return queues.get(station.getId());
    }

    public static class CraftingQueue {
        private final String station;
        private final List<CraftingInfo> crafts = new ArrayList<>();

        public CraftingQueue(CraftingStation station) {
            this.station = station.getId();
        }

        public List<CraftingInfo> getCrafts() {
            return crafts;
        }

        public boolean isFull(CraftingStation station) {
            return crafts.size() >= station.getMaxQueueSize();
        }

        public void remove(CraftingInfo craft) {
            int index = crafts.indexOf(craft);
            if (index != -1)
                for (int j = index + 1; j < crafts.size(); j++) {
                    CraftingInfo nextCraft = crafts.get(j);
                    nextCraft.delay = Math.max(0, nextCraft.delay - craft.getLeft());
                }
            crafts.remove(craft);
        }

        public CraftingInfo getCraft(UUID uuid) {
            for (CraftingInfo craft : crafts)
                if (craft.getUniqueId().equals(uuid))
                    return craft;
            return null;
        }

        /*
         * when adding a crafting recipe, the delay is the actual crafting time
         * PLUS the delay left for the previous item since it's a queue.
         */
        public void add(CraftingRecipe recipe) {
            add(recipe, System.currentTimeMillis(),
                    (crafts.size() == 0 ? 0 : crafts.get(crafts.size() - 1).getLeft()) + (long) recipe.getCraftingTime() * 1000);
        }

        private void add(CraftingRecipe recipe, long started, long delay) {
            crafts.add(new CraftingInfo(recipe, started, delay));
        }

        @Deprecated
        public CraftingStation getStation() {
            return MMOItems.plugin.getCrafting().getStation(station);
        }

        public class CraftingInfo {
            private final String recipe;
            private final UUID uuid = UUID.randomUUID();
            private final long started;
            private long delay;

            private CraftingInfo(CraftingRecipe recipe, long started, long delay) {
                this.recipe = recipe.getId();
                this.started = started;
                this.delay = delay;
            }

            public UUID getUniqueId() {
                return uuid;
            }

            /**
             * @deprecated /mi reload stations force MI to save the recipe
             * IDs instead of a direct reference to the crafting recipe
             */
            @Deprecated
            public CraftingRecipe getRecipe() {
                return (CraftingRecipe) getStation().getRecipe(recipe);
            }

            public boolean isReady() {
                return getLeft() == 0;
            }

            public void removeDelay(long amount) {
                this.delay -= amount;
            }

            public long getElapsed() {
                return Math.max((long) getRecipe().getCraftingTime() * 1000, System.currentTimeMillis() - started);
            }

            public long getLeft() {
                return Math.max(0, started + delay - System.currentTimeMillis());
            }

            @Override
            public boolean equals(Object obj) {
                return obj instanceof CraftingInfo && ((CraftingInfo) obj).uuid.equals(uuid);
            }
        }
    }
}
