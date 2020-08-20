package net.Indyuce.mmoitems.api.recipe.workbench;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.api.recipe.workbench.ingredients.AirIngredient;
import net.Indyuce.mmoitems.api.recipe.workbench.ingredients.WorkbenchIngredient;
import net.Indyuce.mmoitems.stat.data.DoubleData;
import net.Indyuce.mmoitems.stat.type.ItemStat;

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
				if (!(ingredient instanceof AirIngredient))
					ingredients.put(i, ingredient);
			}
			return;
		}

		Validate.isTrue(recipe.size() == 3, "Invalid shaped recipe");
		for (int i = 0; i < 9; i++) {
			List<String> line = Arrays.asList(recipe.get(i / 3).split("\\ "));
			while (line.size() < 3)
				line.add("AIR");

			ingredients.put(i, MMOItems.plugin.getRecipes().getWorkbenchIngredient(line.get(i % 3)));
		}
	}

	public Set<Entry<Integer, WorkbenchIngredient>> getIngredients() {
		return ingredients.entrySet();
	}

	public boolean fitsPlayerCrafting() {
		for (int value : ingredients.keySet())
			if (value > 3)
				return false;
		return true;
	}

	public boolean isEmpty() {
		return ingredients.isEmpty();
	}

	public boolean isShapeless() {
		return shapeless;
	}

	public boolean checkPermission(Player player) {
		return permission == null || player.hasPermission(permission);
	}

	public ItemStack getResult(Player p) {
		PlayerData player = PlayerData.get(p);
		MMOItem mmo = MMOItems.plugin.getMMOItem(type, id, player);
		ItemStack stack = mmo.newBuilder().build();
		if (mmo.hasData(ItemStat.CRAFT_AMOUNT))
			stack.setAmount((int) ((DoubleData) mmo.getData(ItemStat.CRAFT_AMOUNT)).getValue());
		if (mmo.hasData(ItemStat.CRAFT_PERMISSION))
			permission = new Permission(mmo.getData(ItemStat.CRAFT_PERMISSION).toString(), PermissionDefault.FALSE);
		return stack;
	}

	@Override
	public int compareTo(CustomRecipe o) {
		return Boolean.compare(shapeless, o.shapeless);
	}
}
