package net.Indyuce.mmoitems.api.recipe;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.MMOUtils;
import net.mmogroup.mmolib.MMOLib;
import net.mmogroup.mmolib.version.VersionMaterial;

public enum CraftingType {
	SHAPED(21, "The C. Table Recipe (Shaped) for this item", VersionMaterial.CRAFTING_TABLE, true),
	SHAPELESS(22, "The C. Table Recipe (Shapeless) for this item", VersionMaterial.CRAFTING_TABLE, true),
	FURNACE(23, "The Furnace Recipe for this item", Material.FURNACE, true),
	BLAST(30, "The Blast Furnace Recipe for this item", VersionMaterial.BLAST_FURNACE, false),
	SMOKER(31, "The Smoker Recipe for this item", VersionMaterial.SMOKER, false),
	CAMPFIRE(32, "The Campfire Recipe for this item", VersionMaterial.CAMPFIRE, false);

	private final int slot;
	private final String lore;
	private final Material material;
	private final boolean old;

	private CraftingType(int slot, String lore, VersionMaterial material, boolean old) {
		this(slot, lore, material.toMaterial(), old);
	}

	private CraftingType(int slot, String lore, Material material, boolean old) {
		this.slot = slot;
		this.lore = lore;
		this.material = material;
		this.old = old;
	}

	public ItemStack getItem() {
		return new ItemStack(material);
	}

	public int getSlot() {
		return slot;
	}

	public String getName() {
		return MMOUtils.caseOnWords(name().toLowerCase());
	}

	public String getLore() {
		return lore;
	}

	public boolean shouldAdd() {
		return MMOLib.plugin.getVersion().isStrictlyHigher(1, 14) || old;
	}
}