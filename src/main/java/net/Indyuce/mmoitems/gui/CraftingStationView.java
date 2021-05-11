package net.Indyuce.mmoitems.gui;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.api.util.SmartGive;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.crafting.CraftingStation;
import net.Indyuce.mmoitems.api.crafting.CraftingStatus.CraftingQueue;
import net.Indyuce.mmoitems.api.crafting.CraftingStatus.CraftingQueue.CraftingInfo;
import net.Indyuce.mmoitems.api.crafting.IngredientInventory;
import net.Indyuce.mmoitems.api.crafting.Layout;
import net.Indyuce.mmoitems.api.crafting.ingredient.Ingredient;
import net.Indyuce.mmoitems.api.crafting.recipe.CheckedRecipe;
import net.Indyuce.mmoitems.api.crafting.recipe.CraftingRecipe;
import net.Indyuce.mmoitems.api.crafting.recipe.Recipe;
import net.Indyuce.mmoitems.api.event.PlayerUseCraftingStationEvent;
import net.Indyuce.mmoitems.api.item.util.ConfigItems;
import net.Indyuce.mmoitems.api.util.message.Message;
import net.Indyuce.mmoitems.listener.CustomSoundListener;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.UUID;

public class CraftingStationView extends PluginInventory {
	private final CraftingStation station;
	private final Layout layout;
	private List<CheckedRecipe> recipes;
	private IngredientInventory ingredients;

	private int queueOffset;

	public CraftingStationView(Player player, CraftingStation station, int page) {
		super(player);

		this.station = station;
		this.layout = station.getLayout();
		this.page = page;

		updateData();
	}

	public CraftingStation getStation() {
		return station;
	}

	void updateData() {
		ingredients = new IngredientInventory(getPlayer());
		recipes = station.getAvailableRecipes(playerData, ingredients);
	}

	@Override
	public Inventory getInventory() {
		Inventory inv = Bukkit.createInventory(this, layout.getSize(),
				station.getName().replace("#page#", "" + page).replace("#max#", "" + station.getMaxPage()));
		int min = (page - 1) * layout.getRecipeSlots().size(), max = page * layout.getRecipeSlots().size();
		for (int j = min; j < max; j++) {
			if (j >= recipes.size()) {
				if (station.getItemOptions().hasNoRecipe())
					inv.setItem(layout.getRecipeSlots().get(j - min), station.getItemOptions().getNoRecipe());
				continue;
			}

			inv.setItem(layout.getRecipeSlots().get(j - min), recipes.get(j).display());
		}

		if (max < recipes.size())
			inv.setItem(layout.getRecipeNextSlot(), ConfigItems.NEXT_PAGE.getItem());
		if (page > 1)
			inv.setItem(layout.getRecipePreviousSlot(), ConfigItems.PREVIOUS_PAGE.getItem());

		CraftingQueue queue = playerData.getCrafting().getQueue(station);
		for (int j = queueOffset; j < queueOffset + layout.getQueueSlots().size(); j++) {
			if (j >= queue.getCrafts().size()) {
				if (station.getItemOptions().hasNoQueueItem())
					inv.setItem(layout.getQueueSlots().get(j - queueOffset), station.getItemOptions().getNoQueueItem());
				continue;
			}

			inv.setItem(layout.getQueueSlots().get(j - queueOffset),
					ConfigItems.QUEUE_ITEM_DISPLAY.newBuilder(queue.getCrafts().get(j), j + 1).build());
		}
		if (queueOffset + layout.getQueueSlots().size() < queue.getCrafts().size())
			inv.setItem(layout.getQueueNextSlot(), ConfigItems.NEXT_IN_QUEUE.getItem());
		if (queueOffset > 0)
			inv.setItem(layout.getQueuePreviousSlot(), ConfigItems.PREVIOUS_IN_QUEUE.getItem());

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

				for (int j = queueOffset; j < queueOffset + layout.getQueueSlots().size(); j++)
					if (j >= queue.getCrafts().size())
						inv.setItem(layout.getQueueSlots().get(j - queueOffset),
								station.getItemOptions().hasNoQueueItem() ? station.getItemOptions().getNoQueueItem() : null);
					else
						inv.setItem(layout.getQueueSlots().get(j - queueOffset),
								ConfigItems.QUEUE_ITEM_DISPLAY.newBuilder(queue.getCrafts().get(j), j + 1).build());
			}
		}.runTaskTimerAsynchronously(MMOItems.plugin, 0, 20);
		if (station.getItemOptions().hasFill())
			for (int j = 0; j < layout.getSize(); j++)
				if (inv.getItem(j) == null || inv.getItem(j).getType() == Material.AIR)
					inv.setItem(j, station.getItemOptions().getFill());

		return inv;
	}

	@Override
	public void whenClicked(InventoryClickEvent event) {
		if (!playerData.isOnline())
			return;
		event.setCancelled(true);
		if (!MMOUtils.isMetaItem(event.getCurrentItem(), false))
			return;

		NBTItem nbtItem = MythicLib.plugin.getVersion().getWrapper().getNBTItem(event.getCurrentItem());
		if (nbtItem.getString("ItemId").equals("PREVIOUS_IN_QUEUE")) {
			queueOffset--;
			open();
			return;
		}

		if (nbtItem.getString("ItemId").equals("NEXT_IN_QUEUE")) {
			queueOffset++;
			open();
			return;
		}

		if (nbtItem.getString("ItemId").equals("NEXT_PAGE")) {
			page++;
			open();
			return;
		}

		if (nbtItem.getString("ItemId").equals("PREVIOUS_PAGE")) {
			page--;
			open();
			return;
		}

		NBTItem item = MythicLib.plugin.getVersion().getWrapper().getNBTItem(event.getCurrentItem());
		String tag = item.getString("recipeId");
		if (!tag.equals("")) {
			CheckedRecipe recipe = getRecipe(tag);
			if (event.isRightClick()) {
				new CraftingStationPreview(this, recipe).open();
				return;
			}

			processRecipe(recipe);
			open();
		}

		if (!(tag = item.getString("queueId")).equals("")) {
			UUID uuid = UUID.fromString(tag);
			CraftingInfo recipeInfo = playerData.getCrafting().getQueue(station).getCraft(uuid);
			CraftingRecipe recipe = recipeInfo.getRecipe();

			/*
			 * If the crafting recipe is ready, give the player the output item
			 * to the player and remove the recipe from the queue
			 */
			if (recipeInfo.isReady()) {
				PlayerUseCraftingStationEvent called = new PlayerUseCraftingStationEvent(playerData, station, recipe,
						PlayerUseCraftingStationEvent.StationAction.CRAFTING_QUEUE);
				Bukkit.getPluginManager().callEvent(called);
				if (called.isCancelled())
					return;

				playerData.getCrafting().getQueue(station).remove(recipeInfo);
				recipe.getTriggers().forEach(trigger -> trigger.whenCrafting(playerData));
				ItemStack craftedItem = recipe.getOutput().generate(playerData.getRPG());
				CustomSoundListener.stationCrafting(craftedItem, getPlayer());
				if (!recipe.hasOption(Recipe.RecipeOption.SILENT_CRAFT))
					getPlayer().playSound(getPlayer().getLocation(), station.getSound(), 1, 1);
				if (recipe.hasOption(Recipe.RecipeOption.OUTPUT_ITEM))
					new SmartGive(getPlayer()).give(craftedItem);

				/*
				 * If the recipe is not ready, cancel the recipe and give the
				 * ingredients back to the player
				 */
			} else {
				PlayerUseCraftingStationEvent called = new PlayerUseCraftingStationEvent(playerData, station, recipe,
						PlayerUseCraftingStationEvent.StationAction.CANCEL_QUEUE);
				Bukkit.getPluginManager().callEvent(called);
				if (called.isCancelled())
					return;

				playerData.getCrafting().getQueue(station).remove(recipeInfo);
				getPlayer().playSound(getPlayer().getLocation(), station.getSound(), 1, 1);
				for (Ingredient ingredient : recipeInfo.getRecipe().getIngredients())
					new SmartGive(getPlayer()).give(ingredient.generateItemStack(playerData.getRPG()));
			}

			updateData();
			open();
		}
	}

	public void processRecipe(CheckedRecipe recipe) {
		if (!recipe.areConditionsMet()) {
			Message.CONDITIONS_NOT_MET.format(ChatColor.RED).send(getPlayer());
			getPlayer().playSound(getPlayer().getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
			return;
		}

		if (!recipe.allIngredientsHad()) {
			Message.NOT_ENOUGH_MATERIALS.format(ChatColor.RED).send(getPlayer());
			getPlayer().playSound(getPlayer().getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
			return;
		}

		if (!recipe.getRecipe().canUse(playerData, ingredients, recipe, station)) {
			updateData();
			return;
		}

		PlayerUseCraftingStationEvent called = new PlayerUseCraftingStationEvent(playerData, station, recipe,
				PlayerUseCraftingStationEvent.StationAction.INTERACT_WITH_RECIPE);
		Bukkit.getPluginManager().callEvent(called);
		if (called.isCancelled())
			return;

		recipe.getRecipe().whenUsed(playerData, ingredients, recipe, station);
		recipe.getIngredients().forEach(ingredient -> ingredient.getPlayerIngredient().reduceItem(ingredient.getIngredient().getAmount()));
		recipe.getConditions().forEach(condition -> condition.getCondition().whenCrafting(playerData));

		updateData();
	}

	private CheckedRecipe getRecipe(String id) {
		for (CheckedRecipe info : recipes)
			if (info.getRecipe().getId().equals(id))
				return info;
		return null;
	}
}
