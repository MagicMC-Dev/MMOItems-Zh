package net.Indyuce.mmoitems.listener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.recipe.workbench.CachedRecipe;
import net.Indyuce.mmoitems.api.recipe.workbench.CustomRecipe;
import net.Indyuce.mmoitems.api.recipe.workbench.ingredients.WorkbenchIngredient;

public class CraftingListener implements Listener {
	Map<UUID, CachedRecipe> cachedRecipe = new HashMap<>();

	@EventHandler
	public void calculateCrafting(PrepareItemCraftEvent e) {
		if (!(e.getView().getPlayer() instanceof Player))
			return;
		handleCustomCrafting(e.getInventory(), (Player) e.getView().getPlayer());
	}

	@EventHandler
	public void getResult(InventoryClickEvent e) {
		if (!(e.getView().getPlayer() instanceof Player) ||
			!(e.getInventory() instanceof CraftingInventory)) return;
		if (e.getSlotType() == SlotType.CRAFTING && e.getAction() == InventoryAction.PLACE_ONE)
			Bukkit.getScheduler().runTaskLater(MMOItems.plugin, new Runnable() {
				@Override
				public void run() {
					handleCustomCrafting((CraftingInventory) e.getInventory(), (Player) e.getView().getPlayer());
				}
			}, 1);
		else if (e.getSlotType() == SlotType.RESULT) {
			CraftingInventory inv = (CraftingInventory) e.getInventory();
			if (e.getCurrentItem() == null || !cachedRecipe.containsKey(e.getWhoClicked().getUniqueId()))
				return;
			if (e.getAction() != InventoryAction.PICKUP_ALL) {
				e.setCancelled(true);
				return;
			}
			CachedRecipe cached = cachedRecipe.get(e.getWhoClicked().getUniqueId());
			cachedRecipe.remove(e.getWhoClicked().getUniqueId());
			
			if (!cached.isValid(inv.getMatrix())) {
				e.setCancelled(true);
				return;
			}
			ItemStack[] newMatrix = cached.generateMatrix(inv.getMatrix());
			inv.setMatrix(new ItemStack[] { null, null, null, null, null, null, null, null, null });
			Bukkit.getScheduler().runTaskLater(MMOItems.plugin, new Runnable() {
				@Override
				public void run() {
					boolean check = true;
					for (ItemStack stack : newMatrix)
						if (stack != null)
							check = false;
					if (check) {
						inv.setMatrix(new ItemStack[] { null, null, null, null, null, null, null, null, null });
						((Player) e.getView().getPlayer()).updateInventory();
					} else
						inv.setMatrix(newMatrix);
				}
			}, 1);
			e.setCurrentItem(cached.getResult());
		}
	}

	public void handleCustomCrafting(CraftingInventory inv, Player player) {
		cachedRecipe.remove(player.getUniqueId());
		for (CustomRecipe recipe : MMOItems.plugin.getRecipes().getCustomRecipes()) {
			if (!recipe.fitsPlayerCrafting() && inv.getMatrix().length == 4)
				continue;
			int airCount = 0;

			for(Entry<Integer, WorkbenchIngredient> ingredient : recipe.getIngredients()){
				if(ingredient.getValue().matches(new ItemStack(Material.AIR))){
					airCount++;
				}
			}
			if(recipe.isEmpty() || airCount > 8){
				continue;
			}
			CachedRecipe cached = new CachedRecipe();
			boolean matches = true;
			List<Integer> slotsChecked = new ArrayList<>();
			for (Entry<Integer, WorkbenchIngredient> ingredients : recipe.getIngredients()) {
				if (recipe.isShapeless()) {
					boolean check = false;
					int nonnullcount = 0;
					for (int i = 0; i < inv.getMatrix().length; i++) {
						if (slotsChecked.contains(i))
							continue;
						ItemStack item = inv.getMatrix()[i];
						if (item == null) {
							slotsChecked.add(i);
							continue;
						}
						nonnullcount += 1;
						if (ingredients.getValue().matches(item)) {
							cached.add(i, ingredients.getValue().getAmount());
							slotsChecked.add(i);
							check = true;
						}
						if (nonnullcount > recipe.getIngredients().size()) {
							check = false;
							break;
						}
					}
					if (!check)
						matches = false;
				} else {
					if (!ingredients.getValue().matches(inv.getMatrix()[ingredients.getKey()]))
						matches = false;
					else cached.add(ingredients.getKey(), ingredients.getValue().getAmount());
				}

				if (!matches) break;
			}

			if (matches) {
				cached.setResult(recipe.getResult());
				cachedRecipe.put(player.getUniqueId(), cached);
				inv.setResult(recipe.getResult());
				Bukkit.getScheduler().runTaskLater(MMOItems.plugin, new Runnable() {
					@Override
					public void run() {
						inv.setItem(0, recipe.getResult());
						player.updateInventory();
					}
				}, 1);
				break;
			}
		}
	}
}
