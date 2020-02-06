package net.Indyuce.mmoitems.manager.recipe;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.MMORecipeChoice;
import net.Indyuce.mmoitems.api.Type;

/**
 * TODO
 * When Bukkit changes their 'RecipeChoice.ExactChoice' API
 * we can remove the suppressed warnings, but right now it works
 * despite being marked as deprecated. It is just a 
 */
public abstract class RecipeManager {
	protected List<Recipe> loadedRecipes = new ArrayList<>();
	protected Collection<NamespacedKey> keys = new ArrayList<>();
	
	public RecipeManager() { load(); }
	
	protected abstract void load();
	
	/**
	 * @deprecated Some day I want to get proper rid of the AWB
	 * but right now we don't want to force players to update
	 * their recipes right off the bat.
	 */
	protected void registerAdvancedWorkbenchRecipe(Type type, String id, FileConfiguration config) {
		MMOItems.plugin.getLogger().warning("Found deprecated adv. recipe for " + id + ". Converting it to the new system...");
		MMOItems.plugin.getLogger().warning("It is recommended to update your recipes!");
		
		NamespacedKey key = getRecipeKey(type, id, "advanced", "deprecated");
		ShapedRecipe recipe = new ShapedRecipe(key, MMOItems.plugin.getItems().getItem(type, id));
		recipe.shape("012", "345", "678");
		
		setIngredientOrAir(recipe, '0', config.getConfigurationSection(id + ".advanced-craft." + 0));
		setIngredientOrAir(recipe, '1', config.getConfigurationSection(id + ".advanced-craft." + 1));
		setIngredientOrAir(recipe, '2', config.getConfigurationSection(id + ".advanced-craft." + 2));
		setIngredientOrAir(recipe, '3', config.getConfigurationSection(id + ".advanced-craft." + 3));
		setIngredientOrAir(recipe, '4', config.getConfigurationSection(id + ".advanced-craft." + 4));
		setIngredientOrAir(recipe, '5', config.getConfigurationSection(id + ".advanced-craft." + 5));
		setIngredientOrAir(recipe, '6', config.getConfigurationSection(id + ".advanced-craft." + 6));
		setIngredientOrAir(recipe, '7', config.getConfigurationSection(id + ".advanced-craft." + 7));
		setIngredientOrAir(recipe, '8', config.getConfigurationSection(id + ".advanced-craft." + 8));
		
		loadedRecipes.add(recipe); keys.add(key);
	}
	
	// Just for convenience
	protected NamespacedKey getRecipeKey(Type t, String i, String type, String number) {
		return new NamespacedKey(MMOItems.plugin, "mmorecipe_" + type + "_" + t.getId() + "_" + i + "_" + number);
	}

	protected abstract void registerFurnaceRecipe(Type type, String id, RecipeInformation info, String number);
	protected abstract void registerBlastRecipe(Type type, String id, RecipeInformation info, String number);
	protected abstract void registerShapelessRecipe(Type type, String id, ConfigurationSection config, String number);
	protected abstract void shapedIngredient(ShapedRecipe recipe, char c, MMORecipeChoice rc);
	protected abstract void registerShapedRecipe(Type type, String id, List<String> list, String number);
	protected abstract void registerCampfireRecipe(Type type, String id, RecipeInformation info, String number);
	protected abstract void registerSmokerRecipe(Type type, String id, RecipeInformation info, String number);
	protected abstract void shapelessIngredient(ShapelessRecipe recipe, MMORecipeChoice rc);
	
	/**
	 * This method is purely for easily converting the AWB recipes.
	 * 
	 * @deprecated Some day I want to get proper rid of the AWB
	 * but right now we don't want to force players to update
	 * their recipes right off the bat.
	 */
	@Deprecated
	protected abstract void setIngredientOrAir(ShapedRecipe recipe, char character, ConfigurationSection c);
	
	// For adding the recipes to the book
	public Collection<NamespacedKey> getNamespacedKeys() {
		return keys;
	}
	
	public void reloadRecipes() {
		Bukkit.getScheduler().runTask(MMOItems.plugin, new Runnable() {
			@Override
			public void run() {
				Bukkit.resetRecipes();
				loadedRecipes.clear();
				keys.clear();
				load();
			}
		});
	}

	// For the reload command
	public int size() {
		return loadedRecipes.size();
	}

	class RecipeInformation {
		protected final MMORecipeChoice choice;
		protected final float exp;
		protected final int burnTime;
		
		protected RecipeInformation(ConfigurationSection config) {
			choice = MMORecipeChoice.getFromString(config.getString("item"));
			exp = (float) config.getDouble("exp", 0.35);
			burnTime = config.getInt("time", 200);
		}
	}
}
