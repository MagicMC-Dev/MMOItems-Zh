package net.Indyuce.mmoitems.api.edition;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.edition.ChatEditionBase.ChatEditionProcess;

public class ChatEdition implements ChatEditionProcess, Listener {
	private ChatEditionBase edition;

	@Override
	public void open(ChatEditionBase edition) {
		this.edition = edition;

		Bukkit.getPluginManager().registerEvents(this, MMOItems.plugin);
	}

	@Override
	public void close() {
		HandlerList.unregisterAll(this);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void a(AsyncPlayerChatEvent event) {
		if (event.getPlayer().equals(edition.getPlayer())) {
			event.setCancelled(true);
			edition.output(event.getMessage());
		}
	}

	// cancel stat edition when opening any gui
	@EventHandler
	public void b(InventoryOpenEvent event) {
		if (event.getPlayer().equals(edition.getPlayer()))
			close();
	}
}
