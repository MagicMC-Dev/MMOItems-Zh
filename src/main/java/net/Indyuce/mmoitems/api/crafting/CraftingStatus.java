package net.Indyuce.mmoitems.api.crafting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.configuration.ConfigurationSection;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.crafting.CraftingStatus.CraftingQueue.CraftingInfo;
import net.Indyuce.mmoitems.api.crafting.recipe.CraftingRecipe;
import net.Indyuce.mmoitems.api.crafting.recipe.Recipe;
import net.Indyuce.mmoitems.api.player.PlayerData;

public class CraftingStatus {

	/*
	 * saves data about items being constructed in specific stations. players
	 * must go back to the station GUI and claim their item once it's ready
	 */
	private Map<String, CraftingQueue> queues = new HashMap<>();

	public void load(PlayerData data, ConfigurationSection config) {

		for (String stationId : config.getKeys(false)) {
			if (!MMOItems.plugin.getCrafting().hasStation(stationId)) {
				data.log("Could not find crafting station ID " + stationId + ", not loading started recipes.", "Make sure you backup that player data file before the user logs off.");
				continue;
			}

			CraftingStation station = MMOItems.plugin.getCrafting().getStation(stationId);
			CraftingQueue queue = new CraftingQueue(station);
			queues.put(stationId, queue);

			for (String recipeConfigId : config.getConfigurationSection(stationId).getKeys(false)) {
				String recipeId = config.getString(stationId + "." + recipeConfigId + ".recipe");
				if (recipeId == null || !station.hasRecipe(recipeId)) {
					data.log("Could not find recipe ID '" + recipeId + "', not loading this recipe.", "Make sure you backup that player data file before the user logs off.");
					continue;
				}

				Recipe recipe = station.getRecipe(recipeId);
				if (!(recipe instanceof CraftingRecipe)) {
					data.log("Could not load recipe " + recipeId + ", it is not a CRAFTING recipe!?");
					continue;
				}

				queue.add((CraftingRecipe) recipe, config.getLong(stationId + "." + recipeConfigId + ".started"), config.getLong(stationId + "." + recipeConfigId + ".delay"));
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

	public class CraftingQueue {
		private final String station;
		private List<CraftingInfo> crafts = new ArrayList<>();

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
				for (int j = index; j < crafts.size(); j++)
					crafts.get(j).removeDelay(Math.max(0, craft.getLeft() - craft.getElapsed()));
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
		 * PLUS the delay of the previous item since it's a queue.
		 */
		public void add(CraftingRecipe recipe) {
			add(recipe, System.currentTimeMillis(), (crafts.size() == 0 ? 0 : crafts.get(crafts.size() - 1).getLeft()) + (long) recipe.getCraftingTime() * 1000);
		}

		private void add(CraftingRecipe recipe, long started, long delay) {
			crafts.add(new CraftingInfo(recipe, started, delay));
		}

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
				return obj != null && obj instanceof CraftingInfo && ((CraftingInfo) obj).uuid.equals(uuid);
			}
		}
	}

	/*
	 * instant craft: the progress bar is displayed on the subtitle and players
	 * must stay around their initial position or the craft will be cancelled
	 */
	// private InstantCraftingInfo instant;
	//
	// public InstantCraftingInfo getInstant() {
	// return instant;
	// }
	//
	// public
	// public class InstantCraftingInfo extends CraftingInfo {
	// private final PlayerDataManager data;
	// private boolean timedOut;
	//
	// public InstantCraftingInfo(PlayerDataManager data, Recipe recipe) {
	// super(recipe);
	// this.data = data;
	// }
	//
	// public void start() {
	// data.getPlayer().closeInventory();
	//
	// new BukkitRunnable() {
	// int t = 0;
	// final String format =
	// Message.CRAFTING_SUBTITLE.formatRaw(ChatColor.GREEN);
	// final Location loc = data.getPlayer().getLocation().clone();
	//
	// public void run() {
	// t++;
	//
	// if (data.getPlayer() == null || !data.getPlayer().isOnline() ||
	// data.getPlayer().isDead() ||
	// !data.getPlayer().getWorld().equals(loc.getWorld()) ||
	// loc.distanceSquared(data.getPlayer().getLocation()) > 15) {
	// timedOut = true;
	// cancel();
	// return;
	// }
	//
	// if ((double) t / 10 > recipe.getRecipe().getCraftingTime()) {
	// timedOut = true;
	//
	// recipe = recipe.getRecipe().getRecipeInfo(data, new
	// IngredientInventory(data.getPlayer()));
	// if (!recipe.areConditionsMet()) {
	// Message.CONDITIONS_NOT_MET.format(ChatColor.RED).send(data.getPlayer());
	// data.getPlayer().playSound(data.getPlayer().getLocation(),
	// Sound.ENTITY_VILLAGER_NO, 1, 1);
	// return;
	// }
	//
	// if (!recipe.allIngredientsHad()) {
	// Message.NOT_ENOUGH_MATERIALS.format(ChatColor.RED).send(data.getPlayer());
	// data.getPlayer().playSound(data.getPlayer().getLocation(),
	// Sound.ENTITY_VILLAGER_NO, 1, 1);
	// return;
	// }
	//
	// craft();
	//
	// cancel();
	// return;
	// }
	//
	// double left = recipe.getRecipe().getCraftingTime() - (double) t / 10;
	// double r = (double) t / 10 / recipe.getRecipe().getCraftingTime();
	// data.getPlayer().sendTitle("", format.replace("#left#",
	// formatDelay(left)).replace("#bar#", MMOUtils.getProgressBar(r, 10,
	// AltChar.square)), 0, 20, 10);
	// }
	// }.runTaskTimer(MMOItems.plugin, 0, 2);
	// }
	//
	// public void craft() {
	// timedOut = true;
	//
	// for (IngredientInfo ingredient : recipe.getIngredients())
	// ingredient.getPlayerIngredient().reduceItem(ingredient.getIngredient().getAmount(),
	// ingredient.getIngredient());
	//
	// data.getPlayer().playSound(data.getPlayer().getLocation(),
	// Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
	// for (ItemStack drop :
	// data.getPlayer().getInventory().addItem(recipe.getRecipe().getOutput().generate()).values())
	// data.getPlayer().getWorld().dropItem(data.getPlayer().getLocation(),
	// drop);
	//
	// updateData();
	// open();
	// }
	//
	// public boolean isTimedOut() {
	// return timedOut || (System.currentTimeMillis() - started >
	// recipe.getRecipe().getCraftingTime());
	// }
	// }
}
