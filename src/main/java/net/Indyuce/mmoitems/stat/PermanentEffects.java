package net.Indyuce.mmoitems.stat;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

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

public class PermanentEffects extends ItemStat {
	public PermanentEffects() {
		super("PERM_EFFECTS", new ItemStack(Material.POTION), "Permanent Effects",
				new String[] { "The potion effects your", "item grants to the holder." }, new String[] { "!miscellaneous", "!block", "all" });
	}

	@Override
	public RandomStatData whenInitialized(Object object) {
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
	public void whenClicked(EditionInventory inv, InventoryClickEvent event) {
		if (event.getAction() == InventoryAction.PICKUP_ALL)
			new StatEdition(inv, ItemStat.PERM_EFFECTS).enable("Write in the chat the permanent potion effect you want to add.",
					"Format: [POTION_EFFECT] [AMPLIFIER]");

		if (event.getAction() == InventoryAction.PICKUP_HALF) {
			if (inv.getEditedSection().contains("perm-effects")) {
				Set<String> set = inv.getEditedSection().getConfigurationSection("perm-effects").getKeys(false);
				String last = new ArrayList<>(set).get(set.size() - 1);
				inv.getEditedSection().set("perm-effects." + last, null);
				if (set.size() <= 1)
					inv.getEditedSection().set("perm-effects", null);
				inv.registerTemplateEdition();
				inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "Successfully removed " + last.substring(0, 1).toUpperCase()
						+ last.substring(1).toLowerCase() + "�7.");
			}
		}
	}

	@Override
	public void whenInput(EditionInventory inv, String message, Object... info) {
		String[] split = message.split("\\ ");
		Validate.isTrue(split.length == 2,
				message + " is not a valid [POTION_EFFECT] [AMPLIFIER]. Example: 'INCREASE_DAMAGE 4' stands for Strength 4.");

		PotionEffectType effect = null;
		for (PotionEffectType effect1 : PotionEffectType.values())
			if (effect1 != null && effect1.getName().equalsIgnoreCase(split[0].replace("-", "_"))) {
				effect = effect1;
				break;
			}
		Validate.notNull(effect, split[0]
				+ " is not a valid potion effect. All potion effects can be found here: https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/potion/PotionEffectType.html");

		int amplifier = (int) Double.parseDouble(split[1]);

		inv.getEditedSection().set("perm-effects." + effect.getName(), amplifier);
		inv.registerTemplateEdition();
		inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + effect.getName() + " " + amplifier + " successfully added.");
	}

	@Override
	public void whenDisplayed(List<String> lore, Optional<RandomStatData> optional) {

		if (optional.isPresent()) {
			lore.add(ChatColor.GRAY + "Current Value:");
			PotionEffectListData data = (PotionEffectListData) optional.get();
			for (PotionEffectData effect : data.getEffects())
				lore.add(ChatColor.GRAY + "* " + ChatColor.GREEN + MMOUtils.caseOnWords(effect.getType().getName().replace("_", " ").toLowerCase())
						+ " " + MMOUtils.intToRoman(effect.getLevel()));

		} else
			lore.add(ChatColor.GRAY + "Current Value: " + ChatColor.RED + "None");

		lore.add("");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " Click to add an effect.");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " Right click to remove the last effect.");
	}

	@Override
	public void whenApplied(ItemStackBuilder item, StatData data) {
		JsonObject object = new JsonObject();
		List<String> lore = new ArrayList<>();

		String permEffectFormat = ItemStat.translate("perm-effect");
		((PotionEffectListData) data).getEffects().forEach(effect -> {
			lore.add(permEffectFormat.replace("#",
					MMOItems.plugin.getLanguage().getPotionEffectName(effect.getType()) + " " + MMOUtils.intToRoman(effect.getLevel())));
			object.addProperty(effect.getType().getName(), effect.getLevel());
		});

		item.getLore().insert("perm-effects", lore);
		item.addItemTag(new ItemTag("MMOITEMS_PERM_EFFECTS", object.toString()));
	}

	@Override
	public void whenLoaded(ReadMMOItem mmoitem) {
		if (mmoitem.getNBT().hasTag(getNBTPath())) {
			PotionEffectListData effects = new PotionEffectListData();

			JsonObject json = new JsonParser().parse(mmoitem.getNBT().getString("MMOITEMS_PERM_EFFECTS")).getAsJsonObject();
			json.entrySet()
					.forEach(entry -> effects.add(new PotionEffectData(PotionEffectType.getByName(entry.getKey()), entry.getValue().getAsInt())));

			mmoitem.setData(ItemStat.PERM_EFFECTS, effects);
		}
	}
}
