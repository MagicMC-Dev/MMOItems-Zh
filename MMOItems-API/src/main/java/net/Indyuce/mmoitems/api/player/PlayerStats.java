package net.Indyuce.mmoitems.api.player;

import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.api.stat.StatInstance;
import io.lumine.mythic.lib.api.stat.StatMap;
import io.lumine.mythic.lib.player.PlayerMetadata;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import org.jetbrains.annotations.NotNull;

@Deprecated
public class PlayerStats {
    private final PlayerData playerData;
    private final StatMap map;

    @Deprecated
    public PlayerStats(PlayerData playerData) {
        this.playerData = playerData;
        this.map = playerData.getMMOPlayerData().getStatMap();
    }

    @Deprecated
    public PlayerData getData() {
        return playerData;
    }

    @Deprecated
    public StatMap getMap() {
        return map;
    }

    /**
     * @deprecated
     * @see PlayerData#getStat(ItemStat)
     */
    @Deprecated
    public double getStat(@NotNull ItemStat<?, ?> stat) {
        return map.getStat(stat.getId());
    }

    @Deprecated
    public StatInstance getInstance(@NotNull ItemStat<?, ?> stat) {
        return map.getInstance(stat.getId());
    }

    /**
     * @see StatMap#cache(EquipmentSlot)
     * @deprecated
     */
    @Deprecated
    public PlayerMetadata newTemporary(@NotNull EquipmentSlot castSlot) {
        return playerData.getMMOPlayerData().getStatMap().cache(castSlot);
    }

    @Deprecated
    public void updateStats() {
        playerData.resolveInventory();
    }
}
