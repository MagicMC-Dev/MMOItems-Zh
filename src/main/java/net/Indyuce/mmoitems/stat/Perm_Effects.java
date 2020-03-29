package net.Indyuce.mmoitems.stat;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.Validate;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.ConfigFile;
import net.Indyuce.mmoitems.api.edition.StatEdition;
import net.Indyuce.mmoitems.api.item.MMOItem;
import net.Indyuce.mmoitems.api.item.build.MMOItemBuilder;
import net.Indyuce.mmoitems.api.util.AltChar;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.data.EffectListData;
import net.Indyuce.mmoitems.stat.data.PotionEffectData;
import net.Indyuce.mmoitems.stat.data.StatData;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.mmogroup.mmolib.api.item.ItemTag;
import net.mmogroup.mmolib.api.item.NBTItem;

public class Perm_Effects extends ItemStat {
	public Perm_Effects() {
		super(new ItemStack(Material.POTION), "Permanent Effects", new String[] { "The potion effects your", "item grants to the holder." }, "perm-effects", new String[] { "!miscellaneous", "all" });
	}

	@Override
	public boolean whenClicked(EditionInventory inv, InventoryClickEvent event) {
		ConfigFile config = inv.getItemType().getConfigFile();
		if (event.getAction() == InventoryAction.PICKUP_ALL)
			new StatEdition(inv, ItemStat.PERM_EFFECTS).enable("Write in the chat the permanent potion effect you want to add.", "Format: [POTION_EFFECT] [AMPLIFIER]");

		if (event.getAction() == InventoryAction.PICKUP_HALF) {
			if (config.getConfig().getConfigurationSection(inv.getItemId()).contains("perm-effects")) {
				Set<String> set = config.getConfig().getConfigurationSection(inv.getItemId() + ".perm-effects").getKeys(false);
				String last = new ArrayList<>(set).get(set.size() - 1);
				config.getConfig().set(inv.getItemId() + ".perm-effects." + last, null);
				if (set.size() <= 1)
					config.getConfig().set(inv.getItemId() + ".perm-effects", null);
				inv.registerItemEdition(config);
				inv.open();
				inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "Successfully removed " + last.substring(0, 1).toUpperCase() + last.substring(1).toLowerCase() + "�7.");
			}
		}
		return true;
	}

	@Override
	public boolean whenInput(EditionInventory inv, ConfigFile config, String message, Object... info) {
		String[] split = message.split("\\ ");
		if (split.length != 2) {
			inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + message + " is not a valid [POTION_EFFECT] [AMPLIFIER].");
			inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + "Example: 'INCREASE_DAMAGE 4' stands for Strength 4.");
			return false;
		}

		PotionEffectType effect = null;
		for (PotionEffectType effect1 : PotionEffectType.values())
			if (effect1 != null && effect1.getName().equalsIgnoreCase(split[0].replace("-", "_"))) {
				effect = effect1;
				break;
			}

		if (effect == null) {
			inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + split[0] + " is not a valid potion effect.");
			inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + "All potion effects can be found here: https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/potion/PotionEffectType.html");
			return false;
		}

		int amplifier = 0;
		try {
			amplifier = (int) Double.parseDouble(split[1]);
		} catch (Exception e1) {
			inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + split[1] + " is not a valid number!");
			return false;
		}

		config.getConfig().set(inv.getItemId() + ".perm-effects." + effect.getName(), amplifier);
		inv.registerItemEdition(config);
		inv.open();
		inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + effect.getName() + " " + amplifier + " successfully added.");
		return true;
	}

	@Override
	public void whenDisplayed(List<String> lore, FileConfiguration config, String path) {
		lore.add("");
		lore.add(ChatColor.GRAY + "Current Value:");
		if (!config.getConfigurationSection(path).contains("perm-effects"))
			lore.add(ChatColor.RED + "No permanent effect.");
		else if (config.getConfigurationSection(path + ".perm-effects").getKeys(false).isEmpty())
			lore.add(ChatColor.RED + "No permanent effect.");
		else
			for (String s1 : config.getConfigurationSection(path + ".perm-effects").getKeys(false)) {
				String effect = s1;
				effect = effect.replace("-", " ").replace("_", " ");
				effect = effect.substring(0, 1).toUpperCase() + effect.substring(1).toLowerCase();
				String level = MMOUtils.intToRoman(config.getInt(path + ".perm-effects." + s1));
				lore.add(ChatColor.GRAY + "* " + ChatColor.GREEN + effect + " " + level);
			}
		lore.add("");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " Click to add an effect.");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " Right click to remove the last effect.");
	}

	@Override
	public void whenLoaded(MMOItem item, ConfigurationSection config) {
		EffectListData effects = new EffectListData();

		for (String effect : config.getConfigurationSection("perm-effects").getKeys(false)) {
			PotionEffectType type = MMOUtils.valueOfPotionEffectType(effect);
			Validate.isTrue(type != null, "Could not find potion effect type named '" + effect + "'");
			effects.add(new PotionEffectData(type, config.getInt("perm-effects." + effect)));
		}

		item.setData(ItemStat.PERM_EFFECTS, effects);
	}

	@Override
	public boolean whenApplied(MMOItemBuilder item, StatData data) {
		JsonObject object = new JsonObject();
		List<String> lore = new ArrayList<>();

		String permEffectFormat = ItemStat.translate("perm-effect");
		((EffectListData) data).getEffects().forEach(effect -> {
			lore.add(permEffectFormat.replace("#", MMOItems.plugin.getLanguage().getPotionEffectName(effect.getType()) + " " + MMOUtils.intToRoman(effect.getLevel())));
			object.addProperty(effect.getType().getName(), effect.getLevel());
		});

		item.getLore().insert("perm-effects", lore);
		item.addItemTag(new ItemTag("MMOITEMS_PERM_EFFECTS", object.toString()));
		return false;
	}

	@Override
	public void whenLoaded(MMOItem mmoitem, NBTItem item) {
		if (item.hasTag(getNBTPath()))
			try {
				EffectListData effects = new EffectListData();

				JsonObject json = new JsonParser().parse(item.getString("MMOITEMS_PERM_EFFECTS")).getAsJsonObject();
				json.entrySet().forEach(entry -> effects.add(new PotionEffectData(PotionEffectType.getByName(entry.getKey()), entry.getValue().getAsInt())));

				mmoitem.setData(ItemStat.PERM_EFFECTS, effects);
			} catch (JsonSyntaxException exception) {
				/*
				 * OLD ITEM WHICH MUST BE UPDATED.
				 */
			}
	}
}
