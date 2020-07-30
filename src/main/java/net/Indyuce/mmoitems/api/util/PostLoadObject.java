package net.Indyuce.mmoitems.api.util;

import org.bukkit.configuration.ConfigurationSection;

public abstract class PostLoadObject {
	private ConfigurationSection config;

	/*
	 * objects which must load some data afterwards, like quests which must load
	 * their parent quests after all quests were initialized or classes which
	 * must load their subclasses
	 */
	public PostLoadObject(ConfigurationSection config) {
		this.config = config;
	}

	public void postLoad() {
		whenPostLoaded(config);

		/*
		 * clean config object for garbage collection
		 */
		config = null;
	}

	protected abstract void whenPostLoaded(ConfigurationSection config);
}