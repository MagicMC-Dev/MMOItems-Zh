package net.Indyuce.mmoitems.version.durability;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.item.NBTItem;
import net.Indyuce.mmoitems.version.nms.ItemTag;

@SuppressWarnings("deprecation")
public class LegacyDurabilityHandler implements DurabilityHandler {

	@Override
	public void applyDurability(ItemStack item, ItemMeta meta, int damage) {
		item.setDurability((short) damage);
	}

	@Override
	public int getDurability(ItemStack item, ItemMeta meta) {
		return item.getDurability();
	}

	@Override
	public boolean isDamaged(ItemStack item, ItemMeta meta) {
		return item.getDurability() > 0;
	}

	@Override
	public void repair(ItemStack item, int amount) {
		item.setDurability((short) Math.max(0, item.getDurability() - amount));
	}

	@Override
	public ItemStack generate(Material material, int damage) {
		return new ItemStack(material, 1, (short) damage);
	}

	@Override
	public boolean isDamageable(ItemStack item) {
		return item.getType().getMaxDurability() > 20;
	}

	@Override
	public NBTItem copyTexture(NBTItem item) {
		return MMOItems.plugin.getNMS().getNBTItem(new ItemStack(item.getItem().getType(), 1, item.getItem().getDurability())).addTag(new ItemTag("Unbreakable", true));
	}

	@Override
	public ItemStack textureItem(Material material, int model) {
		return MMOItems.plugin.getNMS().getNBTItem(new ItemStack(material, 1, (short) model)).addTag(new ItemTag("Unbreakable", true)).toItem();
	}
}
