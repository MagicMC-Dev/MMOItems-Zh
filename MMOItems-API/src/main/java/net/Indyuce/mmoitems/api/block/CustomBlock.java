package net.Indyuce.mmoitems.api.block;

import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.Indyuce.mmoitems.api.util.MushroomState;
import net.Indyuce.mmoitems.stat.data.BooleanData;
import net.Indyuce.mmoitems.stat.data.DoubleData;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public class CustomBlock {
    private final int id;
    private final MushroomState state;

    private final MMOItem mmoitem;

    @Nullable
    private final WorldGenTemplate template;
    private final int minExp, maxExp, requiredPower;
    private final boolean requirePowerToBreak;

    private static final Random RANDOM = new Random();

    public CustomBlock(MushroomState state, MMOItem mmoitem) {
        this.mmoitem = mmoitem;

        this.id = mmoitem.hasData(ItemStats.BLOCK_ID) ? (int) ((DoubleData) mmoitem.getData(ItemStats.BLOCK_ID)).getValue() : 0;
        this.state = state;

        this.minExp = mmoitem.hasData(ItemStats.MIN_XP) ? (int) ((DoubleData) mmoitem.getData(ItemStats.MIN_XP)).getValue() : 0;
        this.maxExp = mmoitem.hasData(ItemStats.MAX_XP) ? (int) ((DoubleData) mmoitem.getData(ItemStats.MAX_XP)).getValue() : 0;
        this.requiredPower = mmoitem.hasData(ItemStats.REQUIRED_POWER) ? (int) ((DoubleData) mmoitem.getData(ItemStats.REQUIRED_POWER)).getValue() : 0;
        this.requirePowerToBreak = mmoitem.hasData(ItemStats.REQUIRE_POWER_TO_BREAK) && ((BooleanData) mmoitem.getData(ItemStats.REQUIRE_POWER_TO_BREAK)).isEnabled();
        this.template = mmoitem.hasData(ItemStats.GEN_TEMPLATE) ? MMOItems.plugin.getWorldGen().getOrThrow((mmoitem.getData(ItemStats.GEN_TEMPLATE)).toString()) : null;
    }

    public int getId() {
        return id;
    }

    public MushroomState getState() {
        return state;
    }

    public boolean hasGenTemplate() {
        return template != null;
    }

    public WorldGenTemplate getGenTemplate() {
        return template;
    }

    public int getMinExpDrop() {
        return minExp;
    }

    public int getMaxExpDrop() {
        return maxExp;
    }

    public int rollExperience() {
        return Math.max(0, maxExp < minExp ? minExp : RANDOM.nextInt(maxExp - minExp + 1) + minExp);
    }

    public int getRequiredPower() {
        return requiredPower;
    }

    public ItemStack getItem() {
        return mmoitem.newBuilder().build();
    }

    public boolean requirePowerToBreak() {
        return requirePowerToBreak;
    }
}
