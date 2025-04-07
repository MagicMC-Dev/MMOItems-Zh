package net.Indyuce.mmoitems.item.build;

import io.lumine.mythic.lib.util.annotation.NotUsed;
import io.lumine.mythic.lib.util.lang3.Validate;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ItemTier;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.tooltip.TooltipTexture;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Class that gives context for when generating an item. It is being passed down the
 * "item generation chain", from the MMOItemBuilder to the ItemStackBuilder.
 *
 * @author jules
 * @see net.Indyuce.mmoitems.api.item.build.ItemStackBuilder
 * @see net.Indyuce.mmoitems.api.item.build.MMOItemBuilder
 */
@ApiStatus.Experimental
public class BuildMetadata {

    public boolean forDisplay;

    /**
     * Player onto which item is being generated
     */
    private Player onto;

    /**
     * Forced item level if not null.
     * <p>
     * TODO define priorities for item levels, tooltips, tier, etc.
     * This field is only for developers
     * to override item levels when items are generated
     */
    @Nullable
    private Optional<Integer> forcedItemLevel;

    /**
     * Forced item tier if not null.
     */
    @Nullable
    private Optional<ItemTier> forcedItemTier;

    /**
     * Forced tooltip if not null
     */
    @Nullable
    private Optional<TooltipTexture> forcedTooltip;

    /**
     * Switches to true when the item has been generated on the MMOItem level.
     * Then, only a set portions of options are still relevant to edit in this
     * class.
     */
    private boolean mmoitemGenerated;

    /**
     * Will the item be generated asynchronously? If people want to start generating
     * items async for extra performance, could be a thing. If toggled on, MMOItems
     * will call async events instead so that Bukkit does not complain.
     */
    @ApiStatus.Experimental
    private boolean async;

    @NotUsed
    @Deprecated
    public void setLevel(int level) {
        Validate.isTrue(!mmoitemGenerated, "Cannot change item level");

        this.forcedItemLevel = Optional.of(level);
    }

    @NotUsed
    @Deprecated
    public void setTier(@Nullable ItemTier tier) {
        Validate.isTrue(!mmoitemGenerated, "Cannot change item tier");

        this.forcedItemTier = Optional.ofNullable(tier);
    }

    /**
     * Unlike item tier and level which are rolled when generating the MMOItem, tooltips are
     * actually itemstack-level properties. This slightly increases versatility of tooltips
     * since editing the tooltip of an item tier/type after the item has been generated will
     * retroactively apply on item updates.
     *
     * @param tooltip Tooltip to force onto the item
     */
    public void setTooltip(@Nullable TooltipTexture tooltip) {
        this.forcedTooltip = Optional.ofNullable(tooltip);
    }

    public void markMMOItemBuilt() {
        Validate.isTrue(!mmoitemGenerated, "Already marked as MMOItem-built");

        this.mmoitemGenerated = true;
    }

    @Nullable
    @ApiStatus.Experimental
    public TooltipTexture resolveTooltip(ItemStackBuilder builder) {
        MMOItem mmoitem = builder.getMMOItem();

        // Forcefully apply tooltip if provided
        if (forcedTooltip != null) return forcedTooltip.orElse(null);

        // Check item first
        StatData data = mmoitem.getData(ItemStats.TOOLTIP);
        if (data != null) return MMOItems.plugin.getLore().getTooltip(data.toString());

        // Check item tier
        ItemTier tier = mmoitem.getTier();
        if (tier != null && tier.getTooltip() != null) return tier.getTooltip();

        // Check item type
        Type type = mmoitem.getType();
        if (type.getTooltip() != null) return type.getTooltip();

        return null;
    }
}
