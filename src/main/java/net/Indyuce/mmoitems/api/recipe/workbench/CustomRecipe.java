package net.Indyuce.mmoitems.api.recipe.workbench;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.recipe.workbench.ingredients.AirIngredient;
import net.Indyuce.mmoitems.api.recipe.workbench.ingredients.WorkbenchIngredient;
import net.mmogroup.mmolib.api.item.NBTItem;

public class CustomRecipe implements Comparable<CustomRecipe> {
	private final boolean shapeless;
	private final ItemStack output;
	private final Map<Integer, WorkbenchIngredient> ingredients = new HashMap<>(9);

	public CustomRecipe(NBTItem output, List<String> recipe, boolean isShapeless) {
		this.shapeless = isShapeless;
		this.output = output.toItem();
		if (output.hasTag("MMOITEMS_CRAFTED_AMOUNT"))
			this.output.setAmount(output.getInteger("MMOITEMS_CRAFTED_AMOUNT"));
		
		if (shapeless) {
			Validate.isTrue(recipe.size() >= 9, "Recipe does not contain 9 ingredients");
			for (int i = 0; i < 9; i++) {
				ItemStack stack = MMOItems.plugin.parseStack(recipe.get(i));
				if (stack == null || stack.getType() == Material.AIR)
					continue;
				ingredients.put(i, WorkbenchIngredient.getAutomatically(stack));
			}
		}
		else {
			for (int i = 0; i < 9; i++) {
				List<String> line = Arrays.asList(recipe.get(i / 3).split("\\ "));
				while (line.size() < 3)
					line.add("AIR");

				ItemStack stack = MMOItems.plugin.parseStack(line.get(i % 3));
				if (stack == null || stack.getType() == Material.AIR)
					ingredients.put(i, new AirIngredient());
				else ingredients.put(i, WorkbenchIngredient.getAutomatically(stack));
			}
		}
	}

	public Set<Entry<Integer, WorkbenchIngredient>> getIngredients() {
		return ingredients.entrySet();
	}

	public boolean fitsPlayerCrafting() {
		boolean check = true;
		for (int value : ingredients.keySet())
			if (value > 3) {
				check = false;
				break;
			}
		return check;
	}
	
	public boolean isEmpty() {
		return ingredients.isEmpty();
	}
	
	public boolean isShapeless() {
		return shapeless;
	}

	public ItemStack getResult() {
		return output;
	}

	@Override
	public int compareTo(CustomRecipe o) {
		return Boolean.compare(shapeless, o.shapeless);
	}
}
