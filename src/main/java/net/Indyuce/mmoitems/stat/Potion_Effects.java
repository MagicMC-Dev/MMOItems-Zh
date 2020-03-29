package net.Indyuce.mmoitems.stat;

import java.util.Arrays;
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
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffectType;

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
import net.Indyuce.mmoitems.stat.type.StringStat;

public class Potion_Effects extends StringStat {
	public Potion_Effects() {
		super(new ItemStack(Material.POTION), "Potion Effects", new String[] { "The effects of your potion.", "(May have an impact on color)." }, "potion-effect", new String[] { "all" }, Material.POTION, Material.SPLASH_POTION, Material.LINGERING_POTION, Material.TIPPED_ARROW);
	}

	@Override
	public boolean whenClicked(EditionInventory inv, InventoryClickEvent event) {
		ConfigFile config = inv.getItemType().getConfigFile();
		if (event.getAction() == InventoryAction.PICKUP_ALL)
			new StatEdition(inv, ItemStat.POTION_EFFECTS).enable("Write in the chat the potion effect you want to add.", ChatColor.AQUA + "Format: [POTION_EFFECT] [DURATION] [AMPLIFIER]");

		if (event.getAction() == InventoryAction.PICKUP_HALF) {
			if (config.getConfig().getConfigurationSection(inv.getItemId()).contains("potion-effects")) {
				Set<String> set = config.getConfig().getConfigurationSection(inv.getItemId() + ".potion-effects").getKeys(false);
				String last = Arrays.asList(set.toArray(new String[0])).get(set.size() - 1);
				config.getConfig().set(inv.getItemId() + ".potion-effects." + last, null);
				if (set.size() <= 1)
					config.getConfig().set(inv.getItemId() + ".potion-effects", null);
				inv.registerItemEdition(config);
				inv.open();
				inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "Successfully removed " + last.substring(0, 1).toUpperCase() + last.substring(1).toLowerCase() + ChatColor.GRAY + ".");
			}
		}
		return true;
	}

	@Override
	public boolean whenInput(EditionInventory inv, ConfigFile config, String message, Object... info) {
		String[] split = message.split("\\ ");
		if (split.length != 3) {
			inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + message + " is not a valid [POTION_EFFECT] [DURATION] [AMPLIFIER].");
			inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + "Example: 'FAST_DIGGING 30 3' stands for Haste 3 for 30 seconds.");
			return false;
		}

		PotionEffectType effect = null;
		for (PotionEffectType effect1 : PotionEffectType.values())
			if (effect1 != null)
				if (effect1.getName().equalsIgnoreCase(split[0].replace("-", "_"))) {
					effect = effect1;
					break;
				}

		if (effect == null) {
			inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + split[0] + " is not a valid potion effect!");
			inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "All potion effects can be found here: https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/potion/PotionEffectType.html");
			return false;
		}

		double duration = 0;
		try {
			duration = Double.parseDouble(split[1]);
		} catch (Exception e1) {
			inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + split[1] + " is not a valid number!");
			return false;
		}

		int amplifier = 0;
		try {
			amplifier = (int) Double.parseDouble(split[2]);
		} catch (Exception e1) {
			inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + split[2] + " is not a valid number!");
			return false;
		}

		config.getConfig().set(inv.getItemId() + ".potion-effects." + effect.getName(), duration + "," + amplifier);
		inv.registerItemEdition(config);
		inv.open();
		inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + effect.getName() + " " + amplifier + " successfully added.");
		return true;
	}

	@Override
	public void whenDisplayed(List<String> lore, FileConfiguration config, String path) {
		lore.add("");
		lore.add(ChatColor.GRAY + "Current Value:");
		if (!config.getConfigurationSection(path).contains("potion-effects"))
			lore.add(ChatColor.RED + "No effect.");
		else if (config.getConfigurationSection(path + ".potion-effects").getKeys(false).isEmpty())
			lore.add(ChatColor.RED + "No effect.");
		else
			for (String s1 : config.getConfigurationSection(path + ".potion-effects").getKeys(false)) {
				String effect = s1;
				effect = effect.replace("-", " ").replace("_", " ");
				effect = effect.substring(0, 1).toUpperCase() + effect.substring(1).toLowerCase();
				String[] split = config.getString(path + ".potion-effects." + s1).split("\\,");
				String durationFormat = "";
				try {
					durationFormat = "" + Double.parseDouble(split[0]);
				} catch (Exception e) {
					durationFormat = "?";
				}

				if (split.length == 1)
					lore.add(ChatColor.GRAY + "* " + ChatColor.GREEN + effect + " I " + ChatColor.GRAY + "(" + ChatColor.GREEN + durationFormat + ChatColor.GRAY + "s)");

				if (split.length == 2) {
					String amplifierFormat = "";
					try {
						amplifierFormat = "" + MMOUtils.intToRoman(Integer.parseInt(split[1]));
					} catch (Exception e) {
						amplifierFormat = "?";
					}
					lore.add(ChatColor.GRAY + "* " + ChatColor.GREEN + effect + " " + amplifierFormat + " " + ChatColor.GRAY + "(" + ChatColor.GREEN + durationFormat + ChatColor.GRAY + "s)");
				}
			}
		lore.add("");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " Click to add an effect.");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " Right click to remove the last effect.");
	}

	@Override
	public void whenLoaded(MMOItem item, ConfigurationSection config) {
		EffectListData effects = new EffectListData();

		for (String effect : config.getConfigurationSection("potion-effects").getKeys(false)) {
			PotionEffectType type = PotionEffectType.getByName(effect.toUpperCase().replace("-", "_").replace(" ", "_"));
			Validate.isTrue(type != null, "Could not find potion effect type called '" + effect + "'");

			String[] split = config.getString("potion-effects." + effect).split("\\,");
			double duration = Double.parseDouble(split[0]);

			int amplifier = split.length > 1 ? Integer.parseInt(split[1]) : 0;

			effects.add(new PotionEffectData(type, duration, amplifier));
		}

		item.setData(ItemStat.POTION_EFFECTS, effects);
	}

	@Override
	public boolean whenApplied(MMOItemBuilder item, StatData data) {
		if (item.getItemStack().getType().name().contains("POTION") || item.getItemStack().getType() == Material.TIPPED_ARROW)
			for (PotionEffectData effect : ((EffectListData) data).getEffects())
				((PotionMeta) item.getMeta()).addCustomEffect(effect.toEffect(), false);
		return true;
	}
}
