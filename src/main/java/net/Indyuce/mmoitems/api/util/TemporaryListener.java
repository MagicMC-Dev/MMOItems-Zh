package net.Indyuce.mmoitems.api.util;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import net.Indyuce.mmoitems.MMOItems;
import org.jetbrains.annotations.NotNull;

public abstract class TemporaryListener implements Listener {

	/*
	 * handler lists which must be called when the temporary listener is closed
	 * so that the listener is entirely unregistered.
	 */
	private final HandlerList[] lists;

	/*
	 * sometimes the close method is called twice because of a safe delayed task
	 * not being cancelled when the listener is closed.
	 */
	private boolean closed;

	public TemporaryListener(HandlerList... events) {
		lists = events;
		Bukkit.getPluginManager().registerEvents(this, MMOItems.plugin);
	}

	/*
	 * used to close the temporary listener after some delay
	 */
	public void close(long duration) {
		Bukkit.getScheduler().runTaskLater(MMOItems.plugin, (Runnable) this::close, duration);
	}

	public void close() {
		if (closed)
			return;

		closed = true;
		for (HandlerList list : lists)
			list.unregister(this);
	}
}
