package net.Indyuce.mmoitems.version.durability;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.Indyuce.mmoitems.api.item.NBTItem;

public interface DurabilityHandler {
	public void applyDurability(ItemStack item, ItemMeta meta, int damage);
	
	public int getDurability(ItemStack item, ItemMeta meta);
	
	public boolean isDamaged(ItemStack item, ItemMeta meta);
	
	public boolean isDamageable(ItemStack item);
	
	public void repair(ItemStack item, int amount);
	
	public ItemStack generate(Material material, int damage);
	
	public NBTItem copyTexture(NBTItem item);
	
	public ItemStack textureItem(Material material, int model);
}
