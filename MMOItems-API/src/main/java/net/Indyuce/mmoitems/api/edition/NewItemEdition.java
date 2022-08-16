package net.Indyuce.mmoitems.api.edition;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.edition.input.AnvilGUI;
import net.Indyuce.mmoitems.api.edition.input.ChatEdition;
import net.Indyuce.mmoitems.gui.ItemBrowser;
import net.Indyuce.mmoitems.gui.PluginInventory;
import io.lumine.mythic.lib.MythicLib;

public class NewItemEdition implements Edition {
	private final ItemBrowser inv;

	public NewItemEdition(ItemBrowser inv) {
		this.inv = inv;
	}

	@Override
	public PluginInventory getInventory() {
		return inv;
	}

	@Override
	public void enable(String... message) {
		inv.getPlayer().closeInventory();

		inv.getPlayer().sendMessage(ChatColor.YELLOW + "" + ChatColor.STRIKETHROUGH + "-----------------------------------------------------");
		inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "Write in the chat, the id of the new item.");
		inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "Type 'cancel' to abort editing.");

		/*
		 * anvil text input feature. enables players to use an anvil to input
		 * text if they are having conflicts with their chat management plugins.
		 */
		if (MMOItems.plugin.getConfig().getBoolean("anvil-text-input") && MythicLib.plugin.getVersion().isBelowOrEqual(1, 13)) {
			new AnvilGUI(this);
			return;
		}

		/*
		 * default chat edition feature
		 */
		new ChatEdition(this);
		inv.getPlayer().sendTitle(ChatColor.GOLD + "" + ChatColor.BOLD + "Item Creation", "See chat.", 10, 40, 10);
	}

	@Override
	public boolean processInput(String input) {
		if (input.equals("cancel"))
			return true;

		Bukkit.getScheduler().runTask(MMOItems.plugin, () -> Bukkit.dispatchCommand(inv.getPlayer(),
				"mmoitems create " + inv.getType().getId() + " " + input.toUpperCase().replace(" ", "_").replace("-", "_")));
		return true;
	}

	@Override
	public boolean shouldGoBack() {
		return false;
	}
}
