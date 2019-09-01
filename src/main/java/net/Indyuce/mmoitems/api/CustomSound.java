package net.Indyuce.mmoitems.api;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public enum CustomSound {
	ON_ATTACK(Material.IRON_SWORD, 19, "On Attack", new String[] { "Plays when attacking an entity." }),
	ON_RIGHT_CLICK(Material.STONE_HOE, 22, "On Right Click", new String[] { "Plays when item is right-clicked." }),
	ON_BLOCK_BREAK(Material.STONE, 25, "On Block Break", new String[] { "Plays when a block is broken with the item." });

	private ItemStack item;
	private String name;
	private String[] lore;
	private int slot;

	private CustomSound(Material material, int slot, String name, String[] lore) {
		this.item = new ItemStack(material);
		this.name = name;
		this.lore = lore;
		this.slot = slot;
	}

	public ItemStack getItem() {
		return item;
	}

	public String getName() {
		return name;
	}
	
	public String[] getLore() {
		return lore;
	}
	
	public int getSlot() {
		return slot;
	}
}