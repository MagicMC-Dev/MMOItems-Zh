package net.Indyuce.mmoitems.api.util;

import org.bukkit.configuration.ConfigurationSection;

public abstract class PostLoadObject {
	private ConfigurationSection config;

	/**
	 * Objects which must load some data afterwards, like quests which must load
	 * their parent quests after all quests were initialized or classes which
	 * must load their subclasses
	 * 
	 * @param config
	 *            Configuration section which must be cached during a small
	 *            period of time till the rest of the data is loaded
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