package net.Indyuce.mmoitems.api.edition;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.util.ui.FriendlyFeedbackCategory;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.edition.input.AnvilGUI;
import net.Indyuce.mmoitems.api.edition.input.ChatEdition;
import net.Indyuce.mmoitems.gui.PluginInventory;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import org.bukkit.ChatColor;

public class StatEdition implements Edition {

	/*
	 * saves the data about the edited data so the plugin can edit the
	 * corresponding stat. some stats have complex chat formats, so the object
	 * array allow to save more complex edition info
	 */
	private final EditionInventory inv;
	private final ItemStat stat;
	private final Object[] info;

	public StatEdition(EditionInventory inv, ItemStat stat, Object... info) {
		this.inv = inv;
		this.stat = stat;
		this.info = info;
	}

	@Override
	public PluginInventory getInventory() {
		return inv;
	}

	public ItemStat getStat() {
		return stat;
	}

	public Object[] getData() {
		return info;
	}

	@Override
	public void enable(String... message) {
		inv.getPlayer().closeInventory();

		inv.getPlayer().sendMessage(ChatColor.YELLOW + "" + ChatColor.STRIKETHROUGH + "-----------------------------------------------------");
		for (String line : message)
			inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + ChatColor.translateAlternateColorCodes('&', line));
		inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "Type 'cancel' to abort editing.");

		/*
		 * Anvil text input feature. enables players to use an anvil to input
		 * text if they are having conflicts with their chat management plugins.
		 */
		if (MMOItems.plugin.getConfig().getBoolean("anvil-text-input") && MythicLib.plugin.getVersion().isBelowOrEqual(1, 13)) {
			new AnvilGUI(this);
			return;
		}

		// Default chat edition feature
		new ChatEdition(this);
		inv.getPlayer().sendTitle(ChatColor.GOLD + "" + ChatColor.BOLD + "Item Edition", "See chat.", 10, 40, 10);
	}

	@Override
	public boolean processInput(String input) {

		// If cancel, open back inventory
		if (input.equals("cancel")) {
			inv.open();
			return true;
		}

		try {

			// Perform WhenInput Operation
			stat.whenInput(inv, input, info);

			// Success
			return true;
		} catch (RuntimeException exception) {
			// Add message to the FFP
			if (!exception.getMessage().isEmpty()) { inv.getFFP().log(FriendlyFeedbackCategory.ERROR, exception.getMessage()); }

			// Log all
			inv.getFFP().sendTo(FriendlyFeedbackCategory.ERROR, inv.getPlayer());
			inv.getFFP().sendTo(FriendlyFeedbackCategory.FAILURE, inv.getPlayer());
			inv.getFFP().clearFeedback();

			// No success
			return false;
		}
	}

	@Override
	public boolean shouldGoBack() {
		return true;
	}
}
