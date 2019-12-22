package net.Indyuce.mmoitems.api.item.plugin.crafting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.crafting.condition.Condition.ConditionInfo;
import net.Indyuce.mmoitems.api.crafting.recipe.RecipeInfo;
import net.Indyuce.mmoitems.api.crafting.recipe.UpgradingRecipe;
import net.Indyuce.mmoitems.api.item.plugin.ConfigItem;
import net.mmogroup.mmolib.MMOLib;
import net.mmogroup.mmolib.api.item.ItemTag;

public class UpgradingRecipeDisplay extends ConfigItem {
	public UpgradingRecipeDisplay() {
		super("UPGRADING_RECIPE_DISPLAY", Material.BARRIER, "&e&lUpgrade&f #name#", "{conditions}", "#condition_class#", "#condition_level#", "#condition_profession#", "#condition_mana#", "#condition_stamina#", "#condition_food#", "", "&8Ingredients:", "#ingredients#", "", "&eLeft-Click to craft!", "&eRight-Click to preview!");
	}

	public ItemBuilder newBuilder(RecipeInfo recipe) {
		return new ItemBuilder(recipe);
	}

	/*
	 * allows to build an unidentified item based on the given NBTItem.
	 */
	public class ItemBuilder {
		private final RecipeInfo recipe;
		private final UpgradingRecipe upgradingRecipe;

		private String name = new String(getName());
		private List<String> lore = new ArrayList<>(getLore());

		public ItemBuilder(RecipeInfo recipe) {
			this.recipe = recipe;
			this.upgradingRecipe = (UpgradingRecipe) recipe.getRecipe();
		}

		public ItemStack build() {
			Map<String, String> replace = new HashMap<>();

			for (Iterator<String> iterator = lore.iterator(); iterator.hasNext();) {
				String str = iterator.next();


				if (str.equals("{conditions}")) {
					if (recipe.getConditions().size() == 0) {
						iterator.remove();
						continue;
					}

					replace.put(str, str.replace("{conditions}", ""));
				}

				/*
				 * load conditions
				 */
				if (str.startsWith("#condition_")) {
					String format = str.substring("#condition_".length(), str.length() - 1);
					ConditionInfo info = recipe.getCondition(format);
					if (info != null && info.getCondition().displays())
						replace.put(str, info.getCondition().formatDisplay(info.isMet() ? info.getCondition().getDisplay().getPositive() : info.getCondition().getDisplay().getNegative()));
					else
						iterator.remove();
				}
			}

			for (String key : replace.keySet())
				lore.set(lore.indexOf(key), replace.get(key));

			/*
			 * load ingredients. will register an error if the lore does not
			 * contain that line. users MUST display the ingredients somewhere.
			 */
			int index = lore.indexOf("#ingredients#");
			lore.remove(index);
			recipe.getIngredients().forEach(info -> lore.add(index, info.getIngredient().formatDisplay(info.isHad() ? info.getIngredient().getDisplay().getPositive() : info.getIngredient().getDisplay().getNegative())));

			/*
			 * apply color to lore
			 */
			for (int n = 0; n < lore.size(); n++)
				lore.set(n, ChatColor.translateAlternateColorCodes('&', lore.get(n)));

			ItemStack item = upgradingRecipe.getItem().getPreview();
			ItemMeta meta = item.getItemMeta();
			meta.addItemFlags(ItemFlag.values());
			meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name.replace("#name#", MMOUtils.getDisplayName(item))));
			meta.setLore(lore);
			item.setItemMeta(meta);

			return MMOLib.plugin.getNMS().getNBTItem(item).addTag(new ItemTag("recipeId", recipe.getRecipe().getId())).toItem();
		}
	}
}
