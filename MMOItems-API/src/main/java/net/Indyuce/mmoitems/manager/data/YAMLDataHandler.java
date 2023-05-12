package net.Indyuce.mmoitems.manager.data;

import io.lumine.mythic.lib.data.DefaultOfflineDataHolder;
import io.lumine.mythic.lib.data.yaml.YAMLSynchronizedDataHandler;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.UUID;

public class YAMLDataHandler extends YAMLSynchronizedDataHandler<PlayerData, DefaultOfflineDataHolder> {
    public YAMLDataHandler() {
        super(MMOItems.plugin);
    }

    @Override
    public void saveInSection(PlayerData playerData, ConfigurationSection config) {
        config.createSection("crafting-queue");
        config.set("permissions-from-items", new ArrayList<>(playerData.getPermissions()));
        playerData.getCrafting().save(config.getConfigurationSection("crafting-queue"));
    }

    @Override
    public void loadFromSection(PlayerData playerData, ConfigurationSection config) {

        if (config.contains("crafting-queue"))
            playerData.getCrafting().load(playerData, config.getConfigurationSection("crafting-queue"));

        if (MMOItems.plugin.hasPermissions() && config.contains("permissions-from-items")) {
            final Permission perms = MMOItems.plugin.getVault().getPermissions();
            config.getStringList("permissions-from-items").forEach(perm -> {
                if (perms.has(playerData.getPlayer(), perm)) perms.playerRemove(playerData.getPlayer(), perm);
            });
        }
    }

    @Override
    public void setup() {
        // Nothing
    }

    @Override
    public DefaultOfflineDataHolder getOffline(@NotNull UUID uuid) {
        return new DefaultOfflineDataHolder(uuid);
    }

    @Override
    public void close() {
        // Nothing
    }
}
