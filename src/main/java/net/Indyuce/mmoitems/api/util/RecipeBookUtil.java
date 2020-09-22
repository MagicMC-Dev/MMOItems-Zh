package net.Indyuce.mmoitems.api.util;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

import net.Indyuce.mmoitems.MMOItems;

public class RecipeBookUtil {
	private static boolean amounts = false;
	private static boolean enabled = false;

	public static void enableAmounts() {
		amounts = true;
	}
	
	public static void enableBook() {
		if(amounts) {
			MMOItems.plugin.getLogger().warning("Tried to enable recipe book while amounts are active!");
			MMOItems.plugin.getLogger().warning("Please use only ONE of these options!");
			return;
		}
		enabled = true;
	}

	public static void refresh(Player player) {
		if(!enabled) return;
		
		for (NamespacedKey key : player.getDiscoveredRecipes())
			if (key.getNamespace().equals("mmoitems")
					&& !MMOItems.plugin.getRecipes().getNamespacedKeys().contains(key))
				player.undiscoverRecipe(key);

		for (NamespacedKey recipe : MMOItems.plugin.getRecipes().getNamespacedKeys())
			if (!player.hasDiscoveredRecipe(recipe))
				player.discoverRecipe(recipe);
	}

	public static void refreshOnline() {
		if(!enabled) return;
		
		for (Player player : Bukkit.getOnlinePlayers())
			refresh(player);
	}

	public static void clear() {
		for (NamespacedKey recipe : MMOItems.plugin.getRecipes().getNamespacedKeys())
			Bukkit.removeRecipe(recipe);
	}

	public static boolean isAmounts() {
		return amounts;
	}
}
