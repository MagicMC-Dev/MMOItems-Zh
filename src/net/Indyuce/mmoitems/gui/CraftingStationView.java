package net.Indyuce.mmoitems.gui;

import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitRunnable;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.Message;
import net.Indyuce.mmoitems.api.crafting.CraftingStation;
import net.Indyuce.mmoitems.api.crafting.CraftingStatus.CraftingQueue;
import net.Indyuce.mmoitems.api.crafting.CraftingStatus.CraftingQueue.CraftingInfo;
import net.Indyuce.mmoitems.api.crafting.IngredientInventory;
import net.Indyuce.mmoitems.api.crafting.ingredient.Ingredient;
import net.Indyuce.mmoitems.api.crafting.recipe.CraftingRecipe;
import net.Indyuce.mmoitems.api.crafting.recipe.RecipeInfo;
import net.Indyuce.mmoitems.api.event.crafting.PlayerUseRecipeEvent;
import net.Indyuce.mmoitems.api.item.NBTItem;
import net.Indyuce.mmoitems.api.item.plugin.ConfigItem;
import net.Indyuce.mmoitems.api.player.PlayerData;

public class CraftingStationView extends PluginInventory {
	private final CraftingStation station;
	private final PlayerData data;

	private List<RecipeInfo> recipes;
	private IngredientInventory ingredients;

	private int queueOffset;

	private static final int[] slots = { 10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25 }, queueSlots = { 38, 39, 40, 41, 42 };
	private static final int[] fill = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 18, 26, 27, 35, 36, 37, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 28, 29, 30, 31, 32, 33, 34 };

	public CraftingStationView(Player player, CraftingStation station) {
		super(player);

		this.data = PlayerData.get(player);
		this.station = station;

		updateData();
	}

	private void updateData() {
		ingredients = new IngredientInventory(player);
		recipes = station.getAvailableRecipes(data, ingredients);
	}

	@Override
	public Inventory getInventory() {
		Inventory inv = Bukkit.createInventory(this, 54, station.getName().replace("#page#", "" + page).replace("#max#", "" + station.getMaxPage()));

		int min = (page - 1) * slots.length, max = page * slots.length;
		for (int j = min; j < max; j++) {
			if (j >= recipes.size()) {
				if (station.getItemOptions().hasNoRecipe())
					inv.setItem(slots[j - min], station.getItemOptions().getNoRecipe());
				continue;
			}

			inv.setItem(slots[j - min], recipes.get(j).display());
		}

		if (station.getItemOptions().hasFill())
			for (int slot : fill)
				inv.setItem(slot, station.getItemOptions().getFill());

		if (max < recipes.size())
			inv.setItem(26, ConfigItem.NEXT_PAGE.getItem());
		if (page > 1)
			inv.setItem(18, ConfigItem.PREVIOUS_PAGE.getItem());

		CraftingQueue queue = data.getCrafting().getQueue(station);
		for (int j = queueOffset; j < queueOffset + queueSlots.length; j++) {
			if (j >= queue.getCrafts().size()) {
				if (station.getItemOptions().hasNoQueueItem())
					inv.setItem(queueSlots[j - queueOffset], station.getItemOptions().getNoQueueItem());
				continue;
			}

			inv.setItem(queueSlots[j - queueOffset], ConfigItem.QUEUE_ITEM_DISPLAY.newBuilder(queue.getCrafts().get(j), j + 1).build());
		}
		if (queueOffset + queueSlots.length < queue.getCrafts().size())
			inv.setItem(43, ConfigItem.NEXT_IN_QUEUE.getItem());
		if (queueOffset > 0)
			inv.setItem(37, ConfigItem.PREVIOUS_IN_QUEUE.getItem());

		new BukkitRunnable() {
			public void run() {

				/*
				 * easier than caching a boolean and changing its state when
				 * closing or opening inventories which is glitchy when just
				 * updating them.
				 */
				if (inv.getViewers().size() < 1) {
					cancel();
					return;
				}

				for (int j = queueOffset; j < queueOffset + queueSlots.length; j++)
					if (j >= queue.getCrafts().size())
						inv.setItem(queueSlots[j - queueOffset], station.getItemOptions().hasNoQueueItem() ? station.getItemOptions().getNoQueueItem() : null);
					else
						inv.setItem(queueSlots[j - queueOffset], ConfigItem.QUEUE_ITEM_DISPLAY.newBuilder(queue.getCrafts().get(j), j + 1).build());
			}
		}.runTaskTimerAsynchronously(MMOItems.plugin, 0, 20);

		return inv;
	}

	@Override
	public void whenClicked(InventoryClickEvent event) {
		event.setCancelled(true);

		if (!MMOUtils.isPluginItem(event.getCurrentItem(), false))
			return;

		if (event.getCurrentItem().isSimilar(ConfigItem.PREVIOUS_IN_QUEUE.getItem())) {
			queueOffset--;
			open();
			return;
		}

		if (event.getCurrentItem().isSimilar(ConfigItem.NEXT_IN_QUEUE.getItem())) {
			queueOffset++;
			open();
			return;
		}

		if (event.getCurrentItem().isSimilar(ConfigItem.NEXT_PAGE.getItem())) {
			page++;
			open();
			return;
		}

		if (event.getCurrentItem().isSimilar(ConfigItem.PREVIOUS_PAGE.getItem())) {
			page--;
			open();
			return;
		}

		NBTItem item = MMOItems.plugin.getNMS().getNBTItem(event.getCurrentItem());
		String tag = item.getString("recipeId");
		if (!tag.equals("")) {
			RecipeInfo recipe = getRecipe(tag);
			if (!recipe.areConditionsMet()) {
				Message.CONDITIONS_NOT_MET.format(ChatColor.RED).send(player);
				player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
				return;
			}

			if (!recipe.allIngredientsHad()) {
				Message.NOT_ENOUGH_MATERIALS.format(ChatColor.RED).send(player);
				player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
				return;
			}

			if (!recipe.getRecipe().canUse(data, ingredients, recipe, station))
				return;

			PlayerUseRecipeEvent called = new PlayerUseRecipeEvent(data, station, recipe);
			Bukkit.getPluginManager().callEvent(called);
			if (called.isCancelled())
				return;

			recipe.getRecipe().whenUsed(data, ingredients, recipe, station);
			recipe.getIngredients().forEach(ingredient -> ingredient.getPlayerIngredient().reduceItem(ingredient.getIngredient().getAmount()));
			recipe.getConditions().forEach(condition -> condition.getCondition().whenCrafting(data));

			updateData();
			open();
		}

		if (!(tag = item.getString("queueId")).equals("")) {
			UUID uuid = UUID.fromString(tag);
			CraftingInfo craft = data.getCrafting().getQueue(station).getCraft(uuid);

			data.getPlayer().playSound(data.getPlayer().getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
			data.getCrafting().getQueue(station).remove(craft);

			if (craft.isReady()) {
				CraftingRecipe recipe = craft.getRecipe();
				recipe.getTriggers().forEach(trigger -> trigger.whenCrafting(data));
				MMOUtils.giveOrDrop(data.getPlayer(), recipe.getOutput().generate());
			} else
				for (Ingredient ingredient : craft.getRecipe().getIngredients())
					MMOUtils.giveOrDrop(player, ingredient.generateItemStack());

			updateData();
			open();
		}
	}

	private RecipeInfo getRecipe(String id) {
		for (RecipeInfo info : recipes)
			if (info.getRecipe().getId().equals(id))
				return info;
		return null;
	}
}
