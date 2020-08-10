package net.Indyuce.mmoitems.stat;

import java.util.List;

import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.ConfigFile;
import net.Indyuce.mmoitems.api.CustomSound;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.Indyuce.mmoitems.api.item.mmoitem.ReadMMOItem;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.gui.edition.SoundsEdition;
import net.Indyuce.mmoitems.stat.data.SoundData;
import net.Indyuce.mmoitems.stat.data.SoundListData;
import net.Indyuce.mmoitems.stat.data.random.RandomStatData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.Indyuce.mmoitems.stat.type.ProperStat;
import net.mmogroup.mmolib.api.item.ItemTag;
import net.mmogroup.mmolib.api.util.AltChar;

public class CustomSounds extends ItemStat implements ProperStat {
	public CustomSounds() {
		super("SOUNDS", new ItemStack(Material.JUKEBOX), "Custom Sounds", new String[] { "The custom sounds your item will use." },
				new String[] { "all" });
	}

	@Override
	public SoundListData whenInitialized(Object object) {
		Validate.isTrue(object instanceof ConfigurationSection, "Must specify a config section");
		ConfigurationSection config = (ConfigurationSection) object;

		SoundListData sounds = new SoundListData();

		for (CustomSound sound : CustomSound.values()) {
			String path = sound.getName().replace(" ", "-").toLowerCase();
			if (!config.contains(path))
				continue;

			String soundName = config.getString(path + ".sound");
			double vol = config.getDouble(path + ".volume");
			double pit = config.getDouble(path + ".pitch");
			if (!soundName.isEmpty() && vol > 0)
				sounds.set(sound, soundName, vol, pit);
		}

		return sounds;
	}

	@Override
	public RandomStatData whenInitializedGeneration(Object object) {
		return whenInitialized(object);
	}

	@Override
	public void whenClicked(EditionInventory inv, InventoryClickEvent event) {
		ConfigFile config = inv.getEdited().getType().getConfigFile();
		if (event.getAction() == InventoryAction.PICKUP_ALL)
			new SoundsEdition(inv.getPlayer(), inv.getEdited()).open(inv.getPage());

		if (event.getAction() == InventoryAction.PICKUP_HALF)
			if (config.getConfig().getConfigurationSection(inv.getEdited().getId()).contains("sounds")) {
				config.getConfig().set(inv.getEdited().getId() + ".sounds", null);
				inv.registerTemplateEdition(config, true);
				inv.open();
				inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "Custom Sounds successfully removed.");
			}
	}

	@Override
	public boolean whenInput(EditionInventory inv, ConfigFile config, String message, Object... info) {
		String soundsPath = SoundsEdition.correspondingSlot.get(info[0]);
		String[] split = message.split("\\ ");
		if (split.length != 3) {
			inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + message + " is not a valid [SOUND NAME] [VOLUME] [PITCH].");
			inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + "Example: 'mob.giant.roar 1 1'");
			return false;
		}

		String soundName = split[0].replace("-", "_");

		double volume = 0;
		try {
			volume = Double.parseDouble(split[1]);
		} catch (Exception e1) {
			inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + split[1] + " is not a valid number!");
			return false;
		}

		double pitch = 0;
		try {
			pitch = Double.parseDouble(split[2]);
		} catch (Exception e1) {
			inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + split[2] + " is not a valid number!");
			return false;
		}

		config.getConfig().set(inv.getEdited().getId() + ".sounds." + soundsPath + ".sound", soundName);
		config.getConfig().set(inv.getEdited().getId() + ".sounds." + soundsPath + ".volume", volume);
		config.getConfig().set(inv.getEdited().getId() + ".sounds." + soundsPath + ".pitch", pitch);

		inv.registerTemplateEdition(config, true);
		inv.open();
		inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + MMOUtils.caseOnWords(soundsPath.replace(".", " ")) + ChatColor.GRAY
				+ " successfully changed to '" + soundName + "'.");
		return true;
	}

	@Override
	public void whenDisplayed(List<String> lore, MMOItem mmoitem) {

		if (mmoitem.hasData(this)) {
			lore.add(ChatColor.GRAY + "Current Value:");
			SoundListData data = (SoundListData) mmoitem.getData(this);
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
				sounds.set(sound, soundName, mmoitem.getNBT().getDouble("MMOITEMS_SOUND_" + sound.name() + "_VOL"),
						mmoitem.getNBT().getDouble("MMOITEMS_SOUND_" + sound.name() + "_PIT"));
		}

		if (sounds.getCustomSounds().size() > 0)
			mmoitem.setData(ItemStat.CUSTOM_SOUNDS, sounds);
	}
}
