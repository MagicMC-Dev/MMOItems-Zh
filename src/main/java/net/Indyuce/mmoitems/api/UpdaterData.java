package net.Indyuce.mmoitems.api;

import net.Indyuce.mmoitems.api.item.template.MMOItemTemplate;
import net.Indyuce.mmoitems.manager.UpdaterManager.KeepOption;
import net.mmogroup.mmolib.api.item.NBTItem;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class UpdaterData {

	// TODO change this to MMOItemTemplate

	/*
	 * two UUIDs can be found : one on the itemStack in the nbttags, and one in
	 * the UpdaterData instance. if the two match, the item is up to date. if
	 * they don't match, the item needs to be updated. UUID not final because it
	 * must be changed
	 */
	private final MMOItemTemplate template;

	private UUID uuid;

	private final Set<KeepOption> options = new HashSet<>();

	public UpdaterData(MMOItemTemplate template, ConfigurationSection config) {
		this(template, UUID.fromString(config.getString("uuid")));

		for (KeepOption option : KeepOption.values())
			if (config.getBoolean(option.getPath()))
				options.add(option);
	}

	public UpdaterData(MMOItemTemplate template, UUID uuid, KeepOption... options) {
		this.template = template;
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
		return template.getType().getId() + "." + template.getId();
	}

	public Type getType() {
		return template.getType();
	}

	public String getId() {
		return template.getId();
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