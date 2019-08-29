package net.Indyuce.mmoitems.api.edition;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitRunnable;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.gui.PluginInventory;

public class NewItemEdition extends ChatEditionBase {

	/*
	 * saves the data about the edited data so the plugin can edit the
	 * corresponding stat. some stats have complex chat formats, so the object
	 * array allow to save more complex edition info
	 */
	private Type type;

	public NewItemEdition(PluginInventory inv, Type type) {
		super(inv);
		this.type = type;
	}

	public Type getType() {
		return type;
	}

	@Override
	public void enable(String... messages) {
		getPlayer().closeInventory();

		getPlayer().sendMessage(ChatColor.YELLOW + "" + ChatColor.STRIKETHROUGH + "-----------------------------------------------------");
		getPlayer().sendMessage(MMOItems.plugin.getPrefix() + ChatColor.translateAlternateColorCodes('&', "Write in the chat, the id of the new item."));
		getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "Type 'cancel' to abort editing.");

		/*
		 * anvil text input feature. enables players to use an anvil to input
		 * text if they are having conflicts with their chat management plugins.
		 */
		if (MMOItems.plugin.getConfig().getBoolean("anvil-text-input") && MMOItems.plugin.getVersion().isBelowOrEqual(1, 13)) {
			new AnvilGUI().open(this);
			return;
		}

		/*
		 * default chat edition feature
		 */
		new ChatEdition().open(this);
		MMOItems.plugin.getNMS().sendTitle(getPlayer(), ChatColor.GOLD + "" + ChatColor.BOLD + "Item Creation", "See chat.", 10, 40, 10);
	}

	@Override
	public void output(String output) {
		if (output.equals("cancel"))
			inv.open();
		else new BukkitRunnable() {
			@Override
			public void run() {
				Bukkit.dispatchCommand(getPlayer(), "mi create " + type.getId() + " " + output.toUpperCase().replace(" ", "_").replace("-", "_"));
			}
		}.runTask(MMOItems.plugin);
	}
}
