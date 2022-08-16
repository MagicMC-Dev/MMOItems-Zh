package net.Indyuce.mmoitems.api.edition.input;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.edition.Edition;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatEdition extends PlayerInputHandler implements Listener {

	/**
	 * Allows to retrieve player input using chat messages
	 * 
	 * @param edition The type of data being edited
	 */
	public ChatEdition(Edition edition) {
		super(edition);

		Bukkit.getPluginManager().registerEvents(this, MMOItems.plugin);
	}

	@Override
	public void close() {
		HandlerList.unregisterAll(this);
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void a(AsyncPlayerChatEvent event) {
		if (getPlayer() != null && event.getPlayer().equals(getPlayer())) {
			event.setCancelled(true);
			registerInput(event.getMessage());
		}
	}

	// cancel stat edition when opening any gui
	@EventHandler
	public void b(InventoryOpenEvent event) {
		if (event.getPlayer().equals(getPlayer()))
			close();
	}
}
