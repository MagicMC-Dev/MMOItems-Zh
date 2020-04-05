package net.Indyuce.mmoitems.api.crafting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.crafting.recipe.CraftingRecipe;
import net.Indyuce.mmoitems.api.crafting.recipe.Recipe;
import net.Indyuce.mmoitems.api.crafting.recipe.Recipe.RecipeOption;
import net.Indyuce.mmoitems.api.crafting.recipe.RecipeInfo;
import net.Indyuce.mmoitems.api.crafting.recipe.UpgradingRecipe;
import net.Indyuce.mmoitems.api.player.PlayerData;

public class CraftingStation {
	private final String id, name;
	private final StationItemOptions itemOptions;
	private final int maxQueueSize;
	private final Map<String, Recipe> recipes = new LinkedHashMap<>();

	public CraftingStation(String id, FileConfiguration config) {
		this.id = id.toLowerCase().replace("_", "-").replace(" ", "-");
		this.name = ChatColor.translateAlternateColorCodes('&', config.getString("name"));

		for (String key : config.getConfigurationSection("recipes").getKeys(false))
			try {
				registerRecipe(loadRecipe(config.getConfigurationSection("recipes." + key)));
			} catch (IllegalArgumentException exception) {
				MMOItems.plugin.getLogger().log(Level.INFO, "An issue occured registering recipe '" + key + "' from crafting station '" + id + "': " + exception.getMessage());
			}

		itemOptions = new StationItemOptions(config.getConfigurationSection("items"));
		maxQueueSize = Math.max(1, Math.min(config.getInt("max-queue-size"), 64));
	}

	public CraftingStation(String id, String name, StationItemOptions itemOptions, int maxQueueSize) {
		Validate.notNull(id, "Crafting station ID must not be null");
		Validate.notNull(name, "Crafting station name must not be null");

		this.id = id.toLowerCase().replace("_", "-").replace(" ", "-");
		this.name = ChatColor.translateAlternateColorCodes('&', name);
		this.itemOptions = itemOptions;
		this.maxQueueSize = maxQueueSize;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public Collection<Recipe> getRecipes() {
		return recipes.values();
	}

	public boolean hasRecipe(String id) {
		return recipes.containsKey(id);
	}

	public Recipe getRecipe(String id) {
		return recipes.get(id);
	}

	public int getMaxQueueSize() {
		return maxQueueSize;
	}

	public List<RecipeInfo> getAvailableRecipes(PlayerData data, IngredientInventory inv) {
		List<RecipeInfo> infos = new ArrayList<>();

		for (Recipe recipe : getRecipes()) {
			RecipeInfo info = recipe.getRecipeInfo(data, inv);
			if (info.isUnlocked() || !info.getRecipe().getOption(RecipeOption.HIDE_WHEN_LOCKED))
				infos.add(info);
		}

		return infos;
	}

	public StationItemOptions getItemOptions() {
		return itemOptions;
	}

	public void registerRecipe(Recipe recipe) {
		recipes.put(recipe.getId(), recipe);
	}

	public int getMaxPage() {
		return Math.max(1, (int) Math.ceil((double) recipes.size() / 14));
	}

	/*
	 * find type of crafting recipe based on section. there is no 'type' recipe
	 * parameter because old files would be out of date, instead just looks for
	 * a parameter of the crafting recipe which is 'output'
	 */
	private Recipe loadRecipe(ConfigurationSection config) {
		return config.contains("output") ? new CraftingRecipe(config) : new UpgradingRecipe(config);
	}
}
