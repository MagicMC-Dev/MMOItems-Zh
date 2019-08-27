package net.Indyuce.mmoitems.api.crafting.condition;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import net.Indyuce.mmoitems.api.player.PlayerData;

public class PermissionCondition extends Condition {
	private List<String> permissions = new ArrayList<>();

	public PermissionCondition() {
		super("perms");
	}

	@Override
	public Condition load(String[] args) {
		PermissionCondition condition = new PermissionCondition();
		for (String permission : args)
			condition.permissions.add(permission);
		condition.setDisplay(getDisplay());

		return condition;
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
		return string.replace("#perms#", StringUtils.join(permissions, ", "));
	}

	@Override
	public void whenCrafting(PlayerData data) {
	}
}
