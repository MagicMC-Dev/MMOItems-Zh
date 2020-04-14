package net.Indyuce.mmoitems.api;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.util.MushroomState;
import net.Indyuce.mmoitems.api.worldgen.WorldGenTemplate;
import net.mmogroup.mmolib.MMOLib;
import net.mmogroup.mmolib.api.item.ItemTag;

public class CustomBlock {
	private final int id;
	private final MushroomState state;

	private final String blockName;
	private final WorldGenTemplate template;
	private final List<String> lore = new ArrayList<>();
	private final int minExp, maxExp, requiredPower;

	public CustomBlock(MushroomState state, ConfigurationSection config) {
		this.id = Integer.valueOf(config.getName());
		this.state = state;

		Validate.notNull(config, "Could not read custom block config");

		blockName = ChatColor.translateAlternateColorCodes('&', config.getString("display-name", ChatColor.RESET + "Custom Block"));
		if (config.contains("lore"))
			for (String s : config.getStringList("lore"))
				lore.add(ChatColor.translateAlternateColorCodes('&', s));
		minExp = config.getInt("min-xp", 0);
		maxExp = config.getInt("max-xp", 0);
		requiredPower = config.getInt("required-power", 0);
		template = config.contains("gen-template") ? MMOItems.plugin.getWorldGen().getOrThrow(config.get("gen-template").toString()) : null;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return blockName;
	}

	public MushroomState getState() {
		return state;
	}

	public boolean hasGenTemplate() {
		return template != null;
	}

	public WorldGenTemplate getGenTemplate() {
		return template;
	}

	public List<String> getLore() {
		return lore;
	}

	public int getMinExpDrop() {
		return minExp;
	}

	public int getMaxExpDrop() {
		return maxExp;
	}

	public int getRequiredPower() {
		return requiredPower;
	}

	// Convert block data into Item
	public ItemStack getItem() {
		ItemStack item = new ItemStack(Material.CLAY_BALL);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(blockName);
		meta.setLore(lore);
		meta.setUnbreakable(true);
		meta.addItemFlags(ItemFlag.values());

		if (MMOLib.plugin.getVersion().isBelowOrEqual(1, 13))
			((Damageable) meta).setDamage(id);

		item.setItemMeta(meta);

		return MMOLib.plugin.getNMS().getNBTItem(item)
				.addTag(new ItemTag("MMOITEMS_DISABLE_CRAFTING", true), new ItemTag("MMOITEMS_DISABLE_SMITHING", true),
						new ItemTag("MMOITEMS_DISABLE_ENCHANTING", true), new ItemTag("MMOITEMS_DISABLE_REPAIRING", true),
						new ItemTag("MMOITEMS_BLOCK_ID", id), new ItemTag("CustomModelData", id + 1000))
				.toItem();
	}
}
