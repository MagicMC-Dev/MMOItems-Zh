package net.Indyuce.mmoitems.stat;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.google.gson.*;
import io.lumine.mythic.lib.api.item.SupportedNBTTagValues;
import io.lumine.mythic.lib.skill.trigger.TriggerType;
import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryClickEvent;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.ability.Ability;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.ReadMMOItem;
import net.Indyuce.mmoitems.api.util.NumericStatFormula;
import net.Indyuce.mmoitems.api.util.NumericStatFormula.FormulaSaveOption;
import net.Indyuce.mmoitems.gui.edition.AbilityListEdition;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.data.AbilityData;
import net.Indyuce.mmoitems.stat.data.AbilityListData;
import net.Indyuce.mmoitems.stat.data.random.RandomAbilityData;
import net.Indyuce.mmoitems.stat.data.random.RandomAbilityListData;
import net.Indyuce.mmoitems.stat.data.random.RandomStatData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.api.util.AltChar;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Abilities extends ItemStat {
	private final DecimalFormat modifierFormat = new DecimalFormat("0.#");

	public Abilities() {
		super("ABILITY", Material.BLAZE_POWDER, "Item Abilities",
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
	public void whenApplied(@NotNull ItemStackBuilder item, @NotNull StatData data) {

		//Modify Lore
		List<String> abilityLore = new ArrayList<>();
		boolean splitter = !MMOItems.plugin.getLanguage().abilitySplitter.equals("");

		String modifierFormat = ItemStat.translate("ability-modifier"), abilityFormat = ItemStat.translate("ability-format");

		((AbilityListData) data).getAbilities().forEach(ability -> {
			abilityLore.add(abilityFormat.replace("#c", MMOItems.plugin.getLanguage().getCastingModeName(ability.getTriggerType())).replace("#a",
					MMOItems.plugin.getLanguage().getAbilityName(ability.getAbility())));

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

		// Modify tags
		item.getLore().insert("abilities", abilityLore);
		item.addItemTag(getAppliedNBT(data));
	}

	@NotNull
	@Override
	public ArrayList<ItemTag> getAppliedNBT(@NotNull StatData data) {

		// Make Array
		ArrayList<ItemTag> ret = new ArrayList<>();

		// Convert to JSON
		JsonArray jsonArray = new JsonArray();
		for (AbilityData ab : ((AbilityListData) data).getAbilities()) { jsonArray.add(ab.toJson()); }

		// Put
		ret.add(new ItemTag(getNBTPath(), jsonArray.toString()));

		return ret;
	}

	@Override
	public void whenClicked(@NotNull EditionInventory inv, @NotNull InventoryClickEvent event) {
		new AbilityListEdition(inv.getPlayer(), inv.getEdited()).open(inv.getPage());
	}

	@Override
	public void whenInput(@NotNull EditionInventory inv, @NotNull String message, Object... info) {
		String configKey = (String) info[0];
		String edited = (String) info[1];

		String format = message.toUpperCase().replace("-", "_").replace(" ", "_").replaceAll("[^A-Z_]", "");
		if (edited.equals("ability")) {
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
			TriggerType castMode = TriggerType.valueOf(format);
			inv.getEditedSection().set("ability." + configKey + ".mode", castMode.name());
			inv.registerTemplateEdition();
			inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "Successfully set the trigger to " + ChatColor.GOLD + castMode.getName()
					+ ChatColor.GRAY + ".");
			return;
		}

		new NumericStatFormula(message).fillConfigurationSection(inv.getEditedSection(), "ability." + configKey + "." + edited,
				FormulaSaveOption.NONE);
		inv.registerTemplateEdition();
		inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + ChatColor.GOLD + MMOUtils.caseOnWords(edited.replace("-", " ")) + ChatColor.GRAY
				+ " successfully added.");
	}

	@Override
	public void whenDisplayed(List<String> lore, Optional<RandomStatData> statData) {
		lore.add(ChatColor.GRAY + "Current Abilities: " + ChatColor.GOLD
				+ (statData.isPresent() ? ((RandomAbilityListData) statData.get()).getAbilities().size() : 0));
		lore.add("");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " Click to edit the item abilities.");
	}

	@NotNull
	@Override
	public StatData getClearStatData() {
		return new AbilityListData();
	}

	@Override
	public void whenLoaded(@NotNull ReadMMOItem mmoitem) {

		// Get tags
		ArrayList<ItemTag> relevantTags = new ArrayList<>();

		if (mmoitem.getNBT().hasTag(getNBTPath()))
			relevantTags.add(ItemTag.getTagAtPath(getNBTPath(), mmoitem.getNBT(), SupportedNBTTagValues.STRING));

		AbilityListData data = (AbilityListData) getLoadedNBT(relevantTags);

		// Valid?
		if (data != null) {

			// Set
			mmoitem.setData(this, data);
		}
	}

	@Nullable
	@Override
	public StatData getLoadedNBT(@NotNull ArrayList<ItemTag> storedTags) {

		// Get Json Array thing
		ItemTag jsonCompact = ItemTag.getTagAtPath(getNBTPath(), storedTags);

		// Found?
		if (jsonCompact != null) {

			// Attempt to parse
			try {

				// New ablity list data
				AbilityListData list = new AbilityListData();
				JsonArray ar = new JsonParser().parse((String) jsonCompact.getValue()).getAsJsonArray();

				// Convert every object into an ability
				for (JsonElement e : ar) {

					// For every object
					if (e.isJsonObject()) {

						// Get as object
						JsonObject obj = e.getAsJsonObject();

						// Add to abilit list
						list.add(new AbilityData(obj));
					}
				}

				// Well that mus thave worked aye?
				return list;

			} catch (JsonSyntaxException|IllegalStateException exception) {
				/*
				 * OLD ITEM WHICH MUST BE UPDATED.
				 */
			}
		}

		// Did not work out
		return null;
	}
}
