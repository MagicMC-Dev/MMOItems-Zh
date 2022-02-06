package net.Indyuce.mmoitems.api.crafting;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.util.PostLoadObject;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.crafting.ingredient.inventory.IngredientInventory;
import net.Indyuce.mmoitems.api.crafting.recipe.CheckedRecipe;
import net.Indyuce.mmoitems.api.crafting.recipe.CraftingRecipe;
import net.Indyuce.mmoitems.api.crafting.recipe.Recipe;
import net.Indyuce.mmoitems.api.crafting.recipe.Recipe.RecipeOption;
import net.Indyuce.mmoitems.api.crafting.recipe.UpgradingRecipe;
import net.Indyuce.mmoitems.api.player.PlayerData;
import org.apache.commons.lang.Validate;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;
import java.util.logging.Level;

public class CraftingStation extends PostLoadObject {
	private final String id, name;
	private final Layout layout;
	private final Sound sound;
	private final StationItemOptions itemOptions;
	private final int maxQueueSize;
	private final Map<String, Recipe> recipes = new LinkedHashMap<>();

	private CraftingStation parent;

	public CraftingStation(String id, FileConfiguration config) {
		super(config);

		this.id = id.toLowerCase().replace("_", "-").replace(" ", "-");
		this.name = MythicLib.plugin.parseColors(config.getString("name", "Station"));
		this.layout = MMOItems.plugin.getLayouts().getLayout(config.getString("layout", "default"));
		this.sound = Sound.valueOf(config.getString("sound", "ENTITY_EXPERIENCE_ORB_PICKUP").toUpperCase());

		for (String key : config.getConfigurationSection("recipes").getKeys(false))
			try {
				registerRecipe(loadRecipe(config.getConfigurationSection("recipes." + key)));
			} catch (IllegalArgumentException exception) {
				MMOItems.plugin.getLogger().log(Level.INFO,
						"An issue occurred registering recipe '" + key + "' from crafting station '" + id + "': " + exception.getMessage());
			}

		itemOptions = new StationItemOptions(config.getConfigurationSection("items"));
		maxQueueSize = Math.max(1, Math.min(config.getInt("max-queue-size"), 64));
	}

	public CraftingStation(String id, String name, Layout layout, Sound sound, StationItemOptions itemOptions, int maxQueueSize, CraftingStation parent) {
		super(null);

		Validate.notNull(id, "Crafting station ID must not be null");
		Validate.notNull(name, "Crafting station name must not be null");
		Validate.notNull(sound, "Crafting station sound must not be null");

		this.id = id.toLowerCase().replace("_", "-").replace(" ", "-");
		this.name = MythicLib.plugin.parseColors(name);
		this.layout = layout;
		this.sound = sound;
		this.itemOptions = itemOptions;
		this.maxQueueSize = maxQueueSize;
		this.parent = parent;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public Layout getLayout() {
		return layout;
	}

	public Sound getSound() {
		return sound;
	}

	public CraftingStation getParent() {
		return parent;
	}

	public Collection<Recipe> getRecipes() {
		if (parent == null)
			return recipes.values();

		// collect recipes from station inheritance tree
		List<Recipe> collected = new ArrayList<>(recipes.values());
		CraftingStation next = parent;
		while (next != null) {
			collected.addAll(next.getRecipes());
			next = next.getParent();
		}

		return collected;
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

	public List<CheckedRecipe> getAvailableRecipes(PlayerData data, IngredientInventory inv) {
		List<CheckedRecipe> infos = new ArrayList<>();

		for (Recipe recipe : getRecipes()) {
			CheckedRecipe info = recipe.evaluateRecipe(data, inv);
			if ((info.areConditionsMet() || !info.getRecipe().hasOption(RecipeOption.HIDE_WHEN_LOCKED))
					&& (info.allIngredientsHad() || !info.getRecipe().hasOption(RecipeOption.HIDE_WHEN_NO_INGREDIENTS)))
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
		return Math.max(1, (int) Math.ceil((double) recipes.size() / getLayout().getRecipeSlots().size()));
	}

	@Override
	protected void whenPostLoaded(ConfigurationSection config) {
		if (config.contains("parent")) {
			String id = config.getString("parent").toLowerCase().replace(" ", "-").replace("_", "-");
			Validate.isTrue(!id.equals(this.id), "Station cannot use itself as parent");
			Validate.isTrue(MMOItems.plugin.getCrafting().hasStation(id), "Could not find parent station with ID '" + id + "'");
			parent = MMOItems.plugin.getCrafting().getStation(id);
		}
	}

	/*
	 * find type of crafting recipe based on section. there is no 'type' recipe
	 * parameter because old files would be out of date, instead just looks for
	 * a parameter of the crafting recipe which is 'output'
	 */
	private Recipe loadRecipe(ConfigurationSection config) throws IllegalArgumentException {
		return config.contains("output") ? new CraftingRecipe(config) : new UpgradingRecipe(config);
	}
}
