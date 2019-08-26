package net.Indyuce.mmoitems.api;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.bukkit.command.CommandSender;
import org.bukkit.util.Consumer;

public class PluginUpdate {
	private final int id;
	private final Consumer<CommandSender> handler;
	private final List<String> description;

	public PluginUpdate(int id, String[] description, Consumer<CommandSender> handler) {
		Validate.notNull(handler, "Update handler must not be null");
		Validate.notNull(description, "Update description must not be null");

		this.id = id;
		this.handler = handler;
		this.description = Arrays.asList(description);
	}

	public int getId() {
		return id;
	}

	public void apply(CommandSender sender) {
		handler.accept(sender);
	}

	public List<String> getDescription() {
		return description;
	}

	public boolean hasDescription() {
		return !description.isEmpty();
	}
}
