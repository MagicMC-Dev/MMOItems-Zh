package net.Indyuce.mmoitems.api.item.template.explorer;

import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.api.item.template.MMOItemTemplate;
import net.Indyuce.mmoitems.stat.data.random.RandomStatData;

import javax.annotation.Nullable;
import java.util.function.Predicate;

/**
 * Filters items with a specific tier.
 * <p>
 * The 'tier:COMMON' has the effect of setting the item tier
 * to a specific tier. What that filter does is that it lets
 * the user select GROUPS of items, and these groups correspond
 * to tiers; these are two different and possible uses for tiers.
 */
public class TierFilter implements Predicate<MMOItemTemplate> {
    private final String id;

    public TierFilter(String id) {
        this.id = id;
    }

    @Override
    public boolean test(MMOItemTemplate template) {
        final @Nullable RandomStatData found = template.getBaseItemData().get(ItemStats.TIER);
        return found != null && found.toString().equalsIgnoreCase(id);
    }
}
