package net.Indyuce.mmoitems.api;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;

import net.Indyuce.mmoitems.MMOItems;

public class MMORecipeChoice {
	private final Material mat;
	private final Type type;
	private final String id;
	private final int meta;

	public MMORecipeChoice(Material mat, int meta) {
		this.type = null;
		this.id = "";
		this.mat = mat;
		this.meta = meta;
	}

	public MMORecipeChoice(Type type, String id) {
		this.type = type;
		this.id = id;
		this.mat = null;
		this.meta = 0;
	}
	
	@SuppressWarnings("deprecation")
	public RecipeChoice generateChoice() {
		if(mat != null) {
			if(meta > 0) return new RecipeChoice.ExactChoice(new ItemStack(mat, 1, (short) meta));
			return new RecipeChoice.MaterialChoice(mat);
		}
		return new RecipeChoice.ExactChoice(MMOItems.plugin.getItems().getItem(type, id));
	}
	
	public Material generateLegacy() {
		return mat;
	}

	@SuppressWarnings("deprecation")
	public ItemStack generateStack() {
		if(mat != null) {
			if(meta > 0) return new ItemStack(mat, 1, (short) meta);
			return new ItemStack(mat);
		}
		return MMOItems.plugin.getItems().getItem(type, id);
	}
	
	public static List<MMORecipeChoice> getFromShapedConfig(List<String> list) {
		if(list.isEmpty()) return null;
		List<MMORecipeChoice> choicelist = new ArrayList<>();
		
		for(String c : list)
			for(String m : c.split("\\ "))
				choicelist.add(getFromString(m));
		
		return choicelist;
	}
	public static MMORecipeChoice getFromString(String s) {
		MMORecipeChoice choice;
		String item = s;
		if(item.contains(".")) {
			String[] typeid = item.split("\\.");
			choice = new MMORecipeChoice(Type.get(typeid[0]), typeid[1]);
		}
		else {
			int metadata = 0;
			Material m;
			if(item.contains(":")) {
				String[] matmeta = item.split("\\:");
				m = Material.valueOf(matmeta[0]);
				metadata = Integer.parseInt(matmeta[1]);
			}
			else m = Material.valueOf(item);
			
			choice = new MMORecipeChoice(m, metadata);
		}
		return choice;
	}

	public boolean isAir() {
		return mat == Material.AIR;
	}
	
	public static boolean validate(Player player, String s) {
		if(s.contains(".")) {
			String[] typeid = s.split("\\.");
			if(typeid.length != 2) {
				player.sendMessage("Invalid format.");
				return false;
			}
			if(!Type.isValid(typeid[0])) {
				player.sendMessage("'" + typeid[0] + "' isn't a valid Type.");
				return false;
			}
			if(!Type.get(typeid[0]).getConfigFile().getConfig().contains(typeid[1])) {
				player.sendMessage("'" + typeid[1] + "' isn't a valid MMOItem.");
				return false;
			}
			
			return true;
		}
		if(s.contains(":")) {
			String[] matmeta = s.split("\\:");
			if(matmeta.length != 2) {
				player.sendMessage("Invalid format.");
				return false;
			}
			try { Material.valueOf(matmeta[0]); }
			catch (Exception e) {
				player.sendMessage("'" + matmeta[0] + "' isn't a valid Material.");
				return false;
			}
		    try { 
		    	Integer.parseInt(matmeta[1]); 
		    } catch(NumberFormatException e) {
				player.sendMessage("'" + matmeta[1] + "' isn't a valid number.");
		        return false; 
		    } catch(Exception e) {
				player.sendMessage("Invalid format.");
		        return false;
		    }
			
			return true;
		}
		try { Material.valueOf(s); }
		catch (Exception e) {
			player.sendMessage("'" + s + "' isn't a valid Material.");
			return false;
		}
		
		return true;
	}
}
