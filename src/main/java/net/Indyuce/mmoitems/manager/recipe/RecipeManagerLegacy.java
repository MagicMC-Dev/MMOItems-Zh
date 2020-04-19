package net.Indyuce.mmoitems.manager.recipe;

import java.util.List;
import java.util.logging.Level;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.recipe.MMORecipeChoice;

/** One day I'll get rid of 1.12 for real >:) */
public class RecipeManagerLegacy extends RecipeManager {

	public RecipeManagerLegacy() {
		reload();
	}

	@Override
	@SuppressWarnings("deprecation")
	public void reload() {
		for (Type type : MMOItems.plugin.getTypes().getAll()) {
			FileConfiguration config = type.getConfigFile().getConfig();

			for (String id : config.getKeys(false))
				try {
					if (config.getConfigurationSection(id).contains("advanced-craft"))
						registerAdvancedWorkbenchRecipe(type, id, config);

					if (config.getConfigurationSection(id).contains("crafting")) {
						ConfigurationSection section = config.getConfigurationSection(id + ".crafting");

						if (section.contains("shaped"))
							section.getConfigurationSection("shaped").getKeys(false)
									.forEach(recipe -> registerShapedRecipe(type, id, section.getStringList("shaped." + recipe), recipe));
						if (section.contains("shapeless"))
							section.getConfigurationSection("shapeless").getKeys(false).forEach(
									recipe -> registerShapelessRecipe(type, id, section.getConfigurationSection("shapeless." + recipe), recipe));
						if (section.contains("furnace"))
							section.getConfigurationSection("furnace").getKeys(false).forEach(recipe -> registerFurnaceRecipe(type, id,
									new BurningRecipeInformation(section.getConfigurationSection("furnace." + recipe)), recipe));
					}
				} catch (IllegalArgumentException exception) {
					MMOItems.plugin.getLogger().log(Level.WARNING, "Could not load recipe of " + id + ": " + exception.getMessage());
				}
		}

		// registerCampfireRecipe(MMOItems.plugin.getItems().getItem(Type.SWORD,
		// "SILVER_SWORD"), new
		// RecipeChoice.ExactChoice(MMOItems.plugin.getItems().getItem(Type.get("MATERIAL"),
		// "SILVER_INGOT")));

		Bukkit.getScheduler().runTask(MMOItems.plugin, () ->

		getLoadedRecipes().forEach(recipe -> Bukkit.addRecipe(recipe.toBukkit())));
	}

	@Override
	public void registerFurnaceRecipe(Type type, String id, BurningRecipeInformation info, String number) {
		NamespacedKey key = getRecipeKey(type, id, "furnace", number);
		FurnaceRecipe recipe = new FurnaceRecipe(key, MMOItems.plugin.getItems().getItem(type, id), info.getChoice().getMaterial(), info.getExp(),
				info.getBurnTime());
		registerRecipe(key, recipe);
	}

	@Override
	public void registerShapedRecipe(Type type, String id, List<String> list, String number) {
		NamespacedKey key = getRecipeKey(type, id, "shaped", number);
		ShapedRecipe recipe = new ShapedRecipe(key, MMOItems.plugin.getItems().getItem(type, id));

		List<MMORecipeChoice> rcList = MMORecipeChoice.getFromShapedConfig(list);
		if (rcList == null)
			return;

		recipe.shape("ABC", "DEF", "GHI");

		shapedIngredient(recipe, 'A', rcList.get(0));
		shapedIngredient(recipe, 'B', rcList.get(1));
		shapedIngredient(recipe, 'C', rcList.get(2));
		shapedIngredient(recipe, 'D', rcList.get(3));
		shapedIngredient(recipe, 'E', rcList.get(4));
		shapedIngredient(recipe, 'F', rcList.get(5));
		shapedIngredient(recipe, 'G', rcList.get(6));
		shapedIngredient(recipe, 'H', rcList.get(7));
		shapedIngredient(recipe, 'I', rcList.get(8));

		registerRecipe(key, recipe);
	}

	@Override
	public void shapedIngredient(ShapedRecipe recipe, char c, MMORecipeChoice rc) {
		if (rc.isAir())
			recipe.setIngredient(c, Material.AIR);
		else
			recipe.setIngredient(c, rc.generateStack().getType());
	}

	@Override
	public void registerShapelessRecipe(Type type, String id, ConfigurationSection config, String number) {
		NamespacedKey key = getRecipeKey(type, id, "shapeless", number);
		ShapelessRecipe recipe = new ShapelessRecipe(key, MMOItems.plugin.getItems().getItem(type, id));

		for (int i = 1; i < 10; i++)
			if (config.contains("item" + i))
				shapelessIngredient(recipe, new MMORecipeChoice(config.getString("item" + i)));

		if (!recipe.getIngredientList().isEmpty())
			registerRecipe(key, recipe);
	}

	@Override
	public void shapelessIngredient(ShapelessRecipe recipe, MMORecipeChoice rc) {
		if (!rc.isAir())
			recipe.addIngredient(rc.getMaterial());
	}

	@Override
	public void setIngredientOrAir(ShapedRecipe recipe, char character, ConfigurationSection config) {
		if (config.contains("type")) {
			String typeFormat = config.getString("type").toUpperCase().replace("-", "_").replace(" ", "_");
			Validate.notNull(MMOItems.plugin.getTypes().has(typeFormat), "Could not find item type");

			String id = config.getString("id");
			Validate.notNull(id, "Could not find item ID");

			ItemStack item = MMOItems.plugin.getItems().getItem(MMOItems.plugin.getTypes().get(typeFormat), id);
			Validate.isTrue(item != null, "Could not load item with ID: " + id);

			item.setAmount(config.getInt("amount", 1));
			recipe.setIngredient(character, item.getType());

		} else if (config.contains("material")) {
			Material material = Material.valueOf(config.getString("material").toUpperCase().replace("-", "_").replace(" ", "_"));
			recipe.setIngredient(character, material);
		}
	}
}
