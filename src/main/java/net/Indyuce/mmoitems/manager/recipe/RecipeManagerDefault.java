package net.Indyuce.mmoitems.manager.recipe;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.BlastingRecipe;
import org.bukkit.inventory.CampfireRecipe;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.SmokingRecipe;
import org.bukkit.inventory.meta.ItemMeta;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.MMORecipeChoice;
import net.Indyuce.mmoitems.api.Type;

@SuppressWarnings("deprecation")
public class RecipeManagerDefault extends RecipeManager {
	@Override
	protected void load() {
		for (Type type : MMOItems.plugin.getTypes().getAll()) {
			FileConfiguration config = type.getConfigFile().getConfig();

			for (String id : config.getKeys(false)) {
				if (config.getConfigurationSection(id).contains("advanced-craft")) {
					registerAdvancedWorkbenchRecipe(type, id, config);
				}
				
				if (config.getConfigurationSection(id).contains("crafting")) {
					ConfigurationSection craftingc = config.getConfigurationSection(id + ".crafting");
					
					if(craftingc.contains("shaped")) craftingc.getConfigurationSection("shaped").getKeys(false).forEach(recipe -> 
						registerShapedRecipe(type, id, craftingc.getStringList("shaped." + recipe), recipe));
					if(craftingc.contains("shapeless")) craftingc.getConfigurationSection("shapeless").getKeys(false).forEach(recipe ->
						registerShapelessRecipe(type, id, craftingc.getConfigurationSection("shapeless." + recipe), recipe));
					if(craftingc.contains("furnace")) craftingc.getConfigurationSection("furnace").getKeys(false).forEach(recipe ->
						registerFurnaceRecipe(type, id, new RecipeInformation(craftingc.getConfigurationSection("furnace." + recipe)), recipe));
					if(craftingc.contains("blast")) craftingc.getConfigurationSection("blast").getKeys(false).forEach(recipe ->
						registerBlastRecipe(type, id, new RecipeInformation(craftingc.getConfigurationSection("blast." + recipe)), recipe));
					if(craftingc.contains("smoker")) craftingc.getConfigurationSection("smoker").getKeys(false).forEach(recipe ->
						registerSmokerRecipe(type, id, new RecipeInformation(craftingc.getConfigurationSection("smoker." + recipe)), recipe));
					if(craftingc.contains("campfire")) craftingc.getConfigurationSection("campfire").getKeys(false).forEach(recipe ->
						registerCampfireRecipe(type, id, new RecipeInformation(craftingc.getConfigurationSection("campfire." + recipe)), recipe));
				}
			}
		}
		
		
		//registerCampfireRecipe(MMOItems.plugin.getItems().getItem(Type.SWORD, "SILVER_SWORD"), new RecipeChoice.ExactChoice(MMOItems.plugin.getItems().getItem(Type.get("MATERIAL"), "SILVER_INGOT")));
		
		Bukkit.getScheduler().runTask(MMOItems.plugin, new Runnable() {
			@Override
			public void run() {
				for(Recipe r : loadedRecipes)
					Bukkit.addRecipe(r);
			}
		});
	}
	
	@Override
	protected void registerFurnaceRecipe(Type type, String id, RecipeInformation info, String number) {
		NamespacedKey key = getRecipeKey(type, id, "furnace", number);
		FurnaceRecipe recipe = new FurnaceRecipe(key, MMOItems.plugin.getItems().getItem(type, id), generateChoice(info.choice), info.exp, info.burnTime);
		
		loadedRecipes.add(recipe); keys.add(key);
	}
	
	@Override
	protected void registerBlastRecipe(Type type, String id, RecipeInformation info, String number) {
		NamespacedKey key = getRecipeKey(type, id, "blast", number);
		BlastingRecipe recipe = new BlastingRecipe(key, MMOItems.plugin.getItems().getItem(type, id), generateChoice(info.choice), info.exp, info.burnTime);
		
		loadedRecipes.add(recipe); keys.add(key);
	}

	@Override
	protected void registerSmokerRecipe(Type type, String id, RecipeInformation info, String number) {
		NamespacedKey key = getRecipeKey(type, id, "smoker", number);
		SmokingRecipe recipe = new SmokingRecipe(key, MMOItems.plugin.getItems().getItem(type, id), generateChoice(info.choice), info.exp, info.burnTime);
		
		loadedRecipes.add(recipe); keys.add(key);
	}

	@Override
	protected void registerCampfireRecipe(Type type, String id, RecipeInformation info, String number) {
		NamespacedKey key = getRecipeKey(type, id, "campfire", number);
		CampfireRecipe recipe = new CampfireRecipe(key, MMOItems.plugin.getItems().getItem(type, id), generateChoice(info.choice), info.exp, info.burnTime);
		
		loadedRecipes.add(recipe); keys.add(key);
	}
	
	@Override
	protected void registerShapedRecipe(Type type, String id, List<String> list, String number) {
		NamespacedKey key = getRecipeKey(type, id, "shaped", number);
		ShapedRecipe recipe = new ShapedRecipe(key, MMOItems.plugin.getItems().getItem(type, id));
		
		List<MMORecipeChoice> rcList = MMORecipeChoice.getFromShapedConfig(list);
		if(rcList == null) return;
		
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
		
		loadedRecipes.add(recipe); keys.add(key);
	}
	
	@Override
	protected void shapedIngredient(ShapedRecipe recipe, char c, MMORecipeChoice rc) {
		if(rc.isAir()) recipe.setIngredient(c, Material.AIR);
		else recipe.setIngredient(c, generateChoice(rc));
	}

	@Override
	protected void registerShapelessRecipe(Type type, String id, ConfigurationSection config, String number) {
		NamespacedKey key = getRecipeKey(type, id, "shapeless", number);
		ShapelessRecipe recipe = new ShapelessRecipe(key, MMOItems.plugin.getItems().getItem(type, id));

		for (int i = 1; i < 10; i++) {
			if(config.contains("item" + i)) shapelessIngredient(recipe, MMORecipeChoice.getFromString(config.getString("item" + i)));
		}
		
		if(recipe.getIngredientList().isEmpty()) return;
		loadedRecipes.add(recipe); keys.add(key);
	}

	@Override
	protected void shapelessIngredient(ShapelessRecipe recipe, MMORecipeChoice rc) {
		if(!rc.isAir()) recipe.addIngredient(generateChoice(rc));
	}

	@Override
	protected void setIngredientOrAir(ShapedRecipe recipe, char character, ConfigurationSection c) {
		if(c.contains("type")) {
			ItemStack item = MMOItems.plugin.getItems().getItem(Type.get(c.getString("type")), c.getString("id"));
			if(item == null) {
				MMOItems.plugin.getLogger().warning("WARNING - Couldn't add (Type: " + c.getString("type") + ", Id: " + c.getString("id") +") as it wasn't found.");
				MMOItems.plugin.getLogger().warning("Using default material: DIRT BLOCK - (Please fix this as soon as possible!)");
				
				recipe.setIngredient(character, new RecipeChoice.MaterialChoice(Material.DIRT));
			}
			else {
				item.setAmount(c.getInt("amount", 1));
				recipe.setIngredient(character, new RecipeChoice.ExactChoice(item));
				//
			}
		} else if(c.contains("material")) {
			Material material = Material.valueOf(c.getString("material"));
			int amount = c.getInt("amount", 1);
			String name = c.getString("name", "");
			if(name.isEmpty() && amount == 1)
				recipe.setIngredient(character, material);
			else {
				ItemStack item = new ItemStack(material);
				item.setAmount(amount); ItemMeta meta = item.getItemMeta();
				meta.setDisplayName(name); item.setItemMeta(meta);
				recipe.setIngredient(character, new RecipeChoice.ExactChoice(item));
			}
		}
	}

	public RecipeChoice generateChoice(MMORecipeChoice rc) {
		if(rc.getMaterial() != null) {
			if(rc.getMeta() > 0) return new RecipeChoice.ExactChoice(new ItemStack(rc.getMaterial(), 1, (short) rc.getMeta()));
			return new RecipeChoice.MaterialChoice(rc.getMaterial());
		}
		return new RecipeChoice.ExactChoice(MMOItems.plugin.getItems().getItem(rc.getType(), rc.getId()));
	}
}
