package net.Indyuce.mmoitems.stat;

import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.CustomSound;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.ReadMMOItem;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.gui.edition.SoundsEdition;
import net.Indyuce.mmoitems.stat.data.SoundData;
import net.Indyuce.mmoitems.stat.data.SoundListData;
import net.Indyuce.mmoitems.stat.data.random.RandomStatData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.GemStoneStat;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.mmogroup.mmolib.api.item.ItemTag;
import net.mmogroup.mmolib.api.util.AltChar;
import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.List;

public class CustomSounds extends ItemStat implements GemStoneStat {
	public CustomSounds() {
		super("SOUNDS", Material.JUKEBOX, "Custom Sounds", new String[] { "The custom sounds your item will use." },
				new String[] { "all" });
	}

	@Override
	public SoundListData whenInitialized(Object object) {
		Validate.isTrue(object instanceof ConfigurationSection, "Must specify a config section");
		ConfigurationSection config = (ConfigurationSection) object;

		SoundListData sounds = new SoundListData();

		for (CustomSound sound : CustomSound.values()) {
			String path = sound.name().replace("_", "-").toLowerCase();
			if (config.contains(path))
				sounds.set(sound, new SoundData(config.get(path)));
		}

		return sounds;
	}

	@Override
	public void whenClicked(EditionInventory inv, InventoryClickEvent event) {
		if (event.getAction() == InventoryAction.PICKUP_ALL)
			new SoundsEdition(inv.getPlayer(), inv.getEdited()).open(inv.getPage());

		if (event.getAction() == InventoryAction.PICKUP_HALF)
			if (inv.getEditedSection().contains("sounds")) {
				inv.getEditedSection().set("sounds", null);
				inv.registerTemplateEdition();
				inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "Custom Sounds successfully removed.");
			}
	}

	@Override
	public void whenInput(EditionInventory inv, String message, Object... info) {
		String soundsPath = (String) info[0];
		String[] split = message.split(" ");
		Validate.isTrue(split.length == 3, message + " is not a valid [SOUND NAME] [VOLUME] [PITCH].");

		String soundName = split[0].replace("-", "_");
		double volume = MMOUtils.parseDouble(split[1]);
		double pitch = MMOUtils.parseDouble(split[2]);

		inv.getEditedSection().set("sounds." + soundsPath + ".sound", soundName);
		inv.getEditedSection().set("sounds." + soundsPath + ".volume", volume);
		inv.getEditedSection().set("sounds." + soundsPath + ".pitch", pitch);

		inv.registerTemplateEdition();
		inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + MMOUtils.caseOnWords(soundsPath.replace(".", " ")) + ChatColor.GRAY
				+ " successfully changed to '" + soundName + "'.");
	}

	@Override
	public void whenDisplayed(List<String> lore, RandomStatData statData) {

		if (statData.isPresent()) {
			lore.add(ChatColor.GRAY + "Current Value:");
			SoundListData data = (SoundListData) statData;
			data.mapData()
					.forEach((sound,
							soundData) -> lore.add(ChatColor.GRAY + "* " + ChatColor.GREEN
									+ MMOUtils.caseOnWords(sound.getName().toLowerCase().replace("-", " ").replace("_", " ")) + ChatColor.GRAY + ": "
									+ ChatColor.RED + soundData.getVolume() + " " + soundData.getPitch()));
		} else
			lore.add(ChatColor.GRAY + "Current Value: " + ChatColor.RED + "None");

		lore.add("");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " Click to access the sounds edition menu.");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " Right click to remove all custom sounds.");
	}

	@Override
	public void whenApplied(ItemStackBuilder item, StatData data) {
		SoundListData sounds = (SoundListData) data;

		sounds.getCustomSounds().forEach(sound -> {
			SoundData value = sounds.get(sound);
			String s = sound.getName().replace(" ", "_").toUpperCase();

			item.addItemTag(new ItemTag("MMOITEMS_SOUND_" + s, value.getSound()));
			item.addItemTag(new ItemTag("MMOITEMS_SOUND_" + s + "_VOL", value.getVolume()));
			item.addItemTag(new ItemTag("MMOITEMS_SOUND_" + s + "_PIT", value.getPitch()));
		});
	}

	@Override
	public void whenLoaded(ReadMMOItem mmoitem) {
		SoundListData sounds = new SoundListData();

		for (CustomSound sound : CustomSound.values()) {
			String soundName = mmoitem.getNBT().getString("MMOITEMS_SOUND_" + sound.name());
			if (soundName != null && !soundName.isEmpty())
				sounds.set(sound, new SoundData(soundName, mmoitem.getNBT().getDouble("MMOITEMS_SOUND_" + sound.name() + "_VOL"),
						mmoitem.getNBT().getDouble("MMOITEMS_SOUND_" + sound.name() + "_PIT")));
		}

		if (sounds.getCustomSounds().size() > 0)
			mmoitem.setData(ItemStats.CUSTOM_SOUNDS, sounds);
	}
}
