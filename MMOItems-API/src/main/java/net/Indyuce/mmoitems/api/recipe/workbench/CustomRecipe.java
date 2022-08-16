package net.Indyuce.mmoitems.api.recipe.workbench;

import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.api.recipe.workbench.ingredients.AirIngredient;
import net.Indyuce.mmoitems.api.recipe.workbench.ingredients.WorkbenchIngredient;
import org.apache.commons.lang.Validate;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import javax.annotation.Nullable;
import java.util.*;
import java.util.Map.Entry;

public class CustomRecipe implements Comparable<CustomRecipe> {
	private final Type type;
	private final String id;
	private final boolean shapeless;
	private final Map<Integer, WorkbenchIngredient> ingredients = new HashMap<>(9);
	private Permission permission;

	public CustomRecipe(Type type, String id, List<String> recipe, boolean isShapeless) {
		this.shapeless = isShapeless;
		this.type = type;
		this.id = id;

		if (shapeless) {
			Validate.isTrue(recipe.size() == 9, "Invalid shapeless recipe");
			for (int i = 0; i < 9; i++) {
				WorkbenchIngredient ingredient = MMOItems.plugin.getRecipes().getWorkbenchIngredient(recipe.get(i));
				// Only add AirIngredients if the amount system is enabled
				if (MMOItems.plugin.getRecipes().isAmounts() || !(ingredient instanceof AirIngredient))
					ingredients.put(i, ingredient);
			}
			return;
		}

		Validate.isTrue(recipe.size() == 3, "Invalid shaped recipe");
		for (int i = 0; i < 9; i++) {
			List<String> line = Arrays.asList(recipe.get(i / 3).split(" "));
			while (line.size() < 3)
				line.add("AIR");

			WorkbenchIngredient ingredient = MMOItems.plugin.getRecipes().getWorkbenchIngredient(line.get(i % 3));
			if (!(ingredient instanceof AirIngredient))
				ingredients.put(i, ingredient);
		}
	}

	public Set<Entry<Integer, WorkbenchIngredient>> getIngredients() {
		return ingredients.entrySet();
	}

	public boolean isOneRow() {
		for (int value : ingredients.keySet())
			if (value > 2)
				return false;
		return true;
	}

	public boolean isTwoRows() {
		for (int value : ingredients.keySet())
			if (value > 5)
				return false;
		return true;
	}

	public boolean fitsPlayerCrafting() {
		for (int value : ingredients.keySet())
			if (value > 4 || value == 2)
				return false;
		return true;
	}

	public boolean isEmpty() {
		return ingredients.isEmpty();
	}

	public boolean isShapeless() {
		return shapeless;
	}

	public Type getType() {
		return type;
	}

	public String getId() {
		return id;
	}

	public boolean checkPermission(Player player) {
		return permission == null || player.hasPermission(permission);
	}

	public ItemStack getResult(@Nullable Player p) {
		MMOItem mmo = p == null ? MMOItems.plugin.getMMOItem(type, id)
				: MMOItems.plugin.getMMOItem(type, id, PlayerData.get(p));
		ItemStack stack = mmo.newBuilder().build();
		/*if (mmo.hasData(ItemStats.CRAFT_AMOUNT))
			stack.setAmount((int) ((DoubleData) mmo.getData(ItemStats.CRAFT_AMOUNT)).getValue());*/
		if (mmo.hasData(ItemStats.CRAFT_PERMISSION))
			permission = new Permission(mmo.getData(ItemStats.CRAFT_PERMISSION).toString(), PermissionDefault.FALSE);
		return stack;
	}

	@Override
	public int compareTo(CustomRecipe o) {
		return Boolean.compare(shapeless, o.shapeless);
	}

	public Recipe asBukkit(NamespacedKey key) {
		Recipe recipe;
		if (shapeless) {
			org.bukkit.inventory.ShapelessRecipe r = new org.bukkit.inventory.ShapelessRecipe(key, getResult(null));
			for (WorkbenchIngredient ingredient : ingredients.values())
				if (!(ingredient instanceof AirIngredient))
					r.addIngredient(ingredient.toBukkit());
			recipe = r;
		} else {
			org.bukkit.inventory.ShapedRecipe r = new org.bukkit.inventory.ShapedRecipe(key, getResult(null));
			char[] characters = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I'};
			List<Integer> list = new ArrayList<>(ingredients.keySet());
			StringBuilder firstRow = new StringBuilder();
			firstRow.append(list.contains(0) ? "A" : " ");
			firstRow.append(list.contains(1) ? "B" : " ");
			firstRow.append(list.contains(2) ? "C" : " ");
			if(!isOneRow()) {
				StringBuilder secondRow = new StringBuilder();
				secondRow.append(list.contains(3) ? "D" : " ");
				secondRow.append(list.contains(4) ? "E" : " ");
				secondRow.append(list.contains(5) ? "F" : " ");
				if(!isTwoRows()) {
					r.shape(firstRow.toString(), secondRow.toString(),
						(list.contains(6) ? "G" : " ") +
						(list.contains(7) ? "H" : " ") +
						(list.contains(8) ? "I" : " "));
				}
				else r.shape(firstRow.toString(), secondRow.toString());
			}
			else r.shape(firstRow.toString());

			for (Entry<Integer, WorkbenchIngredient> ingredient : getIngredients()) {
				if (ingredient.getValue() instanceof AirIngredient) continue;
				char c = characters[ingredient.getKey()];
				r.setIngredient(c, ingredient.getValue().toBukkit());
			}
			
			recipe = r;
		}
		return recipe;
	}
}
