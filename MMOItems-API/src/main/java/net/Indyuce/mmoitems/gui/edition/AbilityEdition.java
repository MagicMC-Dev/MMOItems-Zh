package net.Indyuce.mmoitems.gui.edition;

import io.lumine.mythic.lib.skill.trigger.TriggerType;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.util.MMOUtils;
import net.Indyuce.mmoitems.api.edition.StatEdition;
import net.Indyuce.mmoitems.api.item.template.MMOItemTemplate;
import net.Indyuce.mmoitems.api.util.NumericStatFormula;
import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.api.util.AltChar;
import io.lumine.mythic.lib.version.VersionMaterial;
import net.Indyuce.mmoitems.skill.RegisteredSkill;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class AbilityEdition extends EditionInventory {
	private final String configKey;

	private RegisteredSkill ability;

	private static final DecimalFormat MODIFIER_FORMAT = new DecimalFormat("0.###");
	private static final int[] slots = { 23, 24, 25, 32, 33, 34, 41, 42, 43, 50, 51, 52 };

	public AbilityEdition(Player player, MMOItemTemplate template, String configKey) {
		super(player, template);

		this.configKey = configKey;
	}

	@Override
	public Inventory getInventory() {
		Inventory inv = Bukkit.createInventory(this, 54, "Ability Edition");
		int n = 0;

		String configString = getEditedSection().getString("ability." + configKey + ".type");
		String format = configString == null ? "" : configString.toUpperCase().replace(" ", "_").replace("-", "_").replaceAll("[^A-Z_]", "");
		ability = MMOItems.plugin.getSkills().hasSkill(format) ? MMOItems.plugin.getSkills().getSkill(format) : null;

		ItemStack abilityItem = new ItemStack(Material.BLAZE_POWDER);
		ItemMeta abilityItemMeta = abilityItem.getItemMeta();
		abilityItemMeta.setDisplayName(ChatColor.GREEN + "Ability");
		List<String> abilityItemLore = new ArrayList<>();
		abilityItemLore.add(ChatColor.GRAY + "Choose what ability your weapon will cast.");
		abilityItemLore.add("");
		abilityItemLore.add(
				ChatColor.GRAY + "Current Value: " + (ability == null ? ChatColor.RED + "No ability selected." : ChatColor.GOLD + ability.getName()));
		abilityItemLore.add("");
		abilityItemLore.add(ChatColor.YELLOW + AltChar.listDash + " Left click to select.");
		abilityItemLore.add(ChatColor.YELLOW + AltChar.listDash + " Right click to reset.");
		abilityItemMeta.setLore(abilityItemLore);
		abilityItem.setItemMeta(abilityItemMeta);

		if (ability != null) {
			String castModeConfigString = getEditedSection().getString("ability." + configKey + ".mode");
			String castModeFormat = castModeConfigString == null ? ""
					: castModeConfigString.toUpperCase().replace(" ", "_").replace("-", "_").replaceAll("[^A-Z0-9_]", "");
			TriggerType castMode;
			try {
				castMode = TriggerType.valueOf(castModeFormat);
			} catch (RuntimeException exception) {
				castMode = null;
			}

			ItemStack castModeItem = new ItemStack(Material.ARMOR_STAND);
			ItemMeta castModeItemMeta = castModeItem.getItemMeta();
			castModeItemMeta.setDisplayName(ChatColor.GREEN + "Trigger");
			List<String> castModeItemLore = new ArrayList<>();
			castModeItemLore.add(ChatColor.GRAY + "Choose what action the player needs to");
			castModeItemLore.add(ChatColor.GRAY + "perform in order to cast your ability.");
			castModeItemLore.add("");
			castModeItemLore.add(ChatColor.GRAY + "Current Value: "
					+ (castMode == null ? ChatColor.RED + "No trigger selected." : ChatColor.GOLD + castMode.getName()));
			castModeItemLore.add("");
			castModeItemLore.add(ChatColor.YELLOW + AltChar.listDash + " Left click to select.");
			castModeItemLore.add(ChatColor.YELLOW + AltChar.listDash + " Right click to reset.");
			castModeItemMeta.setLore(castModeItemLore);
			castModeItem.setItemMeta(castModeItemMeta);

			inv.setItem(30, castModeItem);
		}

		if (ability != null) {
			ConfigurationSection section = getEditedSection().getConfigurationSection("ability." + configKey);
			for (String modifier : ability.getHandler().getModifiers()) {
				ItemStack modifierItem = VersionMaterial.GRAY_DYE.toItem();
				ItemMeta modifierItemMeta = modifierItem.getItemMeta();
				modifierItemMeta.setDisplayName(ChatColor.GREEN + MMOUtils.caseOnWords(modifier.toLowerCase().replace("-", " ")));
				List<String> modifierItemLore = new ArrayList<>();
				modifierItemLore.add("" + ChatColor.GRAY + ChatColor.ITALIC + "This is an ability modifier. Changing this");
				modifierItemLore.add("" + ChatColor.GRAY + ChatColor.ITALIC + "value will slightly customize the ability.");
				modifierItemLore.add("");

				try {
					modifierItemLore.add(ChatColor.GRAY + "Current Value: " + ChatColor.GOLD
							+ (section.contains(modifier) ? new NumericStatFormula(section.get(modifier)).toString()
									: MODIFIER_FORMAT.format(ability.getDefaultModifier(modifier))));
				} catch (IllegalArgumentException exception) {
					modifierItemLore.add(ChatColor.GRAY + "Could not read value. Using default");
				}

				modifierItemLore.add(ChatColor.GRAY + "Default Value: " + ChatColor.GOLD + MODIFIER_FORMAT.format(ability.getDefaultModifier(modifier)));
				modifierItemLore.add("");
				modifierItemLore.add(ChatColor.YELLOW + AltChar.listDash + " Click to change this value.");
				modifierItemLore.add(ChatColor.YELLOW + AltChar.listDash + " Right click to reset.");
				modifierItemMeta.setLore(modifierItemLore);
				modifierItem.setItemMeta(modifierItemMeta);

				modifierItem = MythicLib.plugin.getVersion().getWrapper().getNBTItem(modifierItem).addTag(new ItemTag("abilityModifier", modifier))
						.toItem();

				inv.setItem(slots[n++], modifierItem);
			}
		}

		ItemStack glass = VersionMaterial.GRAY_STAINED_GLASS_PANE.toItem();
		ItemMeta glassMeta = glass.getItemMeta();
		glassMeta.setDisplayName(ChatColor.RED + "- No Modifier -");
		glass.setItemMeta(glassMeta);

		ItemStack back = new ItemStack(Material.BARRIER);
		ItemMeta backMeta = back.getItemMeta();
		backMeta.setDisplayName(ChatColor.GREEN + AltChar.rightArrow + " Ability List");
		back.setItemMeta(backMeta);

		while (n < slots.length)
			inv.setItem(slots[n++], glass);

		addEditionInventoryItems(inv, false);
		inv.setItem(28, abilityItem);
		inv.setItem(6, back);

		return inv;
	}

	@Override
	public void whenClicked(InventoryClickEvent event) {
		ItemStack item = event.getCurrentItem();

		event.setCancelled(true);
		if (event.getInventory() != event.getClickedInventory() || !MMOUtils.isMetaItem(item, false))
			return;

		if (item.getItemMeta().getDisplayName().equals(ChatColor.GREEN + AltChar.rightArrow + " Ability List")) {
			new AbilityListEdition(player, template).open(getPreviousPage());
			return;
		}

		if (item.getItemMeta().getDisplayName().equals(ChatColor.GREEN + "Ability")) {
			if (event.getAction() == InventoryAction.PICKUP_ALL)
				new StatEdition(this, ItemStats.ABILITIES, configKey, "ability").enable("Write in the chat the ability you want.",
						"You can access the ability list by typing " + ChatColor.AQUA + "/mi list ability");

			if (event.getAction() == InventoryAction.PICKUP_HALF) {
				if (getEditedSection().contains("ability." + configKey + ".type")) {
					getEditedSection().set("ability." + configKey, null);

					if (getEditedSection().contains("ability") && getEditedSection().getConfigurationSection("ability").getKeys(false).size() == 0)
						getEditedSection().set("ability", null);

					registerTemplateEdition();
					player.sendMessage(MMOItems.plugin.getPrefix() + "Successfully reset the ability.");
				}
			}
			return;
		}

		if (item.getItemMeta().getDisplayName().equals(ChatColor.GREEN + "Trigger")) {
			if (event.getAction() == InventoryAction.PICKUP_ALL) {
				new StatEdition(this, ItemStats.ABILITIES, configKey, "mode").enable();

				player.sendMessage("");
				player.sendMessage("" + ChatColor.GREEN + ChatColor.BOLD + "Available Triggers");
				for (TriggerType castMode : TriggerType.values())
					player.sendMessage("* " + ChatColor.GREEN + castMode.name());
			}

			if (event.getAction() == InventoryAction.PICKUP_HALF && getEditedSection().contains("ability." + configKey + ".mode")) {
				getEditedSection().set("ability." + configKey + ".mode", null);
				registerTemplateEdition();
				player.sendMessage(MMOItems.plugin.getPrefix() + "Successfully reset the ability trigger.");
			}
			return;
		}

		String tag = MythicLib.plugin.getVersion().getWrapper().getNBTItem(item).getString("abilityModifier");
		if (tag.equals(""))
			return;

		if (event.getAction() == InventoryAction.PICKUP_ALL)
			new StatEdition(this, ItemStats.ABILITIES, configKey, tag).enable("Write in the chat the value you want.");

		if (event.getAction() == InventoryAction.PICKUP_HALF) {
			if (getEditedSection().contains("ability." + configKey + "." + tag)) {
				getEditedSection().set("ability." + configKey + "." + tag, null);
				registerTemplateEdition();
				player.sendMessage(MMOItems.plugin.getPrefix() + "Successfully reset " + ChatColor.GOLD + MMOUtils.caseOnWords(tag.replace("-", " "))
						+ ChatColor.GRAY + ".");
			}
		}
	}
}