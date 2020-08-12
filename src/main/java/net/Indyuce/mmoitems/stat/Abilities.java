package net.Indyuce.mmoitems.stat;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.ability.Ability;
import net.Indyuce.mmoitems.api.ability.Ability.CastingMode;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.ReadMMOItem;
import net.Indyuce.mmoitems.api.util.NumericStatFormula;
import net.Indyuce.mmoitems.gui.edition.AbilityListEdition;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.data.AbilityData;
import net.Indyuce.mmoitems.stat.data.AbilityListData;
import net.Indyuce.mmoitems.stat.data.random.RandomAbilityData;
import net.Indyuce.mmoitems.stat.data.random.RandomAbilityListData;
import net.Indyuce.mmoitems.stat.data.random.RandomStatData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.mmogroup.mmolib.api.item.ItemTag;
import net.mmogroup.mmolib.api.util.AltChar;

public class Abilities extends ItemStat {
	private final DecimalFormat modifierFormat = new DecimalFormat("0.#");

	public Abilities() {
		super("ABILITY", new ItemStack(Material.BLAZE_POWDER), "Item Abilities",
				new String[] { "Make your item cast amazing abilities", "to kill monsters or buff yourself." }, new String[] { "!block", "all" });
	}

	@Override
	public RandomStatData whenInitialized(Object object) {
		Validate.isTrue(object instanceof ConfigurationSection, "Must specify a valid config section");
		ConfigurationSection config = (ConfigurationSection) object;
		RandomAbilityListData list = new RandomAbilityListData();

		for (String key : config.getKeys(false))
			list.add(new RandomAbilityData(config.getConfigurationSection(key)));

		return list;
	}

	@Override
	public void whenApplied(ItemStackBuilder item, StatData data) {
		List<String> abilityLore = new ArrayList<>();
		boolean splitter = !MMOItems.plugin.getLanguage().abilitySplitter.equals("");

		String modifierFormat = ItemStat.translate("ability-modifier"), abilityFormat = ItemStat.translate("ability-format");
		JsonArray jsonArray = new JsonArray();
		((AbilityListData) data).getAbilities().forEach(ability -> {
			abilityLore.add(abilityFormat.replace("#c", MMOItems.plugin.getLanguage().getCastingModeName(ability.getCastingMode())).replace("#a",
					MMOItems.plugin.getLanguage().getAbilityName(ability.getAbility())));

			jsonArray.add(ability.toJson());
			for (String modifier : ability.getModifiers()) {
				item.getLore().registerPlaceholder("ability_" + ability.getAbility().getID().toLowerCase() + "_" + modifier,
						this.modifierFormat.format(ability.getModifier(modifier)));
				abilityLore.add(modifierFormat.replace("#m", MMOItems.plugin.getLanguage().getModifierName(modifier)).replace("#v",
						this.modifierFormat.format(ability.getModifier(modifier))));
			}

			if (splitter)
				abilityLore.add(MMOItems.plugin.getLanguage().abilitySplitter);
		});

		if (splitter && abilityLore.size() > 0)
			abilityLore.remove(abilityLore.size() - 1);

		item.getLore().insert("abilities", abilityLore);
		item.addItemTag(new ItemTag("MMOITEMS_ABILITIES", jsonArray.toString()));
	}

	@Override
	public void whenClicked(EditionInventory inv, InventoryClickEvent event) {
		new AbilityListEdition(inv.getPlayer(), inv.getEdited()).open(inv.getPage());
	}

	@Override
	public void whenInput(EditionInventory inv, String message, Object... info) {
		String configKey = (String) info[0];
		String edited = (String) info[1];

		if (edited.equals("ability")) {
			String format = message.toUpperCase().replace("-", "_").replace(" ", "_").replaceAll("[^A-Z_]", "");
			Validate.isTrue(MMOItems.plugin.getAbilities().hasAbility(format),
					"format is not a valid ability! You may check the ability list using /mi list ability.");
			Ability ability = MMOItems.plugin.getAbilities().getAbility(format);

			inv.getEditedSection().set("ability." + configKey, null);
			inv.getEditedSection().set("ability." + configKey + ".type", format);
			inv.registerTemplateEdition();
			inv.getPlayer().sendMessage(
					MMOItems.plugin.getPrefix() + "Successfully set the ability to " + ChatColor.GOLD + ability.getName() + ChatColor.GRAY + ".");
			return;
		}

		if (edited.equals("mode")) {
			CastingMode castMode = CastingMode.valueOf(message.toUpperCase().replace("-", "_").replace(" ", "_"));
			Ability ability = MMOItems.plugin.getAbilities().getAbility(inv.getEditedSection().getString("ability." + configKey + ".type")
					.toUpperCase().replace("-", "_").replace(" ", "_").replaceAll("[^A-Z_]", ""));
			Validate.isTrue(ability.isAllowedMode(castMode), "This ability does not support this casting mode.");

			inv.getEditedSection().set("ability." + configKey + ".mode", castMode.name());
			inv.registerTemplateEdition();
			inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "Successfully set the casting mode to " + ChatColor.GOLD + castMode.getName()
					+ ChatColor.GRAY + ".");
			return;
		}

		new NumericStatFormula(message).fillConfigurationSection(inv.getEditedSection(), "ability." + configKey + "." + edited);
		inv.registerTemplateEdition();
		inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + ChatColor.GOLD + MMOUtils.caseOnWords(edited.replace("-", " ")) + ChatColor.GRAY
				+ " successfully added.");
	}

	@Override
	public void whenDisplayed(List<String> lore, Optional<RandomStatData> optional) {
		lore.add(ChatColor.GRAY + "Current Abilities: " + ChatColor.GOLD
				+ (optional.isPresent() ? ((RandomAbilityListData) optional.get()).getAbilities().size() : 0));
		lore.add("");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " Click to edit the item abilities.");
	}

	@Override
	public void whenLoaded(ReadMMOItem mmoitem) {
		if (mmoitem.getNBT().hasTag("MMOITEMS_ABILITIES"))
			try {
				AbilityListData list = new AbilityListData();
				new JsonParser().parse(mmoitem.getNBT().getString("MMOITEMS_ABILITIES")).getAsJsonArray()
						.forEach(obj -> list.add(new AbilityData(obj.getAsJsonObject())));
				mmoitem.setData(ItemStat.ABILITIES, list);
			} catch (JsonSyntaxException | IllegalStateException exception) {
				/*
				 * OLD ITEM WHICH MUST BE UPDATED.
				 */
			}
	}
}
