package net.Indyuce.mmoitems.gui.edition;

import io.lumine.mythic.lib.api.util.ui.SilentNumbers;
import io.lumine.mythic.lib.version.VersionMaterial;
import io.lumine.mythic.lib.api.util.ItemFactory;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.util.MMOUtils;
import net.Indyuce.mmoitems.api.item.template.MMOItemTemplate;
import net.Indyuce.mmoitems.api.util.MMOItemReforger;
import net.Indyuce.mmoitems.stat.RevisionID;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Inventory displayed when enabling the item updater.
 * @see RevisionID
 * @see MMOItemReforger
 *
 * @author Gunging
 */
public class RevisionInventory extends EditionInventory {

    static ItemStack name;
    static ItemStack lore;
    static ItemStack enchantments;
    static ItemStack upgrades;
    static ItemStack gemstones;
    static ItemStack soulbind;
    static ItemStack external;

    static ItemStack revisionID;

    static final String REVISION = "\u00a76Revision ID";

    public RevisionInventory(@NotNull Player player, @NotNull MMOItemTemplate template) {
        super(player, template);

        // If null
        if (revisionID == null) {

            name = ItemFactory.of(Material.NAME_TAG).name("\u00a73Name").lore(SilentNumbers.chop(
                    "The display name of the old item will be transferred to the new one"
                    , 40, "\u00a77")).build();

            lore = ItemFactory.of(VersionMaterial.WRITABLE_BOOK.toMaterial()).name("\u00a7dLore").lore(SilentNumbers.chop(
                    "Specifically keeps lore lines that begin with the color code \u00a7n&7"
                    , 40, "\u00a77")).build();

            enchantments = ItemFactory.of(Material.EXPERIENCE_BOTTLE).name("\u00a7bEnchantments").lore(SilentNumbers.chop(
                    "This keeps specifically enchantments that are not accounted for in upgrades nor gem stones (presumably added by the player)."
                    , 40, "\u00a77")).build();

            upgrades = ItemFactory.of(Material.NETHER_STAR).name("\u00a7aUpgrades").lore(SilentNumbers.chop(
                    "Will this item retain the upgrade level after updating? Only the Upgrade Level is kept (as long as it does not exceed the new max)."
                    , 40, "\u00a77")).build();

            gemstones = ItemFactory.of(Material.EMERALD).name("\u00a7eGem Stones").lore(SilentNumbers.chop(
                    "Will the item retain its gem stones when updating? (Note that this allows gemstone overflow - will keep ALL old gemstones even if you reduced the gem sockets)"
                    , 40, "\u00a77")).build();

            soulbind = ItemFactory.of(Material.ENDER_EYE).name("\u00a7cSoulbind").lore(SilentNumbers.chop(
                    "If the old item is soulbound, updating will transfer the soulbind to the new item."
                    , 40, "\u00a77")).build();

            external = ItemFactory.of(Material.SPRUCE_SIGN).name("\u00a79External SH").lore(SilentNumbers.chop(
                    "Data registered onto the item's StatHistory by external plugins (like GemStones but not removable)"
                    , 40, "\u00a77")).build();


            // Fill stack
            revisionID = ItemFactory.of(Material.ITEM_FRAME).name(REVISION).lore(SilentNumbers.chop(
                    "The updater is always active, increasing this number will update all instances of this MMOItem without further action."
                    , 40, "\u00a77")).build();
        }
    }

    @NotNull @Override public Inventory getInventory() {
        Inventory inv = Bukkit.createInventory(this, 54, "Revision Manager");

        // Place corresponding item stacks in there
        for (int i = 0; i < inv.getSize(); i++) {

            // What item to even put here
            ItemStack which = null;
            Boolean enable = null;
            Integer id = null;
            switch (i) {
                case 19:
                    which = name.clone();
                    enable = MMOItems.plugin.getLanguage().revisionOptions.shouldKeepName();
                    break;
                case 20:
                    which = lore.clone();
                    enable = MMOItems.plugin.getLanguage().revisionOptions.shouldKeepLore();
                    break;
                case 21:
                    which = enchantments.clone();
                    enable = MMOItems.plugin.getLanguage().revisionOptions.shouldKeepEnchantments();
                    break;
                case 22:
                    which = external.clone();
                    enable = MMOItems.plugin.getLanguage().revisionOptions.shouldKeepExternalSH();
                    break;
                case 28:
                    which = upgrades.clone();
                    enable = MMOItems.plugin.getLanguage().revisionOptions.shouldKeepUpgrades();
                    break;
                case 29:
                    which = gemstones.clone();
                    enable = MMOItems.plugin.getLanguage().revisionOptions.shouldKeepGemStones();
                    break;
                case 30:
                    which = soulbind.clone();
                    enable = MMOItems.plugin.getLanguage().revisionOptions.shouldKeepSoulBind();
                    break;
                case 33:
                    id = getEditedSection().getInt(ItemStats.REVISION_ID.getPath(), 1);
                    which = revisionID.clone();
                    break;
                default:
                    break;
            }

            // If an item corresponds to this slot
            if (which != null) {

                // If it is defined to be enabled
                if (enable != null) {

                    // Add mentioning if enabled
                    inv.setItem(i, addLore(which, "", "\u00a78Enabled (in config)? \u00a76" + enable.toString()));

                // If ID is enabled
                } else if (id != null) {

                    // Add mentioning if enabled
                    inv.setItem(i, addLore(which, "", "\u00a78Current Value: \u00a76" + id));

                // Neither enable nor ID are defined
                } else {

                    // Add
                    inv.setItem(i, which);
                }
            }
        }

        // Add the top items, including the {Back} button
        addEditionInventoryItems(inv, true);

        return inv;
    }

    @Override
    public void whenClicked(InventoryClickEvent event) {
        // Get the clicked item
        ItemStack item = event.getCurrentItem();
        event.setCancelled(true);

        // If the click did not happen in the correct inventory, or the item is not clickable
        if (event.getInventory() != event.getClickedInventory() || !MMOUtils.isMetaItem(item, false)) { return; }

        // Is the player clicking the revision ID thing?
        if (item.getItemMeta().getDisplayName().equals(REVISION)) {

            // Get the current ID
            int id = getEditedSection().getInt(ItemStats.REVISION_ID.getPath(), 1);

            // If right click
            if (event.getAction() == InventoryAction.PICKUP_HALF) {

                // Decrease by 1, but never before 1
                id = Math.max(id - 1, 1);

            // Any other click
            } else {

                // Increase by 1 until the ultimate maximum
                id = Math.min(id + 1, Integer.MAX_VALUE);

            }

            // Register edition
            getEditedSection().set(ItemStats.REVISION_ID.getPath(), id);
            registerTemplateEdition();

            // Update ig
            event.setCurrentItem(addLore(revisionID.clone(), "", "\u00a78Current Value: \u00a76" + id));
        }
    }

    @NotNull ItemStack addLore(@NotNull ItemStack iSource, String... extraLines){

        // Get its lore
        ArrayList<String> iLore = new ArrayList<>();
        ItemMeta iMeta = iSource.getItemMeta();
        if (iMeta != null && iMeta.getLore() != null) {iLore = new ArrayList<>(iMeta.getLore()); }

        // Add lines
        iLore.addAll(Arrays.asList(extraLines));

        // Put lore
        iMeta.setLore(iLore);

        // Yes
        iSource.setItemMeta(iMeta);

        // Yes
        return iSource;
    }
}
