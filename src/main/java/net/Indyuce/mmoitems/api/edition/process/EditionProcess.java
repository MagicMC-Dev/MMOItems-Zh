package net.Indyuce.mmoitems.api.edition.process;

import org.bukkit.entity.Player;

import net.Indyuce.mmoitems.api.edition.Edition;
import net.Indyuce.mmoitems.gui.PluginInventory;

public abstract class EditionProcess {

	/*
	 * saves the last inventory opened. it saves the item data, and the last
	 * opened page. allows for a much easier access to this data
	 */
	private final PluginInventory inv;
	private final Edition edition;

	public EditionProcess(PluginInventory inv, Edition edition) {
		this.inv = inv;
		this.edition = edition;
	}

	public void input(String input) {
		if (edition.output(input)) {
			if (edition.shouldGoBack())
				inv.open();
			close();
		}
	}

	public PluginInventory getLastOpened() {
		return inv;
	}

	public Player getPlayer() {
		return inv.getPlayer();
	}

	public abstract void close();
}
