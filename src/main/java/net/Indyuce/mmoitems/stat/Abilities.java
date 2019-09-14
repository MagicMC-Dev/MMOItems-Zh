package net.Indyuce.mmoitems.stat;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.Ability;
import net.Indyuce.mmoitems.api.Ability.CastingMode;
import net.Indyuce.mmoitems.api.item.MMOItem;
import net.Indyuce.mmoitems.api.item.NBTItem;
import net.Indyuce.mmoitems.api.item.build.MMOItemBuilder;
import net.Indyuce.mmoitems.api.util.AltChar;
import net.Indyuce.mmoitems.api.ConfigFile;
import net.Indyuce.mmoitems.gui.edition.AbilityListEdition;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.data.AbilityData;
import net.Indyuce.mmoitems.stat.data.Mergeable;
import net.Indyuce.mmoitems.stat.data.StatData;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.Indyuce.mmoitems.version.nms.ItemTag;

public class Abilities extends ItemStat {
	private final DecimalFormat modifierFormat = new DecimalFormat("0.###");

	public Abilities() {
		super(new ItemStack(Material.BLAZE_POWDER), "Item Abilities", new String[] { "Make your item cast amazing abilities", "to kill monsters or buff yourself." }, "ability", new String[] { "all" });
	}

	@Override
	public boolean whenLoaded(MMOItem item, ConfigurationSection config) {
		AbilityListData list = new AbilityListData();

		for (String key : config.getConfigurationSection("ability").getKeys(false)) {
			AbilityData data = new AbilityData(item, config.getConfigurationSection("ability." + key));
			if (data.isValid())
				list.add(data);
		}

		item.setData(ItemStat.ABILITIES, list);
		return true;
	}

	@Override
	public boolean whenApplied(MMOItemBuilder item, StatData data) {
		List<String> abilityLore = new ArrayList<>();
		boolean splitter = !MMOItems.plugin.getLanguage().abilitySplitter.equals("");

		String modifierFormat = ItemStat.translate("ability-modifier"), abilityFormat = ItemStat.translate("ability-format");
		JsonArray jsonArray = new JsonArray();
		((AbilityListData) data).getAbilities().forEach(ability -> {
			abilityLore.add(abilityFormat.replace("#c", MMOItems.plugin.getLanguage().getCastingModeName(ability.getCastingMode())).replace("#a", MMOItems.plugin.getLanguage().getAbilityName(ability.getAbility())));

			jsonArray.add(ability.toJson());
			ability.getModifiers().forEach(modifier -> abilityLore.add(modifierFormat.replace("#m", MMOItems.plugin.getLanguage().getModifierName(modifier)).replace("#v", this.modifierFormat.format(ability.getModifier(modifier)))));

			if (splitter)
				abilityLore.add(MMOItems.plugin.getLanguage().abilitySplitter);
		});

		if (splitter && abilityLore.size() > 0)
			abilityLore.remove(abilityLore.size() - 1);

		item.getLore().insert("abilities", abilityLore);
		item.addItemTag(new ItemTag("MMOITEMS_ABILITIES", jsonArray.toString()));
		return true;
	}

	@Override
	public boolean whenClicked(EditionInventory inv, InventoryClickEvent event) {
		new AbilityListEdition(inv.getPlayer(), inv.getItemType(), inv.getItemId()).open(inv.getPage());
		return true;
	}

	@Override
	public boolean whenInput(EditionInventory inv, ConfigFile config, String message, Object... info) {
		String configKey = (String) info[0];
		String edited = (String) info[1];

		if (edited.equals("ability")) {
			String format = message.toUpperCase().replace("-", "_").replace(" ", "_").replaceAll("[^A-Z_]", "");
			if (!MMOItems.plugin.getAbilities().hasAbility(format)) {
				inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + format + " is not a valid ability!");
				inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + "See all abilities: /mi list ability.");
				return false;
			}

			Ability ability = MMOItems.plugin.getAbilities().getAbility(format);

			config.getConfig().set(inv.getItemId() + ".ability." + configKey, null);
			config.getConfig().set(inv.getItemId() + ".ability." + configKey + ".type", format);
			inv.registerItemEdition(config);
			inv.open();
			inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "Successfully set the ability to " + ChatColor.GOLD + ability.getName() + ChatColor.GRAY + ".");
			return true;
		}

		if (edited.equals("mode")) {
			CastingMode castMode = CastingMode.safeValueOf(message);
			if (castMode == null) {
				inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + "Make sure you enter a valid casting mode.");
				return false;
			}

			Ability ability = MMOItems.plugin.getAbilities().getAbility(config.getConfig().getString(inv.getItemId() + ".ability." + configKey + ".type").toUpperCase().replace("-", "_").replace(" ", "_").replaceAll("[^A-Z_]", ""));
			if (!ability.isAllowedMode(castMode)) {
				inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + "This ability does not support this casting mode.");
				return false;
			}

			config.getConfig().set(inv.getItemId() + ".ability." + configKey + ".mode", castMode.name());
			inv.registerItemEdition(config);
			inv.open();
			inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "Successfully set the casting mode to " + ChatColor.GOLD + castMode.getName() + ChatColor.GRAY + ".");
			return true;
		}

		double value = 0;
		try {
			value = Double.parseDouble(message);
		} catch (Exception e1) {
			inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + message + " is not a valid number!");
			return false;
		}

		config.getConfig().set(inv.getItemId() + ".ability." + configKey + "." + edited, value);
		inv.registerItemEdition(config);
		inv.open();
		inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + ChatColor.GOLD + MMOUtils.caseOnWords(edited.replace("-", " ")) + ChatColor.GRAY + " successfully added.");
		return true;
	}

	@Override
	public void whenDisplayed(List<String> lore, FileConfiguration config, String id) {
		lore.add("");
		lore.add(ChatColor.GRAY + "Current Abilities: " + ChatColor.GREEN + (config.getConfigurationSection(id).contains("ability") ? config.getConfigurationSection(id + ".ability").getKeys(false).size() : 0));
		lore.add("");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " Click to edit the item abilities.");
	}

	@Override
	public void whenLoaded(MMOItem mmoitem, NBTItem nbtItem) {
		if (nbtItem.hasTag("MMOITEMS_ABILITIES"))
			try {
				AbilityListData list = new AbilityListData();
				new JsonParser().parse(nbtItem.getString("MMOITEMS_ABILITIES")).getAsJsonArray().forEach(obj -> list.add(new AbilityData(obj.getAsJsonObject())));
				mmoitem.setData(ItemStat.ABILITIES, list);
			} catch (JsonSyntaxException | IllegalStateException exception) {
				/*
				 * OLD ITEM WHICH MUST BE UPDATED.
				 */
			}
	}

	public class AbilityListData extends StatData implements Mergeable {
		private Set<AbilityData> abilities = new LinkedHashSet<>();

		public AbilityListData() {
		}

		public AbilityListData(AbilityData... abilities) {
			add(abilities);
		}

		public void add(AbilityData... abilities) {
			for (AbilityData ability : abilities)
				this.abilities.add(ability);
		}

		public Set<AbilityData> getAbilities() {
			return abilities;
		}

		@Override
		public void merge(Mergeable stat) {
			abilities.addAll(((AbilityListData) stat).abilities);
		}
	}
}
