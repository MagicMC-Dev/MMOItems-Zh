package net.Indyuce.mmoitems.api.event;

import net.Indyuce.mmoitems.api.item.build.LoreBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.List;

/**
 * @deprecated Will be removed in the future. SCRIPTS must replace these
 *         because there are  no events which can support both sync and async
 */
@Deprecated
public class GenerateLoreEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    private final MMOItem item;
    private final List<String> lore, parsed;
    private final LoreBuilder builder;

    public GenerateLoreEvent(MMOItem item, LoreBuilder builder, List<String> lore, List<String> parsed) {
        this.item = item;
        this.lore = lore;
        this.parsed = parsed;
        this.builder = builder;
    }

    /**
     * @return The MMOItem the lore is being generated for
     */
    public MMOItem getItem() {
        return item;
    }

    /**
     * @return The LoreBuilder used to build this lore.
     */
    public LoreBuilder getBuilder() {
        return builder;
    }

    /**
     * @return The list of pre-placeholder lore (before any placeholders have been parsed)
     */
    public List<String> getLore() {
        return lore;
    }

    /**
     * @return The list of parsed lore (after all placeholders have been parsed)
     */
    public List<String> getParsedLore() {
        return parsed;
    }

    /**
     * Sets the final lore of the item.
     * <p>
     * This sets the parsed lore, so any future calls of
     * getParsedLore() will return the new list.
     *
     * @param lore The new lore of the item
     */
    public void setFinalLore(List<String> lore) {
        this.parsed.clear();
        this.parsed.addAll(lore);
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}