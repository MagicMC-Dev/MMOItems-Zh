package net.Indyuce.mmoitems.api.crafting.recipe;

import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.crafting.ConfigMMOItem;
import net.Indyuce.mmoitems.api.crafting.CraftingStation;
import net.Indyuce.mmoitems.api.crafting.IngredientInventory;
import net.Indyuce.mmoitems.api.crafting.IngredientInventory.IngredientLookupMode;
import net.Indyuce.mmoitems.api.crafting.IngredientInventory.PlayerIngredient;
import net.Indyuce.mmoitems.api.crafting.ingredient.Ingredient;
import net.Indyuce.mmoitems.api.crafting.ingredient.MMOItemIngredient;
import net.Indyuce.mmoitems.api.item.mmoitem.LiveMMOItem;
import net.Indyuce.mmoitems.api.item.util.ConfigItems;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.api.util.message.Message;
import net.Indyuce.mmoitems.stat.data.UpgradeData;
import net.mmogroup.mmolib.MMOLib;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class UpgradingRecipe extends Recipe {
	private final ConfigMMOItem item;
	private final Ingredient ingredient;

	private static final Random random = new Random();

	public UpgradingRecipe(ConfigurationSection config) {
		super(config);

		/*
		 * load item being upgraded.
		 */
		item = new ConfigMMOItem(config.getConfigurationSection("item"));
		ingredient = new MMOItemIngredient(item);
	}

	public ConfigMMOItem getItem() {
		return item;
	}

	@Override
	public void whenUsed(PlayerData data, IngredientInventory inv, RecipeInfo uncastRecipe, CraftingStation station) {
		UpgradingRecipeInfo recipe = (UpgradingRecipeInfo) uncastRecipe;
		recipe.getUpgradeData().upgrade(recipe.getMMOItem());
		recipe.getUpgraded().setItemMeta(recipe.getMMOItem().newBuilder().build().getItemMeta());

		uncastRecipe.getRecipe().getTriggers().forEach(trigger -> trigger.whenCrafting(data));
		if(!data.isOnline()) return;
		Message.UPGRADE_SUCCESS.format(ChatColor.YELLOW, "#item#", MMOUtils.getDisplayName(recipe.getUpgraded())).send(data.getPlayer());
		data.getPlayer().playSound(data.getPlayer().getLocation(), station.getSound(), 1, 1);
	}

	@Override
	public boolean canUse(PlayerData data, IngredientInventory inv, RecipeInfo uncastRecipe, CraftingStation station) {

		/*
		 * try to find the item which is meant to be updated. null check is
		 * ugly, BUT it does halve calculations done because it does not calls
		 * two map lookups. it is not needed to check for the amount because
		 * only one item is upgraded.
		 */
		PlayerIngredient upgraded = inv.getIngredient(ingredient, IngredientLookupMode.IGNORE_ITEM_LEVEL);
		if (upgraded == null) {
			if(!data.isOnline()) return false;
			Message.NOT_HAVE_ITEM_UPGRADE.format(ChatColor.RED).send(data.getPlayer());
			data.getPlayer().playSound(data.getPlayer().getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 2);
			return false;
		}

		UpgradingRecipeInfo recipe = (UpgradingRecipeInfo) uncastRecipe;
		if (!(recipe.mmoitem = new LiveMMOItem(MMOLib.plugin.getVersion().getWrapper().getNBTItem(upgraded.getFirstItem()))).hasData(ItemStats.UPGRADE))
			return false;

		if (!(recipe.upgradeData = (UpgradeData) recipe.getMMOItem().getData(ItemStats.UPGRADE)).canLevelUp()) {
			if(!data.isOnline()) return false;
			Message.MAX_UPGRADES_HIT.format(ChatColor.RED).send(data.getPlayer());
			data.getPlayer().playSound(data.getPlayer().getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 2);
			return false;
		}

		if (random.nextDouble() > recipe.getUpgradeData().getSuccess()) {
			if (recipe.getUpgradeData().destroysOnFail())
				recipe.getUpgraded().setAmount(recipe.getUpgraded().getAmount() - 1);

			recipe.getIngredients().forEach(ingredient -> ingredient.getPlayerIngredient().reduceItem(ingredient.getIngredient().getAmount()));
			if(!data.isOnline()) return false;
			Message.UPGRADE_FAIL_STATION.format(ChatColor.RED).send(data.getPlayer());
			data.getPlayer().playSound(data.getPlayer().getLocation(), Sound.ENTITY_ITEM_BREAK, 1, 2);
			return false;
		}

		return true;
	}

	@Override
	public ItemStack display(RecipeInfo recipe) {
		return ConfigItems.UPGRADING_RECIPE_DISPLAY.newBuilder(recipe).build();
	}

	@Override
	public RecipeInfo getRecipeInfo(PlayerData data, IngredientInventory inv) {
		return new UpgradingRecipeInfo(this, data, inv);
	}

	public static class UpgradingRecipeInfo extends RecipeInfo {
		private LiveMMOItem mmoitem;
		private UpgradeData upgradeData;

		public UpgradingRecipeInfo(Recipe recipe, PlayerData data, IngredientInventory inv) {
			super(recipe, data, inv);
		}

		public UpgradeData getUpgradeData() {
			return upgradeData;
		}

		public LiveMMOItem getMMOItem() {
			return mmoitem;
		}

		public ItemStack getUpgraded() {
			return mmoitem.getNBT().getItem();
		}
	}
}
