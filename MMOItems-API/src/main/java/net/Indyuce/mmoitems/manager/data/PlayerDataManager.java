package net.Indyuce.mmoitems.manager.data;

import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.comp.profile.DefaultProfileDataModule;
import io.lumine.mythic.lib.data.DefaultOfflineDataHolder;
import io.lumine.mythic.lib.data.SynchronizedDataManager;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.player.PlayerData;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class PlayerDataManager extends SynchronizedDataManager<PlayerData, DefaultOfflineDataHolder> {
    public PlayerDataManager(JavaPlugin plugin) {
        super(plugin, new YAMLDataHandler(plugin));
    }

    @Override
    public PlayerData newPlayerData(@NotNull MMOPlayerData mmoPlayerData) {
        return new PlayerData(mmoPlayerData);
    }

    @Override
    public Object newProfileDataModule() {
        return new DefaultProfileDataModule(MMOItems.plugin);
    }
}
