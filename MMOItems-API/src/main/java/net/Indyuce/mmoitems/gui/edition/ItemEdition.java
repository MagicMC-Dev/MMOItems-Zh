package net.Indyuce.mmoitems.gui.edition;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.version.VersionMaterial;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.item.template.MMOItemTemplate;
import net.Indyuce.mmoitems.stat.type.InternalStat;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.Indyuce.mmoitems.util.MMOUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ItemEdition extends EditionInventory {
    private static final int[] slots = {19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43};

    public ItemEdition(Player player, MMOItemTemplate template) {
        super(player, template);
    }

    @Override
    public Inventory getInventory() {
        int min = (page - 1) * slots.length;
        int max = page * slots.length;
        int n = 0;

        /*
         * it has to determine what stats can be applied first because otherwise
         * the for loop will just let some slots empty
         */
        List<ItemStat> appliable = new ArrayList<>(getEdited().getType().getAvailableStats()).stream()
                .filter(stat -> stat.hasValidMaterial(getCachedItem()) && !(stat instanceof InternalStat)).toList();

        Inventory inv = Bukkit.createInventory(this, 54, "Item Edition: " + getEdited().getId());
        for (int j = min; j < Math.min(appliable.size(), max); j++) {
            ItemStat stat = appliable.get(j);
            ItemStack item = new ItemStack(stat.getDisplayMaterial());
            ItemMeta meta = item.getItemMeta();
            meta.addItemFlags(ItemFlag.values());
            meta.setDisplayName(ChatColor.GREEN + stat.getName());
            List<String> lore = MythicLib.plugin.parseColors(Arrays.stream(stat.getLore()).map(s -> ChatColor.GRAY + s).toList());
            lore.add("");

            stat.whenDisplayed(lore, getEventualStatData(stat));

            meta.setLore(lore);
            item.setItemMeta(meta);
            inv.setItem(slots[n++], MythicLib.plugin.getVersion().getWrapper().getNBTItem(item).addTag(new ItemTag("guiStat", stat.getId())).toItem());
        }

        ItemStack glass = VersionMaterial.GRAY_STAINED_GLASS_PANE.toItem();
        ItemMeta glassMeta = glass.getItemMeta();
        glassMeta.setDisplayName(ChatColor.RED + "- No Item Stat -");
        glass.setItemMeta(glassMeta);

        ItemStack next = new ItemStack(Material.ARROW);
        ItemMeta nextMeta = next.getItemMeta();
        nextMeta.setDisplayName(ChatColor.GREEN + "Next Page");
        next.setItemMeta(nextMeta);

        ItemStack previous = new ItemStack(Material.ARROW);
        ItemMeta previousMeta = previous.getItemMeta();
        previousMeta.setDisplayName(ChatColor.GREEN + "Previous Page");
        previous.setItemMeta(previousMeta);

        addEditionInventoryItems(inv, true);

        while (n < slots.length)
            inv.setItem(slots[n++], glass);
        inv.setItem(27, page > 1 ? previous : null);
        inv.setItem(35, appliable.size() > max ? next : null);

        return inv;
    }

    @Override
    public void whenClicked(InventoryClickEvent event) {
        event.setCancelled(true);
        if (event.getInventory() != event.getClickedInventory())
            return;

        ItemStack item = event.getCurrentItem();
        if (!MMOUtils.isMetaItem(item, false) || event.getInventory().getItem(4) == null)
            return;

        if (item.getItemMeta().getDisplayName().equals(ChatColor.GREEN + "Next Page")) {
            page++;
            open();
        }

        if (item.getItemMeta().getDisplayName().equals(ChatColor.GREEN + "Previous Page")) {
            page--;
            open();
        }

        final String tag = NBTItem.get(item).getString("guiStat");
        if (tag.isEmpty())
            return;

        // Check for OP stats
        final ItemStat edited = MMOItems.plugin.getStats().get(tag);
        if (MMOItems.plugin.hasPermissions() && MMOItems.plugin.getLanguage().opStatsEnabled
                && MMOItems.plugin.getLanguage().opStats.contains(edited.getId())
                && !MMOItems.plugin.getVault().getPermissions().has((Player) event.getWhoClicked(), "mmoitems.edit.op")) {
            event.getWhoClicked().sendMessage(ChatColor.RED + "You are lacking permission to edit this stat.");
            return;
        }

        edited.whenClicked(this, event);
    }

    public ItemEdition onPage(int value) {
        page = value;
        return this;
    }
}