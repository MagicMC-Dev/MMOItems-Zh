package net.Indyuce.mmoitems.stat;

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.meta.PotionMeta;

import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.edition.StatEdition;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.data.ColorData;
import net.Indyuce.mmoitems.stat.data.random.RandomStatData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.StringStat;
import io.lumine.mythic.lib.api.util.AltChar;
import org.jetbrains.annotations.NotNull;

public class PotionColor extends StringStat {
	public PotionColor() {
		super("POTION_COLOR", Material.POTION, "Potion Color",
				new String[] { "The color of your potion.", "(Doesn't impact the effects)." }, new String[] { "all" }, Material.POTION,
				Material.SPLASH_POTION, Material.LINGERING_POTION, Material.TIPPED_ARROW);
	}

	@Override
	public RandomStatData whenInitialized(Object object) {
		Validate.isTrue(object instanceof String, "Must specify a string");
		return new ColorData((String) object);
	}

	@Override
	public void whenClicked(@NotNull EditionInventory inv, @NotNull InventoryClickEvent event) {
		if (event.getAction() == InventoryAction.PICKUP_ALL)
			new StatEdition(inv, ItemStats.POTION_COLOR).enable("Write in the chat the RGB color you want.",
					ChatColor.AQUA + "Format: {Red} {Green} {Blue}");

		if (event.getAction() == InventoryAction.PICKUP_HALF) {
			inv.getEditedSection().set("potion-color", null);
			inv.registerTemplateEdition();
			inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "Successfully removed Potion Color.");
		}
	}

	@Override
	public void whenInput(@NotNull EditionInventory inv, @NotNull String message, Object... info) {
		String[] split = message.split(" ");
		Validate.isTrue(split.length == 3, "Use this format: {Red} {Green} {Blue}. Example: '75 0 130' stands for Purple.");

		for (String str : split) {
			int k = Integer.parseInt(str);
			Validate.isTrue(k >= 0 && k < 256, "Color must be between 0 and 255");
		}

		inv.getEditedSection().set("potion-color", message);
		inv.registerTemplateEdition();
		inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "Potion Color successfully changed to " + message + ".");
	}

	@Override
	public void whenDisplayed(List<String> lore, Optional<RandomStatData> statData) {

		lore.add(statData.isPresent() ? ChatColor.GREEN + statData.get().toString() : ChatColor.RED + "Uncolored");

		lore.add("");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " Click to change this value.");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " Right click to remove the potion color.");
	}

	@Override
	public void whenApplied(@NotNull ItemStackBuilder item, @NotNull StatData data) {
		if (item.getItemStack().getType().name().contains("POTION") || item.getItemStack().getType() == Material.TIPPED_ARROW)
			((PotionMeta) item.getMeta()).setColor(((ColorData) data).getColor());
	}
}
