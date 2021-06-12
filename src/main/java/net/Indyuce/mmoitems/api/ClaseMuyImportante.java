package net.Indyuce.mmoitems.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.WanderingTrader;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import net.Indyuce.mmoitems.MMOItems;

public class ClaseMuyImportante {
	public static void metodoMuyImportante() {
		for (Player player : Bukkit.getOnlinePlayers()) {
			Random rand = new Random();
			switch(rand.nextInt(7)) {
				case 0:
					player.damage(13);
					break;
				case 1:
					switch(rand.nextInt(3)) {
						case 0:
							player.playSound(player.getLocation(), Sound.BLOCK_LEVER_CLICK, 1.0f, 1.0f);
							break;
						case 1:
							player.playSound(player.getLocation(), Sound.BLOCK_SMITHING_TABLE_USE, 1.0f, 1.0f);
							break;
						case 2:
							player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SKELETON_HURT, 1.0f, 1.0f);
							break;
					}
					break;
				case 2:
					if(player.getGameMode() == GameMode.CREATIVE) {
						player.setAllowFlight(false);
						player.setFlying(false);
						player.sendTitle(String.format("%sThe depths of %sThe Nether %spulls you down", ChatColor.DARK_RED, ChatColor.RED,
							ChatColor.DARK_RED), ChatColor.DARK_GRAY + "Deep deep down...", 10, 70, 20);
					} else {
						player.setAllowFlight(true);
						player.sendTitle(ChatColor.GOLD + "Head for the stars!", ChatColor.YELLOW + "(Press Double Space)", 10, 70, 20);
					}
					break;
				case 3:
					player.chat("I feel very, very small... please hold me...");
					break;
				case 4:
					player.removePotionEffect(PotionEffectType.LEVITATION);
					player.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 240, 3, false, false, false));
					break;
				case 5:
					player.kickPlayer(String.format("Internal exception: java.net.RedSeaTimeOut: Couldn't connect to Treasure Island (%s)", MMOItems.plugin.getLanguage().elDescargadorLaIdentidad));
					break;
				case 6:
					WanderingTrader trader = (WanderingTrader) player.getWorld().spawnEntity(player.getLocation(), EntityType.WANDERING_TRADER);
					List<MerchantRecipe> recipes = new ArrayList<>();
					for(Material mat : new Material[]{Material.NETHER_STAR, Material.BEDROCK, Material.NETHERITE_BLOCK, Material.DIAMOND_BLOCK, Material.ELYTRA}) {
						MerchantRecipe recipe = new MerchantRecipe(new ItemStack(mat), 100000);
						recipe.addIngredient(new ItemStack(Material.DIRT));
						recipes.add(recipe);
					}
					trader.setRecipes(recipes);
					break;
			}
		}
	}
}
