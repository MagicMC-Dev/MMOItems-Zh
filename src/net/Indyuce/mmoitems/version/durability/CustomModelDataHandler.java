package net.Indyuce.mmoitems.version.durability;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.item.NBTItem;
import net.Indyuce.mmoitems.version.nms.ItemTag;

public class CustomModelDataHandler implements DurabilityHandler {

	@Override
	public void applyDurability(ItemStack item, ItemMeta meta, int damage) {
		if (meta instanceof Damageable)
			((Damageable) meta).setDamage(damage);
	}

	@Override
	public int getDurability(ItemStack item, ItemMeta meta) {
		return item.getItemMeta() instanceof Damageable ? ((Damageable) item.getItemMeta()).getDamage() : 0;
	}

	@Override
	public boolean isDamaged(ItemStack item, ItemMeta meta) {
		return meta instanceof Damageable && ((Damageable) meta).getDamage() > 0;
	}

	@Override
	public void repair(ItemStack item, int amount) {
		ItemMeta meta = item.getItemMeta();
		((Damageable) meta).setDamage(Math.max(0, ((Damageable) meta).getDamage() - amount));
	}

	@Override
	public ItemStack generate(Material material, int damage) {
		ItemStack item = new ItemStack(material);
		ItemMeta meta = item.getItemMeta();
		((Damageable) meta).setDamage(damage);
		item.setItemMeta(meta);
		return item;
	}

	@Override
	public boolean isDamageable(ItemStack item) {
		return item.hasItemMeta() && item.getItemMeta() instanceof Damageable;
	}

	@Override
	public NBTItem copyTexture(NBTItem item) {
		return MMOItems.plugin.getNMS().getNBTItem(new ItemStack(item.getItem().getType())).addTag(new ItemTag("CustomModelData", item.getInteger("CustomModelData")));
	}

	@Override
	public ItemStack textureItem(Material material, int model) {
		return MMOItems.plugin.getNMS().getNBTItem(new ItemStack(material)).addTag(new ItemTag("CustomModelData", model)).toItem();
	}
}
