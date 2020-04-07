package net.Indyuce.mmoitems.stat.data;

import java.util.UUID;

import com.google.gson.JsonObject;
import com.sk89q.worldedit.entity.Player;

import net.Indyuce.mmoitems.stat.data.type.StatData;

public class SoulboundData implements StatData {
	private final UUID uuid;
	private final String name;
	private final int level;

	public SoulboundData(Player player, int level) {
		this(player.getUniqueId(), player.getName(), level);
	}

	public SoulboundData(UUID uuid, String name, int level) {
		this.uuid = uuid;
		this.name = name;
		this.level = level;
	}

	public SoulboundData(JsonObject object) {
		uuid = UUID.fromString(object.get("UUID").getAsString());
		name = object.get("Name").getAsString();
		level = object.get("Level").getAsInt();
	}

	public UUID getUniqueId() {
		return uuid;
	}

	public String getName() {
		return name;
	}

	public int getLevel() {
		return level;
	}

	public JsonObject toJson() {
		JsonObject object = new JsonObject();
		object.addProperty("Level", level);
		object.addProperty("Name", name);
		object.addProperty("UUID", uuid.toString());
		return object;
	}
}
