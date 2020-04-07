package net.Indyuce.mmoitems.stat;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.ConfigFile;
import net.Indyuce.mmoitems.api.edition.StatEdition;
import net.Indyuce.mmoitems.api.item.MMOItem;
import net.Indyuce.mmoitems.api.item.build.MMOItemBuilder;
import net.Indyuce.mmoitems.api.itemgen.RandomStatData;
import net.Indyuce.mmoitems.api.util.AltChar;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.data.PotionEffectData;
import net.Indyuce.mmoitems.stat.data.PotionEffectListData;
import net.Indyuce.mmoitems.stat.data.random.RandomPotionEffectListData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.mmogroup.mmolib.api.item.ItemTag;
import net.mmogroup.mmolib.api.item.NBTItem;

public class Effects extends ItemStat {
	private final DecimalFormat durationFormat = new DecimalFormat("0.#");

	public Effects() {
		super("EFFECTS", new ItemStack(Material.POTION), "Effects", new String[] { "The potion effects your", "consumable item grants." },
				new String[] { "consumable" });
	}

	@Override
	public StatData whenInitialized(Object object) {
		Validate.isTrue(object instanceof ConfigurationSection, "Must specify a config section");
		ConfigurationSection config = (ConfigurationSection) object;

		PotionEffectListData effects = new PotionEffectListData();

		for (String effect : config.getKeys(false)) {
			PotionEffectType type = PotionEffectType.getByName(effect.toUpperCase().replace("-", "_").replace(" ", "_"));
			Validate.isTrue(type != null, "Could not find potion effect type named '" + effect + "'");

			String[] split = config.getString(effect).split("\\,");
			double duration = Double.parseDouble(split[0]);

			int amplifier = Integer.parseInt(split[1]);
			effects.add(new PotionEffectData(type, duration, amplifier));
		}

		return effects;
	}
	
	@Override
	public RandomStatData whenInitializedGeneration(Object object) {
		Validate.isTrue(object instanceof ConfigurationSection, "Must specify a config section");
		return new RandomPotionEffectListData((ConfigurationSection) object);
	}

	@Override
	public boolean whenClicked(EditionInventory inv, InventoryClickEvent event) {
		ConfigFile config = inv.getItemType().getConfigFile();
		if (event.getAction() == InventoryAction.PICKUP_ALL)
			new StatEdition(inv, ItemStat.EFFECTS).enable("Write in the chat the permanent potion effect you want to add.",
					ChatColor.AQUA + "Format: [POTION_EFFECT] [DURATION] [AMPLIFIER]");

		if (event.getAction() == InventoryAction.PICKUP_HALF) {
			if (config.getConfig().getConfigurationSection(inv.getItemId()).contains("effects")) {
				Set<String> set = config.getConfig().getConfigurationSection(inv.getItemId() + ".effects").getKeys(false);
				String last = Arrays.asList(set.toArray(new String[0])).get(set.size() - 1);
				config.getConfig().set(inv.getItemId() + ".effects." + last, null);
				if (set.size() <= 1)
					config.getConfig().set(inv.getItemId() + ".effects", null);
				inv.registerItemEdition(config);
				inv.open();
				inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "Successfully removed " + last.substring(0, 1).toUpperCase()
						+ last.substring(1).toLowerCase() + ChatColor.GRAY + ".");
			}
		}
		return true;
	}

	@Override
	public boolean whenInput(EditionInventory inv, ConfigFile config, String message, Object... info) {
		String[] split = message.split("\\ ");
		if (split.length != 3) {
			inv.getPlayer()
					.sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + message + " is not a valid [POTION_EFFECT] [DURATION] [AMPLIFIER].");
			inv.getPlayer()
					.sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + "Example: 'FAST_DIGGING 30 3' stands for Haste 3 for 30 seconds.");
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
			inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix()
					+ "All potion effects can be found here: https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/potion/PotionEffectType.html");
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

		config.getConfig().set(inv.getItemId() + ".effects." + effect.getName(), duration + "," + amplifier);
		inv.registerItemEdition(config);
		inv.open();
		inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + effect.getName() + " " + amplifier + " successfully added.");
		return true;
	}

	@Override
	public void whenDisplayed(List<String> lore, FileConfiguration config, String path) {
		lore.add("");
		lore.add(ChatColor.GRAY + "Current Value:");
		if (!config.getConfigurationSection(path).contains("effects"))
			lore.add(ChatColor.RED + "No effect.");
		else if (config.getConfigurationSection(path + ".effects").getKeys(false).isEmpty())
			lore.add(ChatColor.RED + "No effect.");
		else
			for (String s1 : config.getConfigurationSection(path + ".effects").getKeys(false)) {
				String effect = s1;
				effect = effect.replace("-", " ").replace("_", " ");
				effect = effect.substring(0, 1).toUpperCase() + effect.substring(1).toLowerCase();
				String[] split = config.getString(path + ".effects." + s1).split("\\,");
				String durationFormat = "";
				try {
					durationFormat = "" + Double.parseDouble(split[0]);
				} catch (Exception e) {
					durationFormat = "?";
				}

				if (split.length == 1)
					lore.add(ChatColor.GRAY + "* " + ChatColor.GREEN + effect + " I " + ChatColor.GRAY + "(" + ChatColor.GREEN + durationFormat
							+ ChatColor.GRAY + "s)");

				if (split.length == 2) {
					String amplifierFormat = "";
					try {
						amplifierFormat = "" + MMOUtils.intToRoman(Integer.parseInt(split[1]));
					} catch (Exception e) {
						amplifierFormat = "?";
					}
					lore.add(ChatColor.GRAY + "* " + ChatColor.GREEN + effect + " " + amplifierFormat + " " + ChatColor.GRAY + "(" + ChatColor.GREEN
							+ durationFormat + ChatColor.GRAY + "s)");
				}
			}
		lore.add("");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " Click to add an effect.");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " Right click to remove the last effect.");
	}

	@Override
	public boolean whenApplied(MMOItemBuilder item, StatData data) {
		List<String> lore = new ArrayList<>();
		JsonArray array = new JsonArray();

		String effectFormat = ItemStat.translate("effect");
		((PotionEffectListData) data).getEffects().forEach(effect -> {
			lore.add(effectFormat
					.replace("#e",
							MMOItems.plugin.getLanguage().getPotionEffectName(effect.getType())
									+ (effect.getLevel() < 2 ? "" : " " + MMOUtils.intToRoman(effect.getLevel())))
					.replace("#d", durationFormat.format(effect.getDuration())));

			JsonObject object = new JsonObject();
			object.addProperty("Type", effect.getType().getName());
			object.addProperty("Duration", effect.getDuration());
			object.addProperty("Level", effect.getLevel());
			array.add(object);
		});

		item.getLore().insert("effects", lore);
		item.addItemTag(new ItemTag("MMOITEMS_EFFECTS", array.toString()));
		return false;
	}

	@Override
	public void whenLoaded(MMOItem mmoitem, NBTItem nbtItem) {
		if (nbtItem.hasTag(getNBTPath()))
			try {
				PotionEffectListData effects = new PotionEffectListData();

				new JsonParser().parse(nbtItem.getString("MMOITEMS_EFFECTS")).getAsJsonArray().forEach(element -> {
					JsonObject key = element.getAsJsonObject();
					effects.add(new PotionEffectData(PotionEffectType.getByName(key.get("Type").getAsString()), key.get("Duration").getAsDouble(),
							key.get("Level").getAsInt()));
				});

				mmoitem.setData(ItemStat.EFFECTS, effects);
			} catch (JsonSyntaxException exception) {
				/*
				 * OLD ITEM WHICH MUST BE UPDATED.
				 */
			}
	}
}
