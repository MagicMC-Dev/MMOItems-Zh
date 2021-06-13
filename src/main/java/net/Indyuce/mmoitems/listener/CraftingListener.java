package net.Indyuce.mmoitems.listener;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.event.CraftMMOItemEvent;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.api.recipe.workbench.CachedRecipe;
import net.Indyuce.mmoitems.api.recipe.workbench.CustomRecipe;
import net.Indyuce.mmoitems.api.recipe.workbench.ingredients.WorkbenchIngredient;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.Map.Entry;

public class CraftingListener implements Listener {
    final Map<UUID, CachedRecipe> cachedRecipe = new HashMap<>();

    @EventHandler
    public void calculateCrafting(PrepareItemCraftEvent e) {
        if (!(e.getView().getPlayer() instanceof Player))
            return;
        handleCustomCrafting(e.getInventory(), (Player) e.getView().getPlayer());
    }

    @EventHandler
    public void getResult(InventoryClickEvent e) {
        if (!(e.getView().getPlayer() instanceof Player) || !(e.getInventory() instanceof CraftingInventory))
            return;
        Player player = (Player) e.getView().getPlayer();

        if (e.getSlotType() == SlotType.CRAFTING && e.getAction() == InventoryAction.PLACE_ONE)
            Bukkit.getScheduler().runTaskLater(MMOItems.plugin, () ->
                    handleCustomCrafting((CraftingInventory) e.getInventory(), player), 1);
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
            CraftMMOItemEvent event = new CraftMMOItemEvent(PlayerData.get(player),
                    cached.getResult());
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                e.setCancelled(true);
                return;
            }

            ItemStack[] newMatrix = cached.generateMatrix(inv.getMatrix());
            inv.setMatrix(new ItemStack[]{null, null, null, null, null, null, null, null, null});
            Bukkit.getScheduler().runTaskLater(MMOItems.plugin, () -> {
                boolean check = true;
                for (ItemStack stack : newMatrix)
                    if (stack != null) {
                        check = false;
                        break;
                    }
                if (check) {
                    inv.setMatrix(new ItemStack[]{null, null, null, null, null, null, null, null, null});
                } else
                    inv.setMatrix(newMatrix);
            }, 1);
            e.setCurrentItem(event.getResult());
            Bukkit.getScheduler().runTaskLater(MMOItems.plugin, player::updateInventory, 1);
        }
    }

    public void handleCustomCrafting(CraftingInventory inv, Player player) {
        cachedRecipe.remove(player.getUniqueId());

        Optional<CachedRecipe> recipe = checkRecipes(player, inv.getMatrix());
        if (recipe.isPresent()) {
            cachedRecipe.put(player.getUniqueId(), recipe.get());
            inv.setResult(recipe.get().getResult());
            Bukkit.getScheduler().runTaskLater(MMOItems.plugin, () -> {
                inv.setItem(0, recipe.get().getResult());
                player.updateInventory();
            }, 1);
        }
    }

    public Optional<CachedRecipe> checkRecipes(Player player, ItemStack[] matrix) {
        for (CustomRecipe recipe : MMOItems.plugin.getRecipes().getLegacyCustomRecipes()) {
            if ((!recipe.fitsPlayerCrafting() && matrix.length == 4) || !recipe.checkPermission(player))
                continue;

            boolean empty = true;
            for (ItemStack itemStack : matrix) {
                if (itemStack != null) {
                    empty = false;
                    break;
                }
            }
            if (empty) return Optional.empty();

            CachedRecipe cached = new CachedRecipe();
            boolean matches = true;
            List<Integer> slotsChecked = new ArrayList<>();
            for (Entry<Integer, WorkbenchIngredient> ingredients : recipe.getIngredients()) {
                if (recipe.isShapeless()) {
                    boolean check = false;
                    int nonnullcount = 0;
                    for (int i = 0; i < matrix.length; i++) {
                        if (slotsChecked.contains(i))
                            continue;
                        ItemStack item = matrix[i];
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
                        cached.clean();
                        if (nonnullcount > recipe.getIngredients().size()) {
                            check = false;
                            break;
                        }
                    }
                    if (!check)
                        matches = false;
                } else {
                    if (!ingredients.getValue().matches(matrix[ingredients.getKey()]))
                        matches = false;
                    else
                        cached.add(ingredients.getKey(), ingredients.getValue().getAmount());
                }

                if (!matches)
                    break;
            }

            if (matches) {
                cached.setResult(recipe.getResult(player));
                return Optional.of(cached);
            }
        }

        return Optional.empty();
    }
}
