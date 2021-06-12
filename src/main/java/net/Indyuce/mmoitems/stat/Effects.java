package net.Indyuce.mmoitems.stat;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.api.item.SupportedNBTTagValues;
import io.lumine.mythic.lib.api.util.AltChar;
import io.lumine.mythic.lib.api.util.ui.FriendlyFeedbackProvider;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.edition.StatEdition;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.ReadMMOItem;
import net.Indyuce.mmoitems.api.item.mmoitem.VolatileMMOItem;
import net.Indyuce.mmoitems.api.util.NumericStatFormula;
import net.Indyuce.mmoitems.api.util.message.FFPMMOItems;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.data.PotionEffectData;
import net.Indyuce.mmoitems.stat.data.PotionEffectListData;
import net.Indyuce.mmoitems.stat.data.random.RandomPotionEffectData;
import net.Indyuce.mmoitems.stat.data.random.RandomPotionEffectListData;
import net.Indyuce.mmoitems.stat.data.random.RandomStatData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.Indyuce.mmoitems.stat.type.SelfConsumable;

public class Effects extends ItemStat implements SelfConsumable {
	private final DecimalFormat durationFormat = new DecimalFormat("0.#");

	public Effects() {
		super("EFFECTS", Material.POTION, "Effects", new String[] { "The potion effects your", "consumable item grants." },
				new String[] { "consumable" });
	}

	@Override
	public RandomStatData whenInitialized(Object object) {
		Validate.isTrue(object instanceof ConfigurationSection, FriendlyFeedbackProvider.quickForConsole(FFPMMOItems.get(),"Must specify a config section"));
		return new RandomPotionEffectListData((ConfigurationSection) object);
	}

	@Override
	public void whenClicked(@NotNull EditionInventory inv, @NotNull InventoryClickEvent event) {
		if (event.getAction() == InventoryAction.PICKUP_ALL)
			new StatEdition(inv, ItemStats.EFFECTS).enable("Write in the chat the permanent potion effect you want to add.",
					ChatColor.AQUA + "Format: {Potion Effect Name}|{Duration Numeric Formula}|{Amplifier Numeric Formula}", ChatColor.DARK_RED + "Note: " + ChatColor.RED + "The '|' lines are literal.");

		if (event.getAction() == InventoryAction.PICKUP_HALF) {
			if (inv.getEditedSection().contains("effects")) {
				Set<String> set = inv.getEditedSection().getConfigurationSection("effects").getKeys(false);
				String last = Arrays.asList(set.toArray(new String[0])).get(set.size() - 1);
				inv.getEditedSection().set("effects." + last, null);
				if (set.size() <= 1)
					inv.getEditedSection().set("effects", null);
				inv.registerTemplateEdition();
				inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "Successfully removed " + last.substring(0, 1).toUpperCase()
						+ last.substring(1).toLowerCase() + ChatColor.GRAY + ".");
			}
		}
	}

	@Override
	public void whenInput(@NotNull EditionInventory inv, @NotNull String message, Object... info) {
		String[] split = message.split("\\|");
		Validate.isTrue(split.length > 1, FriendlyFeedbackProvider.quickForConsole(FFPMMOItems.get(),"Use this format: $e{Potion Effect Name}|{Duration Numeric Formula}|{Amplifier Numeric Formula}$b."));

		PotionEffectType effect = PotionEffectType.getByName(split[0].replace("-", "_").replace(" ", "_").toUpperCase());
		Validate.notNull(effect, split[0] + FriendlyFeedbackProvider.quickForConsole(FFPMMOItems.get()," is not a valid potion effect. All potion effects can be found here:$e https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/potion/PotionEffectType.html"));

		NumericStatFormula duration = new NumericStatFormula(split[1]);
		NumericStatFormula amplifier = split.length > 2 ? new NumericStatFormula(split[2]) : new NumericStatFormula(1, 0, 0, 0);

		duration.fillConfigurationSection(inv.getEditedSection(), "effects." + effect.getName() + ".duration");
		amplifier.fillConfigurationSection(inv.getEditedSection(), "effects." + effect.getName() + ".amplifier");
		inv.registerTemplateEdition();
		inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + effect.getName() + " " + amplifier + " successfully added.");
	}

	@Override
	public void whenDisplayed(List<String> lore, Optional<RandomStatData> statData) {

		if (statData.isPresent()) {
			lore.add(ChatColor.GRAY + "Current Value:");
			RandomPotionEffectListData data = (RandomPotionEffectListData) statData.get();
			for (RandomPotionEffectData effect : data.getEffects())
				lore.add(ChatColor.GRAY + "* " + ChatColor.GREEN + MMOUtils.caseOnWords(effect.getType().getName().toLowerCase().replace("_", " "))
						+ ChatColor.GRAY + " Level: " + ChatColor.GREEN + effect.getAmplifier() + ChatColor.GRAY + " Duration: " + ChatColor.GREEN
						+ effect.getDuration());
		} else
			lore.add(ChatColor.GRAY + "Current Value: " + ChatColor.RED + "None");

		lore.add("");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " Click to add an effect.");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " Right click to remove the last effect.");
	}

	@NotNull
	@Override
	public StatData getClearStatData() {
		return new PotionEffectListData();
	}

	@Override
	public void whenApplied(@NotNull ItemStackBuilder item, @NotNull StatData data) {

		// Process Lore
		List<String> lore = new ArrayList<>();
		String effectFormat = ItemStat.translate("effect");
		((PotionEffectListData) data).getEffects().forEach(effect -> {
			lore.add(effectFormat
					.replace("#e",
							MMOItems.plugin.getLanguage().getPotionEffectName(effect.getType())
									+ (effect.getLevel() < 2 ? "" : " " + MMOUtils.intToRoman(effect.getLevel())))
					.replace("#d", durationFormat.format(effect.getDuration())));
		});
		item.getLore().insert("effects", lore);

		// Add tags to item
		item.addItemTag(getAppliedNBT(data));
	}

	@NotNull
	@Override
	public ArrayList<ItemTag> getAppliedNBT(@NotNull StatData data) {

		// Create aJson Array
		JsonArray array = new JsonArray();

		// For every effect
		for (PotionEffectData effect : ((PotionEffectListData) data).getEffects()) {

			// Convert to Json Object
			JsonObject object = new JsonObject();
			object.addProperty("Type", effect.getType().getName());
			object.addProperty("Duration", effect.getDuration());
			object.addProperty("Level", effect.getLevel());
			array.add(object);
		}

		// Make the tag
		ArrayList<ItemTag> ret = new ArrayList<>();
		ret.add(new ItemTag(getNBTPath(), array.toString()));

		// Thats it
		return ret;
	}

	@Override
	public void whenLoaded(@NotNull ReadMMOItem mmoitem) {

		// Find relevant tag
		ArrayList<ItemTag> relevantTags = new ArrayList<>();
		if (mmoitem.getNBT().hasTag(getNBTPath()))
			relevantTags.add(ItemTag.getTagAtPath(getNBTPath(), mmoitem.getNBT(), SupportedNBTTagValues.STRING));

		// Attempt to build data
		StatData data = getLoadedNBT(relevantTags);

		// Valid? Append.
		if (data != null) { mmoitem.setData(this, data); }
	}

	@Nullable
	@Override
	public StatData getLoadedNBT(@NotNull ArrayList<ItemTag> storedTags) {

		// Find tag
		ItemTag rTag = ItemTag.getTagAtPath(getNBTPath(), storedTags);

		// Found?
		if (rTag != null) {

			// Must be Json Array, attempt to parse.
			try {
				PotionEffectListData effects = new PotionEffectListData();

				// Get as Array
				JsonArray array = new JsonParser().parse((String) rTag.getValue()).getAsJsonArray();

				// BUild each element
				for (JsonElement e : array) {

					// Must be object
					if (e.isJsonObject()) {

						// Extract
						JsonObject key = e.getAsJsonObject();

						effects.add(new PotionEffectData(PotionEffectType.getByName(
										key.get("Type").getAsString()),
										key.get("Duration").getAsDouble(),
										key.get("Level").getAsInt()));
					}
				}

				// Success
				return effects;

			} catch (JsonSyntaxException|IllegalStateException exception) {
				/*
				 * OLD ITEM WHICH MUST BE UPDATED.
				 */
			}
		}

		return null;
	}

	@Override
	public boolean onSelfConsume(@NotNull VolatileMMOItem mmo, @NotNull Player player) {

		// Does it have effects?
		if (!mmo.hasData(ItemStats.EFFECTS)) { return false; }

		// Get Data
		PotionEffectListData pelData = (PotionEffectListData) mmo.getData(ItemStats.EFFECTS);

		// Apply
		for (PotionEffectData ped : pelData.getEffects()) {
			if (ped == null) { continue; }

			player.removePotionEffect(ped.getType());
			player.addPotionEffect(ped.toEffect()); }

		// It was applied it seems
		return true;
	}
}
