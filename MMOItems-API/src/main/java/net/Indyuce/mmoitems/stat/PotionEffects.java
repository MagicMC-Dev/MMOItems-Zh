package net.Indyuce.mmoitems.stat;

import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.api.util.AltChar;
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
import net.Indyuce.mmoitems.stat.type.ItemStat;
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class PotionEffects extends ItemStat<RandomPotionEffectListData, PotionEffectListData> {
	public PotionEffects() {
		super("POTION_EFFECT", Material.POTION, "Potion Effects", new String[] { "The effects of your potion.", "(May have an impact on color).", "Does NOT support tipped arrows." },
				new String[] { "all" }, Material.POTION, Material.SPLASH_POTION, Material.LINGERING_POTION);
	}

	@Override
	public RandomPotionEffectListData whenInitialized(Object object) {
		Validate.isTrue(object instanceof ConfigurationSection, "Must specify a config section");
		return new RandomPotionEffectListData((ConfigurationSection) object);
	}

	@Override
	public void whenClicked(@NotNull EditionInventory inv, @NotNull InventoryClickEvent event) {
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
	public void whenInput(@NotNull EditionInventory inv, @NotNull String message, Object... info) {
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
	public void whenDisplayed(List<String> lore, Optional<RandomPotionEffectListData> statData) {

		if (statData.isPresent()) {
			lore.add(ChatColor.GRAY + "Current Value:");
			RandomPotionEffectListData data = statData.get();
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

	@NotNull
	@Override
	public PotionEffectListData getClearStatData() {
		return new PotionEffectListData();
	}

	@Override
	public void whenApplied(@NotNull ItemStackBuilder item, @NotNull PotionEffectListData data) {
		if (item.getItemStack().getType().name().contains("POTION"))
			for (PotionEffectData effect : data.getEffects())
				((PotionMeta) item.getMeta()).addCustomEffect(effect.toEffect(), false);
	}

	/**
	 * No item tags are added onto the item, I suppose it is saved as the Potion Effects themselves.
	 */
	@NotNull
	@Override
	public ArrayList<ItemTag> getAppliedNBT(@NotNull PotionEffectListData data) {
		throw new NotImplementedException();
	}

	@Override
	public void whenLoaded(@NotNull ReadMMOItem mmoitem) {

		if (!(mmoitem.getNBT().getItem().getItemMeta() instanceof PotionMeta))
			return;

		// Read all custom potion effects
		PotionEffectListData list = new PotionEffectListData();
		for (PotionEffect effect : ((PotionMeta) mmoitem.getNBT().getItem().getItemMeta()).getCustomEffects())
			list.add(new PotionEffectData(effect));

		mmoitem.setData(this, list);
	}

	@Nullable
	@Override
	public PotionEffectListData getLoadedNBT(@NotNull ArrayList<ItemTag> storedTags) {
		throw new NotImplementedException();
	}
}
