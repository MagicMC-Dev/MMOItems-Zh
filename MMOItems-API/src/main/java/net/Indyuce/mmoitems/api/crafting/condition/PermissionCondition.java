package net.Indyuce.mmoitems.api.crafting.condition;

import java.util.Arrays;
import java.util.List;

import net.Indyuce.mmoitems.api.player.PlayerData;
import io.lumine.mythic.lib.api.MMOLineConfig;

public class PermissionCondition extends GenericCondition {
    private final List<String> permissions;

    public PermissionCondition(MMOLineConfig config) {
        super("permission", config);

        config.validate("list");
        permissions = Arrays.asList(config.getString("list").split(","));
    }

    @Override
    public boolean isMet(PlayerData data) {
        for (String permission : permissions)
            if (!data.getPlayer().hasPermission(permission))
                return false;
        return true;
    }

    @Override
    public void whenCrafting(PlayerData data) {
    }
}
