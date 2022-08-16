package net.Indyuce.mmoitems.api.crafting.condition;

import io.lumine.mythic.lib.api.MMOLineConfig;
import net.Indyuce.mmoitems.api.player.PlayerData;

public class LevelCondition extends Condition {
    private final int level;

    /**
     * Should this amount of vanilla levels be
     * consumed when the crafting recipe is used
     */
    private final boolean consume;

    public LevelCondition(MMOLineConfig config) {
        super("level");

        config.validate("level");
        level = config.getInt("level");
        consume = config.getBoolean("consume", false);
    }

    @Override
    public boolean isMet(PlayerData data) {
        return data.getRPG().getLevel() >= level;
    }

    @Override
    public String formatDisplay(String string) {
        return string.replace("#level#", "" + level);
    }

    @Override
    public void whenCrafting(PlayerData data) {
        if (consume)
            data.getPlayer().setLevel(Math.max(0, data.getPlayer().getLevel() - level));
    }
}
