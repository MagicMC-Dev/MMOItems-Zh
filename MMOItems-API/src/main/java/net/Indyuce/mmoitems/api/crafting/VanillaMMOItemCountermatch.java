package net.Indyuce.mmoitems.api.crafting;

import io.lumine.mythic.lib.api.crafting.uifilters.VanillaUIFilter;
import io.lumine.mythic.lib.api.crafting.uimanager.UIFilterCountermatch;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.api.util.ui.FriendlyFeedbackProvider;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * To make MMOItems not match as vanilla items (bruh)
 */
public class VanillaMMOItemCountermatch implements UIFilterCountermatch {

    /**
     * Activates this Counter Match, to prevent MMOItems from matching as vanilla items.
     */
    public static void enable() {

        // Activate
        VanillaUIFilter.get().addMatchOverride(new VanillaMMOItemCountermatch());
    }

    @Override
    public boolean preventsMatching(@NotNull ItemStack itemStack, @Nullable FriendlyFeedbackProvider friendlyFeedbackProvider) {

        // Does it have type? Then cancel lma0
        return NBTItem.get(itemStack).hasType();
    }
}
