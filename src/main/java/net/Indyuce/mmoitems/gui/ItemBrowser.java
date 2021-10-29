package net.Indyuce.mmoitems.gui;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.api.util.AltChar;
import io.lumine.mythic.lib.version.VersionMaterial;
import io.lumine.mythic.utils.adventure.text.Component;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.edition.NewItemEdition;
import net.Indyuce.mmoitems.api.item.template.MMOItemTemplate;
import net.Indyuce.mmoitems.gui.edition.ItemEdition;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class ItemBrowser extends PluginInventory {
    private final Map<String, ItemStack> cached = new LinkedHashMap<>();

    private final Type type;
    private boolean deleteMode;

    // Slots used to display items based on the item type explored
    private static final int[] slots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34};
    private static final int[] slotsAlt = {1, 2, 3, 4, 5, 6, 7, 10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34};

    public ItemBrowser(Player player) {
        this(player, null);
    }

    public ItemBrowser(Player player, Type type) {
        super(player);

        this.type = type;
    }


    @Override
    public Inventory getInventory() {
        int[] usedSlots = type != null && type.isFourGUIMode() ? slotsAlt : slots;
        int min = (page - 1) * usedSlots.length;
        int max = page * usedSlots.length;
        int n = 0;

        /*
         * Displays all possible item types if no
         * type was previously selected by the player
         */
        if (type == null) {
            Inventory inv = Bukkit.createInventory(this, 54, "Item Explorer");
            List<Type> types = new ArrayList<>(MMOItems.plugin.getTypes().getAll());
            for (int j = min; j < Math.min(max, types.size()); j++) {
                Type type = types.get(j);
                int items = MMOItems.plugin.getTemplates().getTemplates(type).size();

                ItemStack item = type.getItem();
                item.setAmount(Math.max(1, Math.min(64, items)));
                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName(ChatColor.GREEN + type.getName() + ChatColor.DARK_GRAY + " (Click to browse)");
                meta.addItemFlags(ItemFlag.values());
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.GRAY + "" + ChatColor.ITALIC + "There " + (items != 1 ? "are" : "is") + " "
                        + (items < 1 ? "" + ChatColor.RED + ChatColor.ITALIC + "no" : "" + ChatColor.GOLD + ChatColor.ITALIC + items) + ChatColor.GRAY
                        + ChatColor.ITALIC + " item" + (items != 1 ? "s" : "") + " in that type.");
                meta.setLore(lore);
                item.setItemMeta(meta);

                inv.setItem(slots[n++], NBTItem.get(item).addTag(new ItemTag("typeId", type.getId())).toItem());
            }

            ItemStack glass = VersionMaterial.GRAY_STAINED_GLASS_PANE.toItem();
            ItemMeta glassMeta = glass.getItemMeta();
            glassMeta.setDisplayName(ChatColor.RED + "- No type -");
            glass.setItemMeta(glassMeta);

            ItemStack next = new ItemStack(Material.ARROW);
            ItemMeta nextMeta = next.getItemMeta();
            nextMeta.setDisplayName(ChatColor.GREEN + "Next Page");
            next.setItemMeta(nextMeta);

            ItemStack previous = new ItemStack(Material.ARROW);
            ItemMeta previousMeta = previous.getItemMeta();
            previousMeta.setDisplayName(ChatColor.GREEN + "Previous Page");
            previous.setItemMeta(previousMeta);

            while (n < slots.length)
                inv.setItem(slots[n++], glass);
            inv.setItem(18, page > 1 ? previous : null);
            inv.setItem(26, max >= MMOItems.plugin.getTypes().getAll().size() ? null : next);

            return inv;
        }

        ItemStack error = VersionMaterial.RED_STAINED_GLASS_PANE.toItem();
        ItemMeta errorMeta = error.getItemMeta();
        errorMeta.setDisplayName(ChatColor.RED + "- Error -");
        List<String> errorLore = new ArrayList<>();
        errorLore.add(ChatColor.GRAY + "" + ChatColor.ITALIC + "An error occurred while");
        errorLore.add(ChatColor.GRAY + "" + ChatColor.ITALIC + "trying to generate that item.");
        errorMeta.setLore(errorLore);
        error.setItemMeta(errorMeta);

        List<MMOItemTemplate> templates = new ArrayList<>(MMOItems.plugin.getTemplates().getTemplates(type));

        /*
         * Displays every item in a specific type. Items are cached inside the
         * map at the top to reduce performance impact and are directly rendered
         */
        Inventory inv = Bukkit.createInventory(this, 54, (deleteMode ? ("Delete Mode: ") : ("Item Explorer: ")) + type.getName());
        for (int j = min; j < Math.min(max, templates.size()); j++) {
            MMOItemTemplate template = templates.get(j);
            ItemStack item = template.newBuilder(playerData.getRPG()).build().newBuilder().build();
            if (item == null || item.getType() == Material.AIR) {
                cached.put(template.getId(), error);
                inv.setItem(usedSlots[n++], error);
                continue;
            }

            ItemMeta meta = item.getItemMeta();
            List<String> lore = meta.getLore();
            lore.add("");

            if (deleteMode) {
                lore.add(ChatColor.RED + AltChar.cross + " CLICK TO DELETE " + AltChar.cross);
                meta.setDisplayName(ChatColor.RED + "DELETE: " + (meta.hasDisplayName() ? meta.getDisplayName() : MMOUtils.getDisplayName(item)));
            } else {
                lore.add(ChatColor.YELLOW + AltChar.smallListDash + " Left click to obtain this item.");
                lore.add(ChatColor.YELLOW + AltChar.smallListDash + " Right click to edit this item.");
            }

            meta.setLore(lore);
            item.setItemMeta(meta);
            cached.put(template.getId(), item);

            inv.setItem(usedSlots[n++], cached.get(template.getId()));
        }

        ItemStack noItem = VersionMaterial.GRAY_STAINED_GLASS_PANE.toItem();
        ItemMeta noItemMeta = noItem.getItemMeta();
        noItemMeta.setDisplayName(ChatColor.RED + "- No Item -");
        noItem.setItemMeta(noItemMeta);

        ItemStack next = new ItemStack(Material.ARROW);
        ItemMeta nextMeta = next.getItemMeta();
        nextMeta.setDisplayName(ChatColor.GREEN + "Next Page");
        next.setItemMeta(nextMeta);

        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.setDisplayName(ChatColor.GREEN + AltChar.rightArrow + " Back");
        back.setItemMeta(backMeta);

        ItemStack create = new ItemStack(VersionMaterial.WRITABLE_BOOK.toMaterial());
        ItemMeta createMeta = create.getItemMeta();
        createMeta.setDisplayName(ChatColor.GREEN + "Create New");
        create.setItemMeta(createMeta);

        ItemStack delete = new ItemStack(VersionMaterial.CAULDRON.toMaterial());
        ItemMeta deleteMeta = delete.getItemMeta();
        deleteMeta.setDisplayName(ChatColor.RED + (deleteMode ? "Cancel Deletion" : "Delete Item"));
        delete.setItemMeta(deleteMeta);

        ItemStack previous = new ItemStack(Material.ARROW);
        ItemMeta previousMeta = previous.getItemMeta();
        previousMeta.setDisplayName(ChatColor.GREEN + "Previous Page");
        previous.setItemMeta(previousMeta);

        if (type == Type.BLOCK) {
            ItemStack downloadPack = new ItemStack(Material.HOPPER);
            ItemMeta downloadMeta = downloadPack.getItemMeta();
            downloadMeta.setDisplayName(ChatColor.GREEN + "Download Default Resourcepack");
            downloadMeta.setLore(Arrays.asList(ChatColor.LIGHT_PURPLE + "Only seeing stone blocks?", "",
                    ChatColor.RED + "By downloading the default resourcepack you can", ChatColor.RED + "edit the blocks however you want.",
                    ChatColor.RED + "You will still have to add it to your server!"));
            downloadPack.setItemMeta(downloadMeta);
            inv.setItem(45, downloadPack);
        }

        while (n < usedSlots.length)
            inv.setItem(usedSlots[n++], noItem);
        if (!deleteMode)
            inv.setItem(51, create);
        inv.setItem(47, delete);
        inv.setItem(49, back);
        inv.setItem(18, page > 1 ? previous : null);
        inv.setItem(26, max >= templates.size() ? null : next);
        return inv;
    }

    public Type getType() {
        return type;
    }

    @Override
    public void whenClicked(InventoryClickEvent event) {
        event.setCancelled(true);
        if (event.getInventory() != event.getClickedInventory())
            return;

        ItemStack item = event.getCurrentItem();
        if (MMOUtils.isMetaItem(item, false)) {
            if (item.getItemMeta().getDisplayName().equals(ChatColor.GREEN + "Next Page")) {
                page++;
                open();
            } else if (item.getItemMeta().getDisplayName().equals(ChatColor.GREEN + "Previous Page")) {
                page--;
                open();
            } else if (item.getItemMeta().getDisplayName().equals(ChatColor.GREEN + AltChar.rightArrow + " Back"))
                new ItemBrowser(getPlayer()).open();

            else if (item.getItemMeta().getDisplayName().equals(ChatColor.RED + "Cancel Deletion")) {
                deleteMode = false;
                open();
            } else if (item.getItemMeta().getDisplayName().equals(ChatColor.GREEN + "Create New"))
                new NewItemEdition(this).enable("Write in the chat the text you want.");

            else if (type != null && item.getItemMeta().getDisplayName().equals(ChatColor.RED + "Delete Item")) {
                deleteMode = true;
                open();
            } else if (item.getItemMeta().getDisplayName().equals(ChatColor.GREEN + "Download Default Resourcepack")) {
                MythicLib.plugin.getVersion().getWrapper().sendJson(getPlayer(),
                        "[{\"text\":\"Click to download!\",\"color\":\"green\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://drive.google.com/uc?id=1FjV7y-2cn8qzSiktZ2CUXmkdjepXdj5N\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":[\"\",{\"text\":\"https://drive.google.com/uc?id=1FjV7y-2cn8qzSiktZ2CUXmkdjepXdj5N\",\"italic\":true,\"color\":\"white\"}]}}]");
                getPlayer().closeInventory();
            } else if (type == null && !item.getItemMeta().getDisplayName().equals(ChatColor.RED + "- No type -"))
                new ItemBrowser(getPlayer(), MMOItems.plugin.getTypes().get(NBTItem.get(item).getString("typeId"))).open();
        }

        String id = NBTItem.get(item).getString("MMOITEMS_ITEM_ID");
        if (id.equals(""))
            return;

        if (deleteMode) {
            MMOItems.plugin.getTemplates().deleteTemplate(type, id);
            deleteMode = false;
            open();

        } else {
            if (event.getAction() == InventoryAction.PICKUP_ALL) {
                // this refreshes the item if it's unstackable
                ItemStack generatedItem = (NBTItem.get(item).getBoolean("UNSTACKABLE")) ? MMOItems.plugin.getItem(type, id, playerData)
                        : removeLastLoreLines(NBTItem.get(item));
                getPlayer().getInventory().addItem(generatedItem);
                getPlayer().playSound(getPlayer().getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 2);
            }

            if (event.getAction() == InventoryAction.PICKUP_HALF)
                new ItemEdition(getPlayer(), MMOItems.plugin.getTemplates().getTemplate(type, id)).open();
        }
    }

    private ItemStack removeLastLoreLines(NBTItem item) {
        List<Component> lore = item.getLoreComponents();
        item.setLoreComponents(lore.subList(0, lore.size() - 3));
        return item.toItem();
    }
}
