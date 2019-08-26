package net.Indyuce.mmoitems.manager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.AdvancedRecipe;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.item.NBTItem;

public class RecipeManager {

	/*
	 * recipes are parsed into a string. they are saved both in a map for
	 * quicker access when checking for patterns - but another map also saves
	 * parsed recipes in a map to easily access items depending on their item
	 * type (for the recipe list)
	 */
	private Map<String, AdvancedRecipe> recipes = new HashMap<>();
	private Map<Type, List<AdvancedRecipe>> types = new HashMap<>();

	private List<AdvancedRecipe> recipeList = new ArrayList<>();
	private List<Type> availableTypes = new ArrayList<>();

	private final String argSeparator = ":";
	private final String itemSeparator = "|";

	public int[] recipeSlots = { 3, 4, 5, 12, 13, 14, 21, 22, 23 };

	public RecipeManager() {
		loadRecipes();

		if (!MMOItems.plugin.getConfig().getBoolean("disable-craftings.vanilla"))
			for (Type type : MMOItems.plugin.getTypes().getAll()) {
				FileConfiguration config = type.getConfigFile().getConfig();
				idLoop: for (String path : config.getKeys(false)) {

					// initialize item so it is not calculated twice
					ItemStack item = config.getConfigurationSection(path).contains("craft") || config.getConfigurationSection(path).contains("shapeless-craft") || config.getConfigurationSection(path).contains("furnace-craft") ? MMOItems.plugin.getItems().getItem(type, path) : null;

					// crafting recipe
					if (config.getConfigurationSection(path).contains("craft")) {
						if (item == null || item.getType() == Material.AIR) {
							MMOItems.plugin.getLogger().log(Level.WARNING, "Could not register the recipe of " + path + " (not a valid item)");
							continue;
						}

						ShapedRecipe recipe = new ShapedRecipe(new NamespacedKey(MMOItems.plugin, "mmoitems_shaped_" + path.toLowerCase()), item);
						recipe.shape(new String[] { "abc", "def", "ghi" });
						char[] chars = new char[] { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i' };
						List<String> list = config.getStringList(path + ".craft");
						if (list.size() != 3) {
							MMOItems.plugin.getLogger().log(Level.WARNING, "Could not register the recipe of " + path + " (format error)");
							continue idLoop;
						}

						// prevent any empty crafting recipe to apply
						if (list.equals(Arrays.asList(new String[] { "AIR AIR AIR", "AIR AIR AIR", "AIR AIR AIR" })))
							continue;

						for (int j = 0; j < 9; j++) {
							char c = chars[j];
							List<String> line = Arrays.asList(list.get(j / 3).split("\\ "));
							if (line.size() < 3) {
								MMOItems.plugin.getLogger().log(Level.WARNING, "Could not register the recipe of " + path + " (format error)");
								continue idLoop;
							}

							String s = line.get(j % 3);
							Material material = null;
							try {
								material = Material.valueOf(s.replace("-", "_").toUpperCase());
							} catch (Exception e1) {
								MMOItems.plugin.getLogger().log(Level.WARNING, "Could not register the recipe of " + path + " (can't read material from " + s + ")");
								continue idLoop;
							}

							if (material != Material.AIR)
								recipe.setIngredient(c, material);
						}

						Bukkit.addRecipe(recipe);
					}

					// shapeless recipe
					if (config.getConfigurationSection(path).contains("shapeless-craft")) {
						if (item == null || item.getType() == Material.AIR) {
							MMOItems.plugin.getLogger().log(Level.WARNING, "Could not register the recipe of " + path + " (not a valid item)");
							continue;
						}

						boolean check = false;
						ShapelessRecipe recipe = new ShapelessRecipe(new NamespacedKey(MMOItems.plugin, "mmoitems_shapeless_" + path.toLowerCase()), item);
						for (String ingredient : config.getStringList(path + ".shapeless-craft")) {
							String format = ingredient.toUpperCase().replace(" ", "_").replace("-", "_");
							Material material = null;
							try {
								material = Material.valueOf(format);
							} catch (Exception e1) {
								MMOItems.plugin.getLogger().log(Level.WARNING, "Could not register the recipe of " + path + " (can't read material from " + format + ")");
								continue idLoop;
							}

							if (material != Material.AIR) {
								check = true;
								recipe.addIngredient(material);
							}
						}
						if (check)
							Bukkit.addRecipe(recipe);
					}

					// furnace crafting recipe
					if (config.getConfigurationSection(path).contains("furnace-craft")) {
						if (item == null || item.getType() == Material.AIR) {
							MMOItems.plugin.getLogger().log(Level.WARNING, "Could not register the recipe of " + path + " (not a valid item)");
							continue;
						}

						Material material = null;
						String format = config.getString(path + ".furnace-craft.input");
						if (format == null)
							continue;

						format = format.toUpperCase().replace(" ", "_").replace("-", "_");
						try {
							material = Material.valueOf(format);
						} catch (Exception e1) {
							MMOItems.plugin.getLogger().log(Level.WARNING, "Could not register the recipe of " + path + " (can't read material from " + format + ")");
							continue idLoop;
						}

						if (material != Material.AIR) {
							float exp = (float) config.getDouble(path + ".furnace-craft.exp");
							int cook = config.getInt(path + ".furnace-craft.cook");
							cook = cook == 0 ? 80 : cook;
							Bukkit.getServer().addRecipe(MMOItems.plugin.getVersion().getVersionWrapper().getFurnaceRecipe(path, item, material, exp, cook));
						}
					}
				}
			}
	}

	public void loadRecipes() {
		if (MMOItems.plugin.getConfig().getBoolean("disable-craftings.advanced"))
			return;

		recipeList.clear();
		types.clear();
		recipes.clear();

		for (Type type : MMOItems.plugin.getTypes().getAll()) {
			FileConfiguration config = type.getConfigFile().getConfig();
			List<AdvancedRecipe> recipeList = new ArrayList<>();

			itemLoop: for (String id : config.getKeys(false)) {
				if (config.getConfigurationSection(id).contains("advanced-craft")) {

					AdvancedRecipe advancedRecipe = new AdvancedRecipe(type, id);
					String parsedRecipe = "";

					for (int j = 0; j < 9; j++) {
						if (!config.getConfigurationSection(id + ".advanced-craft").contains("" + j)) {
							MMOItems.plugin.getLogger().log(Level.WARNING, id.toUpperCase() + " (" + type.getName() + ") is missing ingredient n" + (j + 1));
							continue itemLoop;
						}

						// type, id
						if (config.getConfigurationSection(id + ".advanced-craft." + j).contains("id")) {
							String type1 = config.getString(id + ".advanced-craft." + j + ".type");
							String id1 = config.getString(id + ".advanced-craft." + j + ".id");
							try {
								String itemFormat = MMOItems.plugin.getTypes().get(type1).getId() + argSeparator + id1;
								parsedRecipe += (parsedRecipe.length() > 0 ? itemSeparator : "") + itemFormat;
								advancedRecipe.setAmount(j, config.getInt(id + ".advanced-craft." + j + ".amount"));
							} catch (Exception e) {
								MMOItems.plugin.getLogger().log(Level.WARNING, id.toUpperCase() + " (" + type.getName() + ") - " + type1 + " is not a valid item type");
								continue itemLoop;
							}
							continue;
						}

						// material, name
						String materialParse = config.getString(id + ".advanced-craft." + j + ".material").toUpperCase().replace(" ", "_").replace("-", "_");
						try {
							String itemFormat = Material.valueOf(materialParse).name() + argSeparator + config.getString(id + ".advanced-craft." + j + ".name") + argSeparator + "MN";
							if (itemFormat.startsWith("AIR:")) {
								parsedRecipe += (parsedRecipe.length() > 0 ? itemSeparator : "") + "AIR";
								advancedRecipe.setAmount(j, 0);
								continue;
							}
							parsedRecipe += (parsedRecipe.length() > 0 ? itemSeparator : "") + itemFormat;
							advancedRecipe.setAmount(j, config.getInt(id + ".advanced-craft." + j + ".amount"));
						} catch (Exception e) {
							MMOItems.plugin.getLogger().log(Level.WARNING, id.toUpperCase() + " (" + type.getName() + ") - " +materialParse + " is not a valid material");
							continue itemLoop;
						}
					}

					ItemStack preview = MMOItems.plugin.getItems().getItem(type, id);
					if (preview == null || preview.getType() == Material.AIR)
						continue;

					// recipe is now successfully added.
					advancedRecipe.setPreviewItem(preview);
					advancedRecipe.setPermission(config.getString(id + ".advanced-craft-permission"));
					advancedRecipe.setParsed(parsedRecipe);

					recipes.put(type.getId() + "." + id, advancedRecipe);
					recipeList.add(advancedRecipe);
				}
			}

			if (!recipeList.isEmpty())
				types.put(type, recipeList);
		}

		recipeList = new ArrayList<>(recipes.values());
		availableTypes = new ArrayList<>(types.keySet());
	}

	public AdvancedRecipe getData(Type type, String id) {
		return recipes.get(type.getId() + "." + id);
	}

	public List<AdvancedRecipe> getRecipes() {
		return recipeList;
	}

	public List<Type> getAvailableTypes() {
		return availableTypes;
	}

	public List<AdvancedRecipe> getTypeRecipes(Type type) {
		return types.containsKey(type) ? types.get(type) : new ArrayList<>();
	}

	/*
	 * returns the current recipe of the opened adv workbench. it can return
	 * null either if the player does not have enough permissions to see the
	 * crafting recipe, or if there is simply no corresponding adv recipe
	 */
	public AdvancedRecipe getCurrentRecipe(Player player, Inventory inv) {

		// check for valid pattern
		String parsedRecipe = getRecipeFormat(inv);
		AdvancedRecipe currentRecipe = null;

		for (AdvancedRecipe recipe : getRecipes())
			if (recipe.isParsed(parsedRecipe)) {
				currentRecipe = recipe;
				break;
			}

		// check for null
		if (currentRecipe == null)
			return null;

		// check for permission
		if (!currentRecipe.hasPermission(player))
			return null;

		// check for amounts
		for (int j = 0; j < 9; j++) {
			ItemStack currentItem = inv.getItem(recipeSlots[j]);
			int current = currentItem == null ? 0 : currentItem.getAmount();
			int needed = currentRecipe.getAmount(j);
			if (current < needed)
				return null;
		}

		return currentRecipe;
	}

	private String getRecipeFormat(Inventory inv) {
		String recipeFormat = "";
		for (int j : new int[] { 3, 4, 5, 12, 13, 14, 21, 22, 23 }) {
			if (inv.getItem(j) == null || inv.getItem(j).getType() == Material.AIR) {
				recipeFormat += (recipeFormat.length() < 1 ? "" : itemSeparator) + "AIR";
				continue;
			}

			NBTItem item = MMOItems.plugin.getNMS().getNBTItem(inv.getItem(j));

			// type, id
			String id1 = item.getString("MMOITEMS_ITEM_ID");
			if (id1 != null && !id1.equals("")) {
				String type1 = item.getString("MMOITEMS_ITEM_TYPE");
				String itemFormat = type1 + argSeparator + id1;
				recipeFormat += (recipeFormat.length() < 1 ? "" : itemSeparator) + itemFormat;
				continue;
			}

			// material, name
			String name = item.getItem().getItemMeta().hasDisplayName() ? item.getItem().getItemMeta().getDisplayName() : "";
			String itemFormat = item.getItem().getType().name() + argSeparator + name + argSeparator + "MN";
			recipeFormat += (recipeFormat.length() < 1 ? "" : itemSeparator) + itemFormat;
		}
		return recipeFormat;
	}
}
