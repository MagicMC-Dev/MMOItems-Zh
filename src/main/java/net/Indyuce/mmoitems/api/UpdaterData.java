package net.Indyuce.mmoitems.api;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;

import net.Indyuce.mmoitems.api.item.template.MMOItemTemplate;
import net.Indyuce.mmoitems.manager.UpdaterManager.KeepOption;
import io.lumine.mythic.lib.api.item.NBTItem;

public class UpdaterData {

	/**
	 * The item reference
	 */
	private final Type type;
	private final String id;

	/**
	 * Two UUIDs can be found: one on the itemStack in the NBTTags, and one in
	 * the UpdaterData instance. If the UUIDs match, the item is up to date. If
	 * they don't match, the item needs to be updated. UUID not final because it
	 * is updated everytime the item is edited using the editor GUI
	 */
	private UUID uuid;

	private final Set<KeepOption> options = new HashSet<>();

	public UpdaterData(MMOItemTemplate template, ConfigurationSection config) {
		this(template, UUID.fromString(config.getString("uuid")));

		for (KeepOption option : KeepOption.values())
			if (config.getBoolean(option.getPath()))
				options.add(option);
	}

	public UpdaterData(MMOItemTemplate template, UUID uuid, KeepOption... options) {
		this.id = template.getId();
		this.type = template.getType();
		this.uuid = uuid;
		this.options.addAll(Arrays.asList(options));
	}

	public UpdaterData(MMOItemTemplate template, UUID uuid, boolean enableAllOptions) {
		this(template, uuid);

		if (enableAllOptions)
			options.addAll(Arrays.asList(KeepOption.values()));
	}

	public void save(ConfigurationSection config) {
		for (KeepOption option : KeepOption.values())
			if (options.contains(option))
				config.set(option.getPath(), true);
		config.set("uuid", uuid.toString());
	}

	public String getPath() {
		return type.getId() + "." + id;
	}

	public Type getType() {
		return type;
	}

	public String getId() {
		return id;
	}

	public UUID getUniqueId() {
		return uuid;
	}

	/*
	 * used everytime a change is applied to one item. the database uuid is
	 * randomized so that any item with different UUIDs have to be dynamically
	 * updated
	 */
	public void setUniqueId(UUID uuid) {
		Validate.notNull(uuid, "UUID cannot be null");

		this.uuid = uuid;
	}

	public boolean matches(NBTItem item) {
		return uuid.toString().equals(item.getString("MMOITEMS_ITEM_UUID"));
	}

	public boolean hasOption(KeepOption option) {
		return options.contains(option);
	}

	public void addOption(KeepOption option) {
		options.add(option);
	}

	public void removeOption(KeepOption option) {
		options.remove(option);
	}
}