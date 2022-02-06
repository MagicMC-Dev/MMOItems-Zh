package net.Indyuce.mmoitems.api.item.util.crafting;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.api.util.LegacyComponent;
import io.lumine.mythic.utils.adventure.text.Component;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.crafting.ConditionalDisplay;
import net.Indyuce.mmoitems.api.crafting.condition.CheckedCondition;
import net.Indyuce.mmoitems.api.crafting.recipe.CheckedRecipe;
import net.Indyuce.mmoitems.api.crafting.recipe.CraftingRecipe;
import net.Indyuce.mmoitems.api.item.util.ConfigItem;
import net.Indyuce.mmoitems.api.util.message.Message;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

public class CraftingRecipeDisplay extends ConfigItem {
	private static final DecimalFormat craftingTimeFormat = new DecimalFormat("0.#");

	public CraftingRecipeDisplay() {
		super("CRAFTING_RECIPE_DISPLAY", Material.BARRIER, "&a&lCraft&f #name#", "{conditions}", "{conditions}&8Conditions:", "{crafting_time}",
				"{crafting_time}&7Crafting Time: &c#crafting-time#&7s", "", "&8Ingredients:", "#ingredients#", "", "&eLeft-Click to craft!",
				"&eRight-Click to preview!");
	}

	public ItemBuilder newBuilder(CheckedRecipe recipe) {
		return new ItemBuilder(recipe);
	}

	/*
	 * allows to build an unidentified item based on the given NBTItem.
	 */
	public class ItemBuilder {
		private final CheckedRecipe recipe;
		private final CraftingRecipe craftingRecipe;

		private final String name = getName();
		private final List<String> lore = new ArrayList<>(getLore());

		public ItemBuilder(CheckedRecipe recipe) {
			this.recipe = recipe;
			craftingRecipe = (CraftingRecipe) recipe.getRecipe();
		}

		public ItemStack build() {
			Map<String, String> replace = new HashMap<>();

			/*
			 * used to calculate the last index for conditions. if there are no
			 * conditions, just clean up all {conditions}, otherwise replace all
			 * {conditions} and display conditions at the last index
			 */
			int conditionsIndex = -1;

			for (ListIterator<String> iterator = lore.listIterator(); iterator.hasNext();) {
				int index = iterator.nextIndex();
				String str = iterator.next();

				/*
				 * crafting time
				 */
				if (str.startsWith("{crafting_time}")) {
					if (craftingRecipe.getCraftingTime() <= 0) {
						iterator.remove();
						continue;
					}

					replace.put(str, str.replace("{crafting_time}", "").replace("#crafting-time#",
							craftingTimeFormat.format(craftingRecipe.getCraftingTime())));
				}

				if (str.startsWith("{conditions}")) {
					conditionsIndex = index + 1;
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

			ItemStack item = craftingRecipe.getPreviewItemStack();
			int amount = craftingRecipe.getOutputAmount();

			if (amount > 64)
				lore.add(0, Message.STATION_BIG_STACK.format(ChatColor.GOLD, "#size#", String.valueOf(amount)).toString());
			else
				item.setAmount(amount);

			ItemMeta meta = item.getItemMeta();
			meta.addItemFlags(ItemFlag.values());
			item.setItemMeta(meta);

			NBTItem nbtItem = NBTItem.get(item);

			nbtItem.setDisplayNameComponent(LegacyComponent.parse(name.replace("#name#", (amount > 1 ? (ChatColor.WHITE + "" + amount + " x ") : "") + MMOUtils.getDisplayName(item))));

			List<Component> componentLore = new ArrayList<>();
			lore.forEach(line -> componentLore.add(LegacyComponent.parse(line)));
			nbtItem.setLoreComponents(componentLore);

			return nbtItem.addTag(new ItemTag("recipeId", craftingRecipe.getId())).toItem();
		}
	}
}
