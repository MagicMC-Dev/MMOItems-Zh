package net.Indyuce.mmoitems.stat;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.google.gson.JsonSyntaxException;
import io.lumine.mythic.lib.api.item.SupportedNBTTagValues;
import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.potion.PotionEffectType;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.util.MMOUtils;
import net.Indyuce.mmoitems.api.edition.StatEdition;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.ReadMMOItem;
import net.Indyuce.mmoitems.api.util.NumericStatFormula;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.data.PotionEffectData;
import net.Indyuce.mmoitems.stat.data.PotionEffectListData;
import net.Indyuce.mmoitems.stat.data.random.RandomPotionEffectData;
import net.Indyuce.mmoitems.stat.data.random.RandomPotionEffectListData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.api.util.AltChar;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * This class has not been updated for the item generation update!!! The potion
 * amplifier and duration are not numeric formulas but flat values.... TODO
 */
public class PermanentEffects extends ItemStat<RandomPotionEffectListData, PotionEffectListData> {
	public PermanentEffects() {
		super("PERM_EFFECTS", Material.POTION, "Permanent Effects", new String[] { "The potion effects your", "item grants to the holder." },
				new String[] { "!miscellaneous", "!block", "all" });
	}

	@Override
	public RandomPotionEffectListData whenInitialized(Object object) {
		Validate.isTrue(object instanceof ConfigurationSection, "Must specify a config section");
		ConfigurationSection config = (ConfigurationSection) object;

		RandomPotionEffectListData effects = new RandomPotionEffectListData();

		for (String effect : config.getKeys(false)) {
			PotionEffectType type = PotionEffectType.getByName(effect.toUpperCase().replace("-", "_").replace(" ", "_"));
			Validate.notNull(type, "Could not find potion effect type named '" + effect + "'");
			effects.add(new RandomPotionEffectData(type, new NumericStatFormula(config.get(effect))));
		}

		return effects;
	}

	@Override
	public void whenClicked(@NotNull EditionInventory inv, @NotNull InventoryClickEvent event) {
		if (event.getAction() == InventoryAction.PICKUP_ALL)
			new StatEdition(inv, ItemStats.PERM_EFFECTS).enable("Write in the chat the permanent potion effect you want to add.",
					ChatColor.AQUA + "Format: {Effect Name} {Amplifier Numeric Formula}");

		if (event.getAction() == InventoryAction.PICKUP_HALF) {
			if (inv.getEditedSection().contains("perm-effects")) {
				Set<String> set = inv.getEditedSection().getConfigurationSection("perm-effects").getKeys(false);
				String last = new ArrayList<>(set).get(set.size() - 1);
				inv.getEditedSection().set("perm-effects." + last, null);
				if (set.size() <= 1)
					inv.getEditedSection().set("perm-effects", null);
				inv.registerTemplateEdition();
				inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "Successfully removed " + last.substring(0, 1).toUpperCase()
						+ last.substring(1).toLowerCase() + ".");
			}
		}
	}

	@Override
	public void whenInput(@NotNull EditionInventory inv, @NotNull String message, Object... info) {
		String[] split = message.split(" ");
		Validate.isTrue(split.length >= 2, "Use this format: {Effect Name} {Effect Amplifier Numeric Formula}. Example: 'speed 1 0.3' "
				+ "stands for Speed 1, plus 0.3 level per item level (rounded up to lower int)");

		PotionEffectType effect = PotionEffectType.getByName(split[0].replace("-", "_"));
		Validate.notNull(effect, split[0] + " is not a valid potion effect. All potion effects can be found here: "
				+ "https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/potion/PotionEffectType.html");

		NumericStatFormula formula = new NumericStatFormula(message.substring(message.indexOf(" ") + 1));
		formula.fillConfigurationSection(inv.getEditedSection(), "perm-effects." + effect.getName());
		inv.registerTemplateEdition();
		inv.getPlayer().sendMessage(
				MMOItems.plugin.getPrefix() + ChatColor.GOLD + effect.getName() + " " + formula + ChatColor.GRAY + " successfully added.");
	}

	@Override
	public void whenDisplayed(List<String> lore, Optional<RandomPotionEffectListData> statData) {
		if (statData.isPresent()) {
			lore.add(ChatColor.GRAY + "Current Value:");
			RandomPotionEffectListData data = statData.get();
			for (RandomPotionEffectData effect : data.getEffects())
				lore.add(ChatColor.GRAY + "* " + ChatColor.GREEN + MMOUtils.caseOnWords(effect.getType().getName().replace("_", " ").toLowerCase())
						+ " " + effect.getAmplifier().toString());

		} else
			lore.add(ChatColor.GRAY + "Current Value: " + ChatColor.RED + "None");

		lore.add("");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " Click to add an effect.");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " Right click to remove the last effect.");
	}

	@NotNull
	@Override
	public PotionEffectListData getClearStatData() {
		return new PotionEffectListData();
	}

	@Override
	public void whenApplied(@NotNull ItemStackBuilder item, @NotNull PotionEffectListData data) {
		List<String> lore = new ArrayList<>();

		String permEffectFormat = ItemStat.translate("perm-effect");
		((PotionEffectListData) data).getEffects().forEach(effect -> {
			lore.add(permEffectFormat.replace("{effect}", MMOItems.plugin.getLanguage().getPotionEffectName(effect.getType()) + " " + MMOUtils.intToRoman(effect.getLevel())));
		});

		item.getLore().insert("perm-effects", lore);

		// Yes
		item.addItemTag(getAppliedNBT(data));
	}

	@NotNull
	@Override
	public ArrayList<ItemTag> getAppliedNBT(@NotNull PotionEffectListData data) {

		// Them tags
		ArrayList<ItemTag> ret = new ArrayList<>();
		JsonObject object = new JsonObject();

		// For every registered effect
		for (PotionEffectData effect : data.getEffects()) {

			// Add Properies
			object.addProperty(effect.getType().getName(), effect.getLevel());
		}

		// Add onto the list
		ret.add(new ItemTag(getNBTPath(), object.toString()));

		return ret;
	}

	@Override
	public void whenLoaded(@NotNull ReadMMOItem mmoitem) {

		// Find tags
		ArrayList<ItemTag> rTag = new ArrayList<>();
		if (mmoitem.getNBT().hasTag(getNBTPath()))
			rTag.add(ItemTag.getTagAtPath(getNBTPath(), mmoitem.getNBT(), SupportedNBTTagValues.STRING));

		// Build Data
		StatData data = getLoadedNBT(rTag);

		// Add data, if valid
		if (data != null) { mmoitem.setData(this, data); }
	}

	@Nullable
	@Override
	public PotionEffectListData getLoadedNBT(@NotNull ArrayList<ItemTag> storedTags) {

		// Find tag
		ItemTag oTag = ItemTag.getTagAtPath(getNBTPath(), storedTags);

		// Well
		if (oTag != null) {

			// Parse as Json
			try {

				// A new effect
				PotionEffectListData effects = new PotionEffectListData();

				JsonElement element = new JsonParser().parse((String) oTag.getValue());

				element.getAsJsonObject().entrySet().forEach(entry ->
						effects.add(new PotionEffectData(PotionEffectType.getByName(entry.getKey()), entry.getValue().getAsInt())));

				// Thats it
				return effects;

			} catch (JsonSyntaxException |IllegalStateException exception) {
				/*
				 * OLD ITEM WHICH MUST BE UPDATED.
				 */
			}
		}

		// Noep
		return null;
	}
}
