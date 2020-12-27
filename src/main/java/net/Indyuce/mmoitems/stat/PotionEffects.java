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
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffectType;

import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.edition.StatEdition;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.ReadMMOItem;
import net.Indyuce.mmoitems.api.util.NumericStatFormula;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.data.PotionEffectData;
import net.Indyuce.mmoitems.stat.data.PotionEffectListData;
import net.Indyuce.mmoitems.stat.data.StringData;
import net.Indyuce.mmoitems.stat.data.random.RandomPotionEffectData;
import net.Indyuce.mmoitems.stat.data.random.RandomPotionEffectListData;
import net.Indyuce.mmoitems.stat.data.random.RandomStatData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.mmogroup.mmolib.api.util.AltChar;

public class PotionEffects extends ItemStat {
	public PotionEffects() {
		super("POTION_EFFECT", Material.POTION, "Potion Effects", new String[] { "The effects of your potion.", "(May have an impact on color)." },
				new String[] { "all" }, Material.POTION, Material.SPLASH_POTION, Material.LINGERING_POTION, Material.TIPPED_ARROW);
	}

	@Override
	public RandomStatData whenInitialized(Object object) {
		Validate.isTrue(object instanceof ConfigurationSection, "Must specify a config section");
		return new RandomPotionEffectListData((ConfigurationSection) object);
	}

	@Override
	public void whenClicked(EditionInventory inv, InventoryClickEvent event) {
		if (event.getAction() == InventoryAction.PICKUP_ALL)
			new StatEdition(inv, ItemStats.POTION_EFFECTS).enable("Write in the chat the potion effect you want to add.",
					ChatColor.AQUA + "Format: {Effect Name} {Duration} {Amplifier}",
					ChatColor.AQUA + "Other Format: {Effect Name}|{Duration Numeric Formula}|{Amplifier Numeric Formula}");

		if (event.getAction() == InventoryAction.PICKUP_HALF && inv.getEditedSection().contains("potion-effect")) {
			Set<String> set = inv.getEditedSection().getConfigurationSection("potion-effect").getKeys(false);
			String last = new ArrayList<>(set).get(set.size() - 1);
			inv.getEditedSection().set("potion-effect." + last, null);
			if (set.size() <= 1)
				inv.getEditedSection().set("potion-effect", null);
			inv.registerTemplateEdition();
			inv.getPlayer()
					.sendMessage(MMOItems.plugin.getPrefix() + "Successfully removed " + ChatColor.GOLD + formatName(last) + ChatColor.GRAY + ".");
		}
	}

	@Override
	public void whenInput(EditionInventory inv, String message, Object... info) {
		if (message.contains("|")) {
			String[] split = message.split("\\|");

			PotionEffectType effect = PotionEffectType.getByName(split[0].replace("-", "_"));
			Validate.notNull(effect, split[0] + " is not a valid potion effect. All potion effects can be found here: "
					+ "https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/potion/PotionEffectType.html");

			NumericStatFormula duration = new NumericStatFormula(split[1]), amplifier = new NumericStatFormula(split[2]);
			duration.fillConfigurationSection(inv.getEditedSection(), "potion-effect." + effect.getName() + ".duration");
			amplifier.fillConfigurationSection(inv.getEditedSection(), "potion-effect." + effect.getName() + ".amplifier");
			inv.registerTemplateEdition();
			inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + ChatColor.GOLD + formatName(effect) + ChatColor.GRAY + " successfully added.");
			return;
		}

		String[] split = message.split(" ");
		Validate.isTrue(split.length == 3,
				message + " is not a valid {Effect Name} {Duration} {Amplifier}. Example: 'FAST_DIGGING 30 3' stands for Haste 3 for 30 seconds.");

		PotionEffectType effect = PotionEffectType.getByName(split[0].replace("-", "_"));
		Validate.notNull(effect, split[0] + " is not a valid potion effect. All potion effects can be found here: "
				+ "https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/potion/PotionEffectType.html");

		double duration = MMOUtils.parseDouble(split[1]);
		int amplifier = (int) MMOUtils.parseDouble(split[2]);

		inv.getEditedSection().set("potion-effect." + effect.getName() + ".duration", duration);
		inv.getEditedSection().set("potion-effect." + effect.getName() + ".amplifier", amplifier);
		inv.registerTemplateEdition();
		inv.getPlayer().sendMessage(
				MMOItems.plugin.getPrefix() + ChatColor.GOLD + formatName(effect) + " " + amplifier + ChatColor.GRAY + " successfully added.");
	}

	private String formatName(PotionEffectType effect) {
		return formatName(effect.getName());
	}

	private String formatName(String input) {
		return MMOUtils.caseOnWords(input.replace("_", " ").toLowerCase());
	}

	@Override
	public void whenDisplayed(List<String> lore, Optional<RandomStatData> statData) {

		if (statData.isPresent()) {
			lore.add(ChatColor.GRAY + "Current Value:");
			RandomPotionEffectListData data = (RandomPotionEffectListData) statData.get();
			for (RandomPotionEffectData effect : data.getEffects())
				lore.add(ChatColor.GRAY + "* " + ChatColor.GREEN + MMOUtils.caseOnWords(effect.getType().getName().toLowerCase().replace("_", " "))
						+ " " + effect.getAmplifier().toString() + " " + ChatColor.GRAY + "(" + ChatColor.GREEN + effect.getDuration().toString()
						+ ChatColor.GRAY + "s)");
		} else
			lore.add(ChatColor.GRAY + "Current Value: " + ChatColor.RED + "None");

		lore.add("");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " Click to add an effect.");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " Right click to remove the last effect.");
	}

	@Override
	public void whenApplied(ItemStackBuilder item, StatData data) {
		if (item.getItemStack().getType().name().contains("POTION") || item.getItemStack().getType() == Material.TIPPED_ARROW)
			for (PotionEffectData effect : ((PotionEffectListData) data).getEffects())
				((PotionMeta) item.getMeta()).addCustomEffect(effect.toEffect(), false);
	}

	@Override
	public void whenLoaded(ReadMMOItem mmoitem) {
		if (mmoitem.getNBT().hasTag(getNBTPath()))
			mmoitem.setData(this, new StringData(mmoitem.getNBT().getString(getNBTPath())));
	}
}
