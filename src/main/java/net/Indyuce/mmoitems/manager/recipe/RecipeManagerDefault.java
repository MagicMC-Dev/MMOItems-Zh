package net.Indyuce.mmoitems.manager.recipe;

import java.util.List;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.BlastingRecipe;
import org.bukkit.inventory.CampfireRecipe;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.SmokingRecipe;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.recipe.MMORecipeChoice;
import net.Indyuce.mmoitems.api.recipe.workbench.CustomRecipe;

public class RecipeManagerDefault extends RecipeManager {
	public RecipeManagerDefault() {
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
							section.getConfigurationSection("shapeless").getKeys(false).forEach(
									recipe -> registerShapelessRecipe(type, id, section.getStringList("shapeless." + recipe)));
						if (section.contains("furnace"))
							section.getConfigurationSection("furnace").getKeys(false).forEach(recipe -> registerFurnaceRecipe(type, id,
									new BurningRecipeInformation(section.getConfigurationSection("furnace." + recipe)), recipe));
						if (section.contains("blast"))
							section.getConfigurationSection("blast").getKeys(false).forEach(recipe -> registerBlastRecipe(type, id,
									new BurningRecipeInformation(section.getConfigurationSection("blast." + recipe)), recipe));
						if (section.contains("smoker"))
							section.getConfigurationSection("smoker").getKeys(false).forEach(recipe -> registerSmokerRecipe(type, id,
									new BurningRecipeInformation(section.getConfigurationSection("smoker." + recipe)), recipe));
						if (section.contains("campfire"))
							section.getConfigurationSection("campfire").getKeys(false).forEach(recipe -> registerCampfireRecipe(type, id,
									new BurningRecipeInformation(section.getConfigurationSection("campfire." + recipe)), recipe));
					}
				} catch (IllegalArgumentException exception) {
					MMOItems.plugin.getLogger().log(Level.WARNING, "Could not load recipe of " + id + ": " + exception.getMessage());
				}
		}

		sortRecipes();		
		Bukkit.getScheduler().runTask(MMOItems.plugin,
				() -> getLoadedRecipes().forEach(recipe -> Bukkit.addRecipe(recipe.getRecipe())));
	}

	@Override
	public void registerFurnaceRecipe(Type type, String id, BurningRecipeInformation info, String number) {
		if(!info.getChoice().isValid()) {
			MMOItems.plugin.getLogger().warning("Couldn't load furnace recipe for '" + type.getId() + "." + id + "'");
			return;
		}
		NamespacedKey key = getRecipeKey(type, id, "furnace", number);
		FurnaceRecipe recipe = new FurnaceRecipe(key, MMOItems.plugin.getItems().getItem(type, id), toBukkit(info.getChoice()), info.getExp(),
				info.getBurnTime());
		registerRecipe(key, recipe);
	}

	public void registerBlastRecipe(Type type, String id, BurningRecipeInformation info, String number) {
		if(!info.getChoice().isValid()) {
			MMOItems.plugin.getLogger().warning("Couldn't load blast furnace recipe for '" + type.getId() + "." + id + "'");
			return;
		}
		NamespacedKey key = getRecipeKey(type, id, "blast", number);
		BlastingRecipe recipe = new BlastingRecipe(key, MMOItems.plugin.getItems().getItem(type, id), toBukkit(info.getChoice()), info.getExp(),
				info.getBurnTime());
		registerRecipe(key, recipe);
	}

	public void registerSmokerRecipe(Type type, String id, BurningRecipeInformation info, String number) {
		if(!info.getChoice().isValid()) {
			MMOItems.plugin.getLogger().warning("Couldn't load smoker recipe for '" + type.getId() + "." + id + "'");
			return;
		}
		NamespacedKey key = getRecipeKey(type, id, "smoker", number);
		SmokingRecipe recipe = new SmokingRecipe(key, MMOItems.plugin.getItems().getItem(type, id), toBukkit(info.getChoice()), info.getExp(),
				info.getBurnTime());
		registerRecipe(key, recipe);
	}

	public void registerCampfireRecipe(Type type, String id, BurningRecipeInformation info, String number) {
		if(!info.getChoice().isValid()) {
			MMOItems.plugin.getLogger().warning("Couldn't load campfire recipe for '" + type.getId() + "." + id + "'");
			return;
		}
		NamespacedKey key = getRecipeKey(type, id, "campfire", number);
		CampfireRecipe recipe = new CampfireRecipe(key, MMOItems.plugin.getItems().getItem(type, id), toBukkit(info.getChoice()), info.getExp(),
				info.getBurnTime());
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

	/*
	 * TODO When Bukkit changes their 'RecipeChoice.ExactChoice' API we can
	 * remove the suppressed warnings, but right now it works despite being
	 * marked as deprecated. It is just draft API and probably subject to change.
	 */
	@SuppressWarnings("deprecation")
	public RecipeChoice toBukkit(MMORecipeChoice choice) {
		return choice.isVanilla() ? new RecipeChoice.MaterialChoice(choice.getItem().getType())
				: new RecipeChoice.ExactChoice(choice.getItem());
	}
}
