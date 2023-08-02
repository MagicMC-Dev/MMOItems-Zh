package net.Indyuce.mmoitems.gui.edition;

import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.util.MMOUtils;
import net.Indyuce.mmoitems.api.edition.StatEdition;
import net.Indyuce.mmoitems.api.item.template.MMOItemTemplate;
import net.Indyuce.mmoitems.stat.data.ParticleData;
import io.lumine.mythic.lib.api.util.AltChar;
import io.lumine.mythic.lib.version.VersionMaterial;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ArrowParticlesEdition extends EditionInventory {
	public ArrowParticlesEdition(Player player, MMOItemTemplate template) {
		super(player, template);
	}

	@Override
	public Inventory getInventory() {
		Inventory inv = Bukkit.createInventory(this, 54, "箭头粒子: " + template.getId());
		// FileConfiguration config =
		// template.getType().getConfigFile().getConfig();

		Particle particle = null;
		try {
			particle = Particle.valueOf(getEditedSection().getString("arrow-particles.particle"));
		} catch (Exception ignored) {}

		ItemStack particleItem = new ItemStack(Material.BLAZE_POWDER);
		ItemMeta particleItemMeta = particleItem.getItemMeta();
		particleItemMeta.setDisplayName(ChatColor.GREEN + "粒子");
		List<String> particleItemLore = new ArrayList<>();
		particleItemLore.add(ChatColor.GRAY + "" + ChatColor.ITALIC + "显示在周围的箭头粒子");
		particleItemLore.add(ChatColor.GRAY + "" + ChatColor.ITALIC + "当箭落地时消失");
		particleItemLore.add("");
		particleItemLore.add(ChatColor.GRAY + "当前值: " + (particle == null ? ChatColor.RED + "未选择任何粒子"
				: ChatColor.GOLD + MMOUtils.caseOnWords(particle.name().toLowerCase().replace("_", " "))));
		particleItemLore.add("");
		particleItemLore.add(ChatColor.YELLOW + AltChar.listDash + "左键单击进行选择");
		particleItemLore.add(ChatColor.YELLOW + AltChar.listDash + "右键单击重置");
		particleItemMeta.setLore(particleItemLore);
		particleItem.setItemMeta(particleItemMeta);

		ItemStack amount = VersionMaterial.GRAY_DYE.toItem();
		ItemMeta amountMeta = amount.getItemMeta();
		amountMeta.setDisplayName(ChatColor.GREEN + "数量");
		List<String> amountLore = new ArrayList<>();
		amountLore.add("");
		amountLore.add(ChatColor.GRAY + "当前值: " + ChatColor.GOLD + getEditedSection().getInt("arrow-particles.amount"));
		amountLore.add("");
		amountLore.add(ChatColor.YELLOW + AltChar.listDash + "左键单击进行选择");
		amountLore.add(ChatColor.YELLOW + AltChar.listDash + "右键单击重置");
		amountMeta.setLore(amountLore);
		amount.setItemMeta(amountMeta);

		ItemStack offset = VersionMaterial.GRAY_DYE.toItem();
		ItemMeta offsetMeta = offset.getItemMeta();
		offsetMeta.setDisplayName(ChatColor.GREEN + "抵消");
		List<String> offsetLore = new ArrayList<>();
		offsetLore.add("");
		offsetLore.add(ChatColor.GRAY + "当前值: " + ChatColor.GOLD + getEditedSection().getDouble("arrow-particles.offset"));
		offsetLore.add("");
		offsetLore.add(ChatColor.YELLOW + AltChar.listDash + "左键单击进行选择");
		offsetLore.add(ChatColor.YELLOW + AltChar.listDash + "右键单击重置");
		offsetMeta.setLore(offsetLore);
		offset.setItemMeta(offsetMeta);

		if (particle != null) {
			ConfigurationSection section = getEditedSection().getConfigurationSection("arrow-particles");
			if (MMOUtils.isColorable(particle)) {
				int red = section.getInt("color.red");
				int green = section.getInt("color.green");
				int blue = section.getInt("color.blue");

				ItemStack speed = VersionMaterial.GRAY_DYE.toItem();
				ItemMeta speedMeta = speed.getItemMeta();
				speedMeta.setDisplayName(ChatColor.GREEN + "粒子颜色");
				List<String> speedLore = new ArrayList<>();
				speedLore.add("");
				speedLore.add(ChatColor.GRAY + "当前值 (R-G-B ) :");
				speedLore.add("" + ChatColor.RED + ChatColor.BOLD + red + ChatColor.GRAY + " - " + ChatColor.GREEN + ChatColor.BOLD + green
						+ ChatColor.GRAY + " - " + ChatColor.BLUE + ChatColor.BOLD + blue);
				speedLore.add("");
				speedLore.add(ChatColor.YELLOW + AltChar.listDash + "左键单击进行选择");
				speedLore.add(ChatColor.YELLOW + AltChar.listDash + "右键单击重置");
				speedMeta.setLore(speedLore);
				speed.setItemMeta(speedMeta);

				inv.setItem(41, speed);
			} else {
				ItemStack colorItem = VersionMaterial.GRAY_DYE.toItem();
				ItemMeta colorItemMeta = colorItem.getItemMeta();
				colorItemMeta.setDisplayName(ChatColor.GREEN + "速度");
				List<String> colorItemLore = new ArrayList<>();
				colorItemLore.add(ChatColor.GRAY + "" + ChatColor.ITALIC + "粒子沿随机");
				colorItemLore.add(ChatColor.GRAY + "" + ChatColor.ITALIC + "方向飞行的速度");
				colorItemLore.add("");
				colorItemLore.add(ChatColor.GRAY + "当前值: " + ChatColor.GOLD + section.getDouble("speed"));
				colorItemLore.add("");
				colorItemLore.add(ChatColor.YELLOW + AltChar.listDash + "左键单击进行选择");
				colorItemLore.add(ChatColor.YELLOW + AltChar.listDash + "右键单击重置");
				colorItemMeta.setLore(colorItemLore);
				colorItem.setItemMeta(colorItemMeta);

				inv.setItem(41, colorItem);
			}
		}

		addEditionInventoryItems(inv, true);
		inv.setItem(30, particleItem);
		inv.setItem(23, amount);
		inv.setItem(32, offset);

		return inv;
	}

	@Override
	public void whenClicked(InventoryClickEvent event) {
		ItemStack item = event.getCurrentItem();

		event.setCancelled(true);
		if (event.getInventory() != event.getClickedInventory() || !MMOUtils.isMetaItem(item, false))
			return;

		if (item.getItemMeta().getDisplayName().equals(ChatColor.GREEN + "粒子")) {
			if (event.getAction() == InventoryAction.PICKUP_ALL)
				new StatEdition(this, ItemStats.ARROW_PARTICLES, "粒子").enable("在聊天中写下您想要的粒子");

			if (event.getAction() == InventoryAction.PICKUP_HALF) {
				if (getEditedSection().contains("arrow-particles.particle")) {
					getEditedSection().set("arrow-particles", null);
					registerTemplateEdition();
					player.sendMessage(MMOItems.plugin.getPrefix() + "成功重置粒子");
				}
			}
		}

		if (item.getItemMeta().getDisplayName().equals(ChatColor.GREEN + "粒子颜色")) {
			if (event.getAction() == InventoryAction.PICKUP_ALL)
				new StatEdition(this, ItemStats.ARROW_PARTICLES, "颜色").enable("在聊天中写下您想要的 RGB 颜色",
						ChatColor.AQUA + "格式: [RED] [GREEN] [BLUE]");

			if (event.getAction() == InventoryAction.PICKUP_HALF) {
				if (getEditedSection().contains("arrow-particles.color")) {
					getEditedSection().set("arrow-particles.color", null);
					registerTemplateEdition();
					player.sendMessage(MMOItems.plugin.getPrefix() + "成功重置粒子颜色");
				}
			}
		}

		for (String string : new String[] { "amount", "offset", "speed" })
			if (item.getItemMeta().getDisplayName().equals(ChatColor.GREEN + MMOUtils.caseOnWords(string))) {
				if (event.getAction() == InventoryAction.PICKUP_ALL)
					new StatEdition(this, ItemStats.ARROW_PARTICLES, string).enable("在聊天中写下你想要的" + string + " .");

				if (event.getAction() == InventoryAction.PICKUP_HALF) {
					if (getEditedSection().contains("arrow-particles." + string)) {
						getEditedSection().set("arrow-particles." + string, null);
						registerTemplateEdition();
						player.sendMessage(MMOItems.plugin.getPrefix() + "重置成功" + string + ".");
					}
				}
			}
	}
}