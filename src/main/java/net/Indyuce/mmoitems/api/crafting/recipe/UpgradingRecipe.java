package net.Indyuce.mmoitems.api.crafting.recipe;

import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.crafting.ConfigMMOItem;
import net.Indyuce.mmoitems.api.crafting.CraftingStation;
import net.Indyuce.mmoitems.api.crafting.IngredientInventory;
import net.Indyuce.mmoitems.api.crafting.ingredient.Ingredient;
import net.Indyuce.mmoitems.api.crafting.ingredient.MMOItemIngredient;
import net.Indyuce.mmoitems.api.item.MMOItem;
import net.Indyuce.mmoitems.api.item.plugin.ConfigItem;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.api.util.message.Message;
import net.Indyuce.mmoitems.stat.Upgrade_Stat.UpgradeData;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.mmogroup.mmolib.MMOLib;

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
		Message.UPGRADE_SUCCESS.format(ChatColor.YELLOW, "#item#", MMOUtils.getDisplayName(recipe.getUpgraded())).send(data.getPlayer());
		data.getPlayer().playSound(data.getPlayer().getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
	}

	@Override
	public boolean canUse(PlayerData data, IngredientInventory inv, RecipeInfo uncastRecipe, CraftingStation station) {

		if (!inv.hasIngredient(ingredient)) {
			Message.NOT_HAVE_ITEM_UPGRADE.format(ChatColor.RED).send(data.getPlayer());
			data.getPlayer().playSound(data.getPlayer().getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 2);
			return false;
		}

		UpgradingRecipeInfo recipe = (UpgradingRecipeInfo) uncastRecipe;
		if (!(recipe.mmoitem = new MMOItem(MMOLib.plugin.getNMS().getNBTItem(inv.getIngredient(ingredient, true).getFirstItem()))).hasData(ItemStat.UPGRADE))
			return false;

		if (!(recipe.upgradeData = (UpgradeData) recipe.getMMOItem().getData(ItemStat.UPGRADE)).canLevelUp()) {
			Message.MAX_UPGRADES_HIT.format(ChatColor.RED).send(data.getPlayer());
			data.getPlayer().playSound(data.getPlayer().getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 2);
			return false;
		}

		if (random.nextDouble() > recipe.getUpgradeData().getSuccess()) {
			Message.UPGRADE_FAIL_STATION.format(ChatColor.RED).send(data.getPlayer());
			if (recipe.getUpgradeData().destroysOnFail())
				recipe.getUpgraded().setAmount(recipe.getUpgraded().getAmount() - 1);
			
			recipe.getIngredients().forEach(ingredient -> ingredient.getPlayerIngredient().reduceItem(ingredient.getIngredient().getAmount()));
			data.getPlayer().playSound(data.getPlayer().getLocation(), Sound.ENTITY_ITEM_BREAK, 1, 2);
			return false;
		}

		return true;
	}

	@Override
	public ItemStack display(RecipeInfo recipe) {
		return ConfigItem.UPGRADING_RECIPE_DISPLAY.newBuilder(recipe).build();
	}

	@Override
	public RecipeInfo getRecipeInfo(PlayerData data, IngredientInventory inv) {
		return new UpgradingRecipeInfo(this, data, inv);
	}

	public class UpgradingRecipeInfo extends RecipeInfo {
		private MMOItem mmoitem;
		private UpgradeData upgradeData;

		public UpgradingRecipeInfo(Recipe recipe, PlayerData data, IngredientInventory inv) {
			super(recipe, data, inv);
		}

		public UpgradeData getUpgradeData() {
			return upgradeData;
		}

		public MMOItem getMMOItem() {
			return mmoitem;
		}

		public ItemStack getUpgraded() {
			return mmoitem.getNBTItem().getItem();
		}
	}
}
