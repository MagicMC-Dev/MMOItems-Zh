package net.Indyuce.mmoitems.api.recipe.workbench;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.Material;
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
	private Permission perm = null;

	public CustomRecipe(Type type, String id, List<String> recipe, boolean isShapeless) {
		this.shapeless = isShapeless;
		this.type = type;
		this.id = id;

		if (shapeless) {
			if (recipe.size() != 9) {
				MMOItems.plugin.getLogger()
						.warning("Invalid shapeless recipe for '" + type.getId() + "." + id + "'");
				recipe = Arrays.asList("AIR", "AIR", "AIR", "AIR", "AIR", "AIR", "AIR", "AIR", "AIR");
			}
			for (int i = 0; i < 9; i++) {
				ItemStack stack = MMOItems.plugin.getRecipes().parseStack(recipe.get(i));
				if (stack != null && stack.getType() != Material.AIR)
					ingredients.put(i, WorkbenchIngredient.getAutomatically(stack));
			}
		} else {
			if (recipe.size() != 3) {
				MMOItems.plugin.getLogger()
						.warning("Invalid shaped recipe for '" + type.getId() + "." + id + "'");
				recipe = Arrays.asList("AIR AIR AIR", "AIR AIR AIR", "AIR AIR AIR");
			}
			for (int i = 0; i < 9; i++) {
				List<String> line = Arrays.asList(recipe.get(i / 3).split("\\ "));
				while (line.size() < 3)
					line.add("AIR");

				ItemStack stack = MMOItems.plugin.getRecipes().parseStack(line.get(i % 3));
				ingredients.put(i,
						stack == null || stack.getType() == Material.AIR ? new AirIngredient() : WorkbenchIngredient.getAutomatically(stack));
			}
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
	
	public boolean permCheck(Player player) {
		if(perm == null) return true;
		else return player.hasPermission(perm);
	}

	public ItemStack getResult(Player p) {
		PlayerData player = PlayerData.get(p);
		MMOItem mmo = MMOItems.plugin.getMMOItem(type, id, player);
		ItemStack stack = mmo.newBuilder().build();
		if(mmo.hasData(ItemStat.CRAFT_AMOUNT))
			stack.setAmount((int) ((DoubleData) mmo.getData(ItemStat.CRAFT_AMOUNT)).getValue());
		if(mmo.hasData(ItemStat.CRAFT_PERMISSION))
			perm = new Permission(mmo.getData(ItemStat.CRAFT_PERMISSION).toString(), PermissionDefault.FALSE);
		return stack;
	}

	@Override
	public int compareTo(CustomRecipe o) {
		return Boolean.compare(shapeless, o.shapeless);
	}
}
