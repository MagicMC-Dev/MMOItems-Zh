package net.Indyuce.mmoitems.comp.mythicmobs.crafting;

import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.items.MythicItem;
import io.lumine.mythic.lib.api.MMOLineConfig;
import net.Indyuce.mmoitems.api.crafting.ingredient.Ingredient;
import net.Indyuce.mmoitems.api.player.RPGPlayer;
import org.apache.commons.lang.Validate;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * No way of checking if an item was generated using MythicMobs? There does
 * not seem to be any NBTTag for that using the latest dev build
 *
 * @deprecated Not implemented yet
 */
@Deprecated
public class MythicItemIngredient extends Ingredient<MythicItemPlayerIngredient> {
    private final MythicItem mythicitem;

    private final String display;

    public MythicItemIngredient(MMOLineConfig config) {
        super("mythicitem", config);

        config.validate("item");
        Optional<MythicItem> mmitem = MythicBukkit.inst().getItemManager().getItem(config.getString("item"));
        Validate.isTrue(mmitem.isPresent(), "Could not find MM Item with ID '" + config.getString("item") + "'");

        display = config.contains("display") ? config.getString("display") : mmitem.get().getDisplayName();
        mythicitem = mmitem.get();
    }

    @Override
    public String getKey() {
        return "mythicitem:" + mythicitem.getInternalName().toLowerCase();
    }

    @Override
    public String formatDisplay(String s) {
        return s.replace("#item#", display).replace("#amount#", "" + getAmount());
    }

    @Override
    public boolean matches(MythicItemPlayerIngredient playerIngredient) {
        return false;
    }

    @NotNull
    @Override
    public ItemStack generateItemStack(@NotNull RPGPlayer player, boolean forDisplay) {
        return BukkitAdapter.adapt(mythicitem.generateItemStack(getAmount()));
    }

    @Override
    public String toString() {
        return getKey();
    }
}
