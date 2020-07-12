package net.Indyuce.mmoitems.manager.recipe;

import java.util.List;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.FurnaceRecipe;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.recipe.workbench.CustomRecipe;

/** One day we'll get rid of 1.12 for real >:) */
public class RecipeManagerLegacy extends RecipeManager {
	public RecipeManagerLegacy() {
		reload();
	}

	@Override
	public void reload() {
		clearCustomRecipes();
		
		for (Type type : MMOItems.plugin.getTypes().getAll()) {
			FileConfiguration config = type.getConfigFile().getConfig();

			for (String id : config.getKeys(false))
				try {
					if (config.getConfigurationSection(id).contains("crafting")) {
						ConfigurationSection section = config.getConfigurationSection(id + ".crafting");

						if (section.contains("shaped"))
							section.getConfigurationSection("shaped").getKeys(false)
									.forEach(recipe -> registerShapedRecipe(type, id, section.getStringList("shaped." + recipe)));
						if (section.contains("shapeless"))
							section.getConfigurationSection("shapeless").getKeys(false)
									.forEach(recipe -> registerShapelessRecipe(type, id, section.getStringList("shapeless." + recipe)));
						if (section.contains("furnace"))
							section.getConfigurationSection("furnace").getKeys(false)
									.forEach(recipe -> registerFurnaceRecipe(type, id,
											new BurningRecipeInformation(section.getConfigurationSection("furnace." + recipe)), recipe));
					}
				} catch (IllegalArgumentException exception) {
					MMOItems.plugin.getLogger().log(Level.WARNING,
							"Could not load recipe of " + id + ": " + exception.getMessage());
				}
		}

		sortRecipes();
		Bukkit.getScheduler().runTask(MMOItems.plugin,
				() -> getLoadedRecipes().forEach(recipe -> Bukkit.addRecipe(recipe.getRecipe())));
	}

	@Override
	public void registerFurnaceRecipe(Type type, String id, BurningRecipeInformation info, String number) {
		if (!info.getChoice().isValid()) {
			MMOItems.plugin.getLogger().warning("Couldn't load furnace recipe for '" + type.getId() + "." + id + "'");
			return;
		}
		NamespacedKey key = getRecipeKey(type, id, "furnace", number);
		FurnaceRecipe recipe = new FurnaceRecipe(key, MMOItems.plugin.getItems().getItem(type, id),
				info.getChoice().getItem().getType(), info.getExp(), info.getBurnTime());
		registerRecipe(key, recipe);
	}

	@Override
	public void registerShapedRecipe(Type type, String id, List<String> list) {
		registerRecipe(new CustomRecipe(MMOItems.plugin.getItems().getMMOItem(type, id).newBuilder().buildNBT(), list, false));
	}

	@Override
	public void registerShapelessRecipe(Type type, String id, List<String> list) {
		registerRecipe(new CustomRecipe(MMOItems.plugin.getItems().getMMOItem(type, id).newBuilder().buildNBT(), list, true));
	}
}
