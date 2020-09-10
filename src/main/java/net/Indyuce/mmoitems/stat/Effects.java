package net.Indyuce.mmoitems.stat;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.edition.StatEdition;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.ReadMMOItem;
import net.Indyuce.mmoitems.api.util.NumericStatFormula;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.data.PotionEffectData;
import net.Indyuce.mmoitems.stat.data.PotionEffectListData;
import net.Indyuce.mmoitems.stat.data.random.RandomPotionEffectData;
import net.Indyuce.mmoitems.stat.data.random.RandomPotionEffectListData;
import net.Indyuce.mmoitems.stat.data.random.RandomStatData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.mmogroup.mmolib.api.item.ItemTag;
import net.mmogroup.mmolib.api.util.AltChar;
import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import java.text.DecimalFormat;
import java.util.*;

public class Effects extends ItemStat {
	private final DecimalFormat durationFormat = new DecimalFormat("0.#");

	public Effects() {
		super("EFFECTS", new ItemStack(Material.POTION), "Effects", new String[] { "The potion effects your", "consumable item grants." },
				new String[] { "consumable" });
	}

	@Override
	public RandomStatData whenInitialized(Object object) {
		Validate.isTrue(object instanceof ConfigurationSection, "Must specify a config section");
		return new RandomPotionEffectListData((ConfigurationSection) object);
	}

	@Override
	public void whenClicked(EditionInventory inv, InventoryClickEvent event) {
		if (event.getAction() == InventoryAction.PICKUP_ALL)
			new StatEdition(inv, ItemStat.EFFECTS).enable("Write in the chat the permanent potion effect you want to add.",
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
	public void whenInput(EditionInventory inv, String message, Object... info) {
		String[] split = message.split("\\|");
		Validate.isTrue(split.length > 1, "Use this format: {Potion Effect Name}|{Duration Numeric Formula}|{Amplifier Numeric Formula}.");

		PotionEffectType effect = PotionEffectType.getByName(split[0].replace("-", "_").replace(" ", "_").toUpperCase());
		Validate.notNull(effect, split[0] + " is not a valid potion effect. All potion effects "
				+ "can be found here: https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/potion/PotionEffectType.html");

		NumericStatFormula duration = new NumericStatFormula(split[1]);
		NumericStatFormula amplifier = split.length > 2 ? new NumericStatFormula(split[2]) : new NumericStatFormula(1, 0, 0, 0);

		duration.fillConfigurationSection(inv.getEditedSection(), "effects." + effect.getName() + ".duration");
		amplifier.fillConfigurationSection(inv.getEditedSection(), "effects." + effect.getName() + ".amplifier");
		inv.registerTemplateEdition();
		inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + effect.getName() + " " + amplifier + " successfully added.");
	}

	@Override
	public void whenDisplayed(List<String> lore, Optional<RandomStatData> optional) {

		if (optional.isPresent()) {
			lore.add(ChatColor.GRAY + "Current Value:");
			RandomPotionEffectListData data = (RandomPotionEffectListData) optional.get();
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

	@Override
	public void whenApplied(ItemStackBuilder item, StatData data) {
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
	}

	@Override
	public void whenLoaded(ReadMMOItem mmoitem) {
		if (mmoitem.getNBT().hasTag(getNBTPath()))
			try {
				PotionEffectListData effects = new PotionEffectListData();

				new JsonParser().parse(mmoitem.getNBT().getString("MMOITEMS_EFFECTS")).getAsJsonArray().forEach(element -> {
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
