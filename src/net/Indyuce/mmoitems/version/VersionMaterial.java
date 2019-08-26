package net.Indyuce.mmoitems.version;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.MMOItems;

public enum VersionMaterial {

	/*
	 * the enum object name corresponds to the 1.14 material name. the first
	 * argument corresponds to the 1.13 name. second argument corresponds to
	 * legacy, third is DURABILITY if needed only.
	 */

	OAK_SIGN("SIGN", "SIGN"),
	LAPIS_LAZULI("LAPIS_LAZULI", "INK_SACK", 4),
	LIME_DYE("LIME_DYE", "INK_SACK", 5),
	LIGHT_GRAY_DYE("LIGHT_GRAY_DYE", "INK_SACK", 7),
	GRAY_DYE("GRAY_DYE", "INK_SACK", 8),
	LIGHT_BLUE_DYE("LIGHT_BLUE_DYE", "INK_SACK", 12),
	RED_DYE("ROSE_RED", "INK_SACK", 14),
	BONE_MEAL("BONE_MEAL", "INK_SACK", 18),
	GRAY_STAINED_GLASS_PANE("GRAY_STAINED_GLASS_PANE", "STAINED_GLASS_PANE", 7),
	RED_STAINED_GLASS_PANE("RED_STAINED_GLASS_PANE", "STAINED_GLASS_PANE", 14),
	LIME_STAINED_GLASS("LIME_STAINED_GLASS", "STAINED_GLASS", 5),
	PINK_STAINED_GLASS("PINK_STAINED_GLASS", "STAINED_GLASS", 6),
	PLAYER_HEAD("PLAYER_HEAD", "SKULL_ITEM", 3),
	SKELETON_SKULL("SKELETON_SKULL", "SKULL_ITEM"),
	NETHER_WART("NETHER_WART", "NETHER_STALK"),
	WRITABLE_BOOK("WRITABLE_BOOK", "BOOK_AND_QUILL"),
	CRAFTING_TABLE("CRAFTING_TABLE", "WORKBENCH"),
	SNOWBALL("SNOWBALL", "SNOW_BALL"),
	LILY_PAD("LILY_PAD", "WATER_LILY"),
	GUNPOWDER("GUNPOWDER", "SULPHUR"),
	OAK_SAPLING("OAK_SAPLING", "SAPLING"),
	COMPARATOR("COMPARATOR", "REDSTONE_COMPARATOR"),
	EXPERIENCE_BOTTLE("EXPERIENCE_BOTTLE", "EXP_BOTTLE"),
	IRON_HORSE_ARMOR("IRON_HORSE_ARMOR", "IRON_BARDING"),
	MUSIC_DISC_MALL("MUSIC_DISC_MALL", "RECORD_8"),
	COBBLESTONE_WALL("COBBLESTONE_WALL", "COBBLE_WALL"),
	ENDER_EYE("ENDER_EYE", "EYE_OF_ENDER"),
	GRASS_BLOCK("GRASS_BLOCK", "GRASS"),
	ENCHANTING_TABLE("ENCHANTING_TABLE", "ENCHANTMENT_TABLE"),
	PORKCHOP("PORKCHOP", "PORK"),
	GOLDEN_CHESTPLATE("GOLDEN_CHESTPLATE", "GOLD_CHESTPLATE"),
	GOLDEN_HORSE_ARMOR("GOLDEN_HORSE_ARMOR", "GOLD_BARDING"),
	COMMAND_BLOCK_MINECART("COMMAND_BLOCK_MINECART", "COMMAND_MINECART"),
	OAK_PLANKS("OAK_PLANKS", "WOOD"),

	;

	private Material material;
	private ItemStack item;

	private VersionMaterial(String name_1_13, String legacy) {
		material = Material.valueOf(MMOItems.plugin.getVersion().isStrictlyHigher(1, 13) ? name() : MMOItems.plugin.getVersion().isStrictlyHigher(1, 12) ? name_1_13 : legacy);
	}

	@SuppressWarnings("deprecation")
	private VersionMaterial(String name_1_13, String legacy, int legacyDurability) {
		if (MMOItems.plugin.getVersion().isStrictlyHigher(1, 12))
			material = Material.valueOf(MMOItems.plugin.getVersion().isStrictlyHigher(1, 13) ? name() : name_1_13);
		else
			item = new ItemStack(material = Material.valueOf(legacy), 1, (short) legacyDurability);
	}

	public Material toMaterial() {
		return material;
	}

	public boolean hasItem() {
		return item != null;
	}

	public ItemStack toItem() {
		return hasItem() ? item.clone() : new ItemStack(material);
	}
}
