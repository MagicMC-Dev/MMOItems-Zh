package net.Indyuce.mmoitems.api.edition;

import org.bukkit.entity.Player;
import net.Indyuce.mmoitems.gui.PluginInventory;

public abstract class ChatEditionBase {

	/*
	 * saves the last inventory opened. it saves the item data, and the last
	 * opened page. allows for a much easier access to this data
	 */
	protected PluginInventory inv;

	public ChatEditionBase(PluginInventory inv) {
		this.inv = inv;
	}

	public PluginInventory getInventory() {
		return inv;
	}

	public Player getPlayer() {
		return inv.getPlayer();
	}

	public abstract void enable(String... messages);

	public abstract void output(String output);

	public interface ChatEditionProcess {
		void open(ChatEditionBase edition);

		void close();
	}
}
