package net.Indyuce.mmoitems.stat;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ConfigFile;
import net.Indyuce.mmoitems.api.edition.StatEdition;
import net.Indyuce.mmoitems.api.item.MMOItem;
import net.Indyuce.mmoitems.api.item.ReadMMOItem;
import net.Indyuce.mmoitems.api.item.build.MMOItemBuilder;
import net.Indyuce.mmoitems.api.itemgen.RandomStatData;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.data.StringListData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.asangarin.hexcolors.ColorParse;
import net.mmogroup.mmolib.api.item.ItemTag;
import net.mmogroup.mmolib.api.util.AltChar;
import net.mmogroup.mmolib.version.VersionMaterial;

public class CompatibleTypes extends ItemStat {
	public CompatibleTypes() {
		super("COMPATIBLE_TYPES", new ItemStack(VersionMaterial.COMMAND_BLOCK.toMaterial()), "Compatible Types",
				new String[] { "The item types this skin is", "compatible with." }, new String[] { "skin" });
	}

	@Override
	@SuppressWarnings("unchecked")
	public StringListData whenInitialized(Object object) {
		Validate.isTrue(object instanceof List<?>, "Must specify a string list");
		return new StringListData((List<String>) object);
	}

	@Override
	public RandomStatData whenInitializedGeneration(Object object) {
		return whenInitialized(object);
	}

	@Override
	public void whenClicked(EditionInventory inv, InventoryClickEvent event) {
		ConfigFile config = inv.getEdited().getType().getConfigFile();
		if (event.getAction() == InventoryAction.PICKUP_ALL)
			new StatEdition(inv, ItemStat.COMPATIBLE_TYPES).enable("Write in the chat the name of the type you want to add.");

		if (event.getAction() == InventoryAction.PICKUP_HALF) {
			if (config.getConfig().getConfigurationSection(inv.getEdited().getId()).contains("compatible-types")) {
				List<String> lore = config.getConfig().getStringList(inv.getEdited().getId() + ".compatible-types");
				if (lore.size() < 1)
					return;

				String last = lore.get(lore.size() - 1);
				lore.remove(last);
				config.getConfig().set(inv.getEdited().getId() + ".compatible-types", lore);
				inv.registerItemEdition(config);
				inv.open();
				inv.getPlayer().sendMessage(
						MMOItems.plugin.getPrefix() + "Successfully removed '" + new ColorParse('&', last).toChatColor() + ChatColor.GRAY + "'.");
			}
		}
	}

	@Override
	public boolean whenInput(EditionInventory inv, ConfigFile config, String message, Object... info) {
		List<String> lore = config.getConfig().getConfigurationSection(inv.getEdited().getId()).contains("compatible-types")
				? config.getConfig().getStringList(inv.getEdited().getId() + ".compatible-types")
				: new ArrayList<>();
		lore.add(message.toUpperCase());
		config.getConfig().set(inv.getEdited().getId() + ".compatible-types", lore);
		inv.registerItemEdition(config);
		inv.open();
		inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "Compatible Types successfully added.");
		return true;
	}

	@Override
	public void whenDisplayed(List<String> lore, MMOItem mmoitem) {

		if (mmoitem.hasData(this)) {
			lore.add(ChatColor.GRAY + "Current Value:");
			StringListData data = (StringListData) mmoitem.getData(this);
			data.getList().forEach(str -> lore.add(ChatColor.GRAY + new ColorParse('&', str).toChatColor()));

		} else
			lore.add(ChatColor.GRAY + "Current Value: " + ChatColor.RED + "Compatible with any item.");

		lore.add("");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " Click to add a new type.");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " Right click to remove the last type.");
	}

	@Override
	public void whenApplied(MMOItemBuilder item, StatData data) {
		List<String> compatibleTypes = new ArrayList<>();
		JsonArray array = new JsonArray();
		((StringListData) data).getList().forEach(line -> {
			line = new ColorParse('&', line).toChatColor();
			array.add(line);
			compatibleTypes.add(line);
		});
		item.getLore().insert("compatible-types", compatibleTypes);
		item.addItemTag(new ItemTag("MMOITEMS_COMPATIBLE_TYPES", array.toString()));
	}

	@Override
	public void whenLoaded(ReadMMOItem mmoitem) {
		if (mmoitem.getNBT().hasTag("MMOITEMS_COMPATIBLE_TYPES"))
			mmoitem.setData(ItemStat.COMPATIBLE_TYPES,
					new StringListData(new JsonParser().parse(mmoitem.getNBT().getString("MMOITEMS_COMPATIBLE_TYPES")).getAsJsonArray()));
	}
}
