package net.Indyuce.mmoitems.api.edition.process;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.edition.Edition;

public class ChatEdition extends EditionProcess implements Listener {
	public ChatEdition(Edition edition) {
		super(edition);

		Bukkit.getPluginManager().registerEvents(this, MMOItems.plugin);
	}

	@Override
	public void close() {
		HandlerList.unregisterAll(this);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void a(AsyncPlayerChatEvent event) {
		if (event.getPlayer().equals(getPlayer())) {
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
