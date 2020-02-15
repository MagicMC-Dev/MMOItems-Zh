package net.Indyuce.mmoitems.api.crafting.condition;

import java.util.Arrays;
import java.util.List;

import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.api.util.MMOLineConfig;

public class PermissionCondition extends Condition {
	private final List<String> permissions;

	public PermissionCondition(MMOLineConfig config) {
		super("permission");

		config.validate("list");
		permissions = Arrays.asList(config.getString("list").split("\\,"));
	}

	@Override
	public boolean isMet(PlayerData data) {
		for (String permission : permissions)
			if (!data.getPlayer().hasPermission(permission))
				return false;
		return true;
	}

	@Override
	public String formatDisplay(String string) {
		return string.replace("#perms#", String.join(", ", permissions));
	}

	@Override
	public void whenCrafting(PlayerData data) {
	}
}
