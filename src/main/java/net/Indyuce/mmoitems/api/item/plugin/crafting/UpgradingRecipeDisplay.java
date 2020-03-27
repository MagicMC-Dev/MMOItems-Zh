package net.Indyuce.mmoitems.api.item.plugin.crafting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.crafting.ConditionalDisplay;
import net.Indyuce.mmoitems.api.crafting.condition.Condition.CheckedCondition;
import net.Indyuce.mmoitems.api.crafting.recipe.RecipeInfo;
import net.Indyuce.mmoitems.api.crafting.recipe.UpgradingRecipe;
import net.Indyuce.mmoitems.api.item.plugin.ConfigItem;
import net.mmogroup.mmolib.MMOLib;
import net.mmogroup.mmolib.api.item.ItemTag;

public class UpgradingRecipeDisplay extends ConfigItem {
	public UpgradingRecipeDisplay() {
		super("UPGRADING_RECIPE_DISPLAY", Material.BARRIER, "&e&lUpgrade&f #name#", "{conditions}", "{conditions}&8Conditions:", "", "&8Ingredients:", "#ingredients#", "", "&eLeft-Click to craft!", "&eRight-Click to preview!");
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
			int conditionsIndex = -1;

			for (ListIterator<String> iterator = lore.listIterator(); iterator.hasNext();) {
				int index = iterator.nextIndex();
				String str = iterator.next();

				if (str.startsWith("{conditions}")) {
					conditionsIndex = index;
					if (recipe.getConditions().size() == 0)
						iterator.remove();
					else
						replace.put(str, str.replace("{conditions}", ""));
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
			recipe.getIngredients().forEach(info -> lore.add(index, info.format()));

			if (conditionsIndex >= 0)
				for (CheckedCondition condition : recipe.getConditions()) {
					ConditionalDisplay display = condition.getCondition().getDisplay();
					if (display != null)
						// ++ allows to sort displays in the same order as in
						// the config
						lore.add(conditionsIndex++, condition.format());
				}
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
