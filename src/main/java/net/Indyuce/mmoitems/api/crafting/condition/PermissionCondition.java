package net.Indyuce.mmoitems.api.crafting.condition;

import java.util.Arrays;
import java.util.List;

import net.Indyuce.mmoitems.api.player.PlayerData;
import io.lumine.mythic.lib.api.MMOLineConfig;

public class PermissionCondition extends Condition {
	private final List<String> permissions;

	/**
	 * Permissions are super ugly to display so MI uses a string instead.
	 * This way 'Only for Mages' is used instead of 'class.mage'
	 *
	 * One string can also replace multiple permissions.
	 * 'Magic Classes Only' instead of 'class.mage' and 'class.apprentice'
	 */
	private final String display;

	public PermissionCondition(MMOLineConfig config) {
		super("permission");

		config.validate("list");
		permissions = Arrays.asList(config.getString("list").split(","));
		display = config.contains("display") ? config.getString("display") : "?";
	}

	@Override
	public boolean isMet(PlayerData data) {
		if(!data.isOnline()) return false;
		for (String permission : permissions)
			if (!data.getPlayer().hasPermission(permission))
				return false;
		return true;
	}

	@Override
	public String formatDisplay(String string) {
		return string.replace("#perms#", String.join(", ", permissions)).replace("#display#", display);
	}

	@Override
	public void whenCrafting(PlayerData data) {
	}
}
