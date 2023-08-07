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
		Inventory inv = Bukkit.createInventory(this, 54, "技能编辑器");
		int n = 0;

		String configString = getEditedSection().getString("ability." + configKey + ".type");
		String format = configString == null ? "" : configString.toUpperCase().replace(" ", "_").replace("-", "_").replaceAll("[^A-Z_]", "");
		ability = MMOItems.plugin.getSkills().hasSkill(format) ? MMOItems.plugin.getSkills().getSkill(format) : null;

		ItemStack abilityItem = new ItemStack(Material.BLAZE_POWDER);
		ItemMeta abilityItemMeta = abilityItem.getItemMeta();
		abilityItemMeta.setDisplayName(ChatColor.GREEN + "技能");
		List<String> abilityItemLore = new ArrayList<>();
		abilityItemLore.add(ChatColor.GRAY + "选择你的武器将施展的技能");
		abilityItemLore.add("");
		abilityItemLore.add(
				ChatColor.GRAY + "当前值: " + (ability == null ? ChatColor.RED + "没有选择技能" : ChatColor.GOLD + ability.getName()));
		abilityItemLore.add("");
		abilityItemLore.add(ChatColor.YELLOW + AltChar.listDash + "左键单击进行选择");
		abilityItemLore.add(ChatColor.YELLOW + AltChar.listDash + "右键单击以重置");
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
			castModeItemMeta.setDisplayName(ChatColor.GREEN + "触发器");
			List<String> castModeItemLore = new ArrayList<>();
			castModeItemLore.add(ChatColor.GRAY + "选择玩家需要采取的行动");
			castModeItemLore.add(ChatColor.GRAY + "执行以施展的技能");
			castModeItemLore.add("");
			castModeItemLore.add(ChatColor.GRAY + "当前值:"
					+ (castMode == null ? ChatColor.RED + "未选择触发器" : ChatColor.GOLD + castMode.getName()));
			castModeItemLore.add("");
			castModeItemLore.add(ChatColor.YELLOW + AltChar.listDash + "左键单击进行选择");
			castModeItemLore.add(ChatColor.YELLOW + AltChar.listDash + "右键单击重置");
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
				modifierItemLore.add("" + ChatColor.GRAY + ChatColor.ITALIC + "这是一个技能修正,改变这个");
				modifierItemLore.add("" + ChatColor.GRAY + ChatColor.ITALIC + "值会稍微改变自定义技能");
				modifierItemLore.add("");

				try {
					modifierItemLore.add(ChatColor.GRAY + "当前值: " + ChatColor.GOLD
							+ (section.contains(modifier) ? new NumericStatFormula(section.get(modifier)).toString()
									: MODIFIER_FORMAT.format(ability.getDefaultModifier(modifier))));
				} catch (IllegalArgumentException exception) {
					modifierItemLore.add(ChatColor.GRAY + "无法读取,使用默认值");
				}

				modifierItemLore.add(ChatColor.GRAY + "默认值: " + ChatColor.GOLD + MODIFIER_FORMAT.format(ability.getDefaultModifier(modifier)));
				modifierItemLore.add("");
				modifierItemLore.add(ChatColor.YELLOW + AltChar.listDash + "左键单击进行选择");
				modifierItemLore.add(ChatColor.YELLOW + AltChar.listDash + "右键单击重置");
				modifierItemMeta.setLore(modifierItemLore);
				modifierItem.setItemMeta(modifierItemMeta);

				modifierItem = MythicLib.plugin.getVersion().getWrapper().getNBTItem(modifierItem).addTag(new ItemTag("abilityModifier", modifier))
						.toItem();

				inv.setItem(slots[n++], modifierItem);
			}
		}

		ItemStack glass = VersionMaterial.GRAY_STAINED_GLASS_PANE.toItem();
		ItemMeta glassMeta = glass.getItemMeta();
		glassMeta.setDisplayName(ChatColor.RED + "- 无修改 -");
		glass.setItemMeta(glassMeta);

		ItemStack back = new ItemStack(Material.BARRIER);
		ItemMeta backMeta = back.getItemMeta();
		backMeta.setDisplayName(ChatColor.GREEN + AltChar.rightArrow + " 技能列表");
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

		if (item.getItemMeta().getDisplayName().equals(ChatColor.GREEN + AltChar.rightArrow + " 技能列表")) {
			new AbilityListEdition(player, template).open(getPreviousPage());
			return;
		}

		if (item.getItemMeta().getDisplayName().equals(ChatColor.GREEN + "技能")) {
			if (event.getAction() == InventoryAction.PICKUP_ALL)
				new StatEdition(this, ItemStats.ABILITIES, configKey, "ability").enable("在聊天中写下你想要的技能",
						"您可以通过命令访问技能列表" + ChatColor.AQUA + "/mi list ability");

			if (event.getAction() == InventoryAction.PICKUP_HALF) {
				if (getEditedSection().contains("ability." + configKey + ".type")) {
					getEditedSection().set("ability." + configKey, null);

					if (getEditedSection().contains("ability") && getEditedSection().getConfigurationSection("ability").getKeys(false).size() == 0)
						getEditedSection().set("ability", null);

					registerTemplateEdition();
					player.sendMessage(MMOItems.plugin.getPrefix() + "技能重置成功");
				}
			}
			return;
		}

		if (item.getItemMeta().getDisplayName().equals(ChatColor.GREEN + "触发器")) {
			if (event.getAction() == InventoryAction.PICKUP_ALL) {
				new StatEdition(this, ItemStats.ABILITIES, configKey, "mode").enable();

				player.sendMessage("");
				player.sendMessage("" + ChatColor.GREEN + ChatColor.BOLD + "可用的触发器");
				for (TriggerType castMode : TriggerType.values())
					player.sendMessage("* " + ChatColor.GREEN + castMode.name());
			}

			if (event.getAction() == InventoryAction.PICKUP_HALF && getEditedSection().contains("ability." + configKey + ".mode")) {
				getEditedSection().set("ability." + configKey + ".mode", null);
				registerTemplateEdition();
				player.sendMessage(MMOItems.plugin.getPrefix() + "成功重置技能触发器");
			}
			return;
		}

		String tag = MythicLib.plugin.getVersion().getWrapper().getNBTItem(item).getString("abilityModifier");
		if (tag.equals(""))
			return;

		if (event.getAction() == InventoryAction.PICKUP_ALL)
			new StatEdition(this, ItemStats.ABILITIES, configKey, tag).enable("在聊天中写下您想要的值");

		if (event.getAction() == InventoryAction.PICKUP_HALF) {
			if (getEditedSection().contains("ability." + configKey + "." + tag)) {
				getEditedSection().set("ability." + configKey + "." + tag, null);
				registerTemplateEdition();
				player.sendMessage(MMOItems.plugin.getPrefix() + "重置成功" + ChatColor.GOLD + MMOUtils.caseOnWords(tag.replace("-", " "))
						+ ChatColor.GRAY + "");
			}
		}
	}
}