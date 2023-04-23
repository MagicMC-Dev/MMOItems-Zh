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
		Inventory inv = Bukkit.createInventory(this, 54, "Arrow Particles: " + template.getId());
		// FileConfiguration config =
		// template.getType().getConfigFile().getConfig();

		Particle particle = null;
		try {
			particle = Particle.valueOf(getEditedSection().getString("arrow-particles.particle"));
		} catch (Exception ignored) {}

		ItemStack particleItem = new ItemStack(Material.BLAZE_POWDER);
		ItemMeta particleItemMeta = particleItem.getItemMeta();
		particleItemMeta.setDisplayName(ChatColor.GREEN + "Particle");
		List<String> particleItemLore = new ArrayList<>();
		particleItemLore.add(ChatColor.GRAY + "" + ChatColor.ITALIC + "The particle which is displayed around the");
		particleItemLore.add(ChatColor.GRAY + "" + ChatColor.ITALIC + "arrow. Fades away when the arrow lands.");
		particleItemLore.add("");
		particleItemLore.add(ChatColor.GRAY + "Current Value: " + (particle == null ? ChatColor.RED + "No particle selected."
				: ChatColor.GOLD + MMOUtils.caseOnWords(particle.name().toLowerCase().replace("_", " "))));
		particleItemLore.add("");
		particleItemLore.add(ChatColor.YELLOW + AltChar.listDash + " Click to change this value.");
		particleItemLore.add(ChatColor.YELLOW + AltChar.listDash + " Right click to reset.");
		particleItemMeta.setLore(particleItemLore);
		particleItem.setItemMeta(particleItemMeta);

		ItemStack amount = VersionMaterial.GRAY_DYE.toItem();
		ItemMeta amountMeta = amount.getItemMeta();
		amountMeta.setDisplayName(ChatColor.GREEN + "Amount");
		List<String> amountLore = new ArrayList<>();
		amountLore.add("");
		amountLore.add(ChatColor.GRAY + "Current Value: " + ChatColor.GOLD + getEditedSection().getInt("arrow-particles.amount"));
		amountLore.add("");
		amountLore.add(ChatColor.YELLOW + AltChar.listDash + " Click to change this value.");
		amountLore.add(ChatColor.YELLOW + AltChar.listDash + " Right click to reset.");
		amountMeta.setLore(amountLore);
		amount.setItemMeta(amountMeta);

		ItemStack offset = VersionMaterial.GRAY_DYE.toItem();
		ItemMeta offsetMeta = offset.getItemMeta();
		offsetMeta.setDisplayName(ChatColor.GREEN + "Offset");
		List<String> offsetLore = new ArrayList<>();
		offsetLore.add("");
		offsetLore.add(ChatColor.GRAY + "Current Value: " + ChatColor.GOLD + getEditedSection().getDouble("arrow-particles.offset"));
		offsetLore.add("");
		offsetLore.add(ChatColor.YELLOW + AltChar.listDash + " Click to change this value.");
		offsetLore.add(ChatColor.YELLOW + AltChar.listDash + " Right click to reset.");
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
				speedMeta.setDisplayName(ChatColor.GREEN + "Particle Color");
				List<String> speedLore = new ArrayList<>();
				speedLore.add("");
				speedLore.add(ChatColor.GRAY + "Current Value (R-G-B):");
				speedLore.add("" + ChatColor.RED + ChatColor.BOLD + red + ChatColor.GRAY + " - " + ChatColor.GREEN + ChatColor.BOLD + green
						+ ChatColor.GRAY + " - " + ChatColor.BLUE + ChatColor.BOLD + blue);
				speedLore.add("");
				speedLore.add(ChatColor.YELLOW + AltChar.listDash + " Click to change this value.");
				speedLore.add(ChatColor.YELLOW + AltChar.listDash + " Right click to reset.");
				speedMeta.setLore(speedLore);
				speed.setItemMeta(speedMeta);

				inv.setItem(41, speed);
			} else {
				ItemStack colorItem = VersionMaterial.GRAY_DYE.toItem();
				ItemMeta colorItemMeta = colorItem.getItemMeta();
				colorItemMeta.setDisplayName(ChatColor.GREEN + "Speed");
				List<String> colorItemLore = new ArrayList<>();
				colorItemLore.add(ChatColor.GRAY + "" + ChatColor.ITALIC + "The speed at which your particle");
				colorItemLore.add(ChatColor.GRAY + "" + ChatColor.ITALIC + "flies off in random directions.");
				colorItemLore.add("");
				colorItemLore.add(ChatColor.GRAY + "Current Value: " + ChatColor.GOLD + section.getDouble("speed"));
				colorItemLore.add("");
				colorItemLore.add(ChatColor.YELLOW + AltChar.listDash + " Click to change this value.");
				colorItemLore.add(ChatColor.YELLOW + AltChar.listDash + " Right click to reset.");
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

		if (item.getItemMeta().getDisplayName().equals(ChatColor.GREEN + "Particle")) {
			if (event.getAction() == InventoryAction.PICKUP_ALL)
				new StatEdition(this, ItemStats.ARROW_PARTICLES, "particle").enable("Write in the chat the particle you want.");

			if (event.getAction() == InventoryAction.PICKUP_HALF) {
				if (getEditedSection().contains("arrow-particles.particle")) {
					getEditedSection().set("arrow-particles", null);
					registerTemplateEdition();
					player.sendMessage(MMOItems.plugin.getPrefix() + "Successfully reset the particle.");
				}
			}
		}

		if (item.getItemMeta().getDisplayName().equals(ChatColor.GREEN + "Particle Color")) {
			if (event.getAction() == InventoryAction.PICKUP_ALL)
				new StatEdition(this, ItemStats.ARROW_PARTICLES, "color").enable("Write in the chat the RGB color you want.",
						ChatColor.AQUA + "Format: [RED] [GREEN] [BLUE]");

			if (event.getAction() == InventoryAction.PICKUP_HALF) {
				if (getEditedSection().contains("arrow-particles.color")) {
					getEditedSection().set("arrow-particles.color", null);
					registerTemplateEdition();
					player.sendMessage(MMOItems.plugin.getPrefix() + "Successfully reset the particle color.");
				}
			}
		}

		for (String string : new String[] { "amount", "offset", "speed" })
			if (item.getItemMeta().getDisplayName().equals(ChatColor.GREEN + MMOUtils.caseOnWords(string))) {
				if (event.getAction() == InventoryAction.PICKUP_ALL)
					new StatEdition(this, ItemStats.ARROW_PARTICLES, string).enable("Write in the chat the " + string + " you want.");

				if (event.getAction() == InventoryAction.PICKUP_HALF) {
					if (getEditedSection().contains("arrow-particles." + string)) {
						getEditedSection().set("arrow-particles." + string, null);
						registerTemplateEdition();
						player.sendMessage(MMOItems.plugin.getPrefix() + "Successfully reset the " + string + ".");
					}
				}
			}
	}
}