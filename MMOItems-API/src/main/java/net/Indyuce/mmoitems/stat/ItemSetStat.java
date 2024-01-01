package net.Indyuce.mmoitems.stat;

import io.lumine.mythic.lib.api.item.ItemTag;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ItemSet;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.data.StringData;
import net.Indyuce.mmoitems.stat.type.GemStoneStat;
import net.Indyuce.mmoitems.stat.type.StringStat;
import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class ItemSetStat extends StringStat implements GemStoneStat {
    public ItemSetStat() {
        super("SET", Material.LEATHER_CHESTPLATE, "物品套装",
                new String[]{"物品套装可以为玩家提供额外的属性效果技能", "这些属性效果技能取决于你穿戴", "了多少同一套的物品"},
                new String[]{"!gem_stone", "!consumable", "!material", "!block", "!miscellaneous", "all"});
    }

    @Override
    public void whenClicked(@NotNull EditionInventory inv, @NotNull InventoryClickEvent e) {
        super.whenClicked(inv, e);
        if (e.getAction() != InventoryAction.PICKUP_HALF) {
            inv.getPlayer().sendMessage(ChatColor.GREEN + "可用的物品套装: ");
            StringBuilder builder = new StringBuilder();
            for (ItemSet set : MMOItems.plugin.getSets().getAll())
                builder.append(ChatColor.GREEN).append(set.getId()).append(ChatColor.GRAY)
                        .append(" (").append(set.getName()).append(ChatColor.GRAY).append("), ");
            if (builder.length() > 1)
                builder.setLength(builder.length() - 2);
            inv.getPlayer().sendMessage(builder.toString());
        }
    }

    @Override
    public void whenApplied(@NotNull ItemStackBuilder item, @NotNull StringData data) {

        // Display in lore
        ItemSet set = MMOItems.plugin.getSets().get(data.toString());

        // Apply lore
        if (set != null)
            item.getLore().insert("set", set.getLoreTag());

        // Add NBT
        item.addItemTag(getAppliedNBT(data));
    }

    @NotNull
    @Override
    public ArrayList<ItemTag> getAppliedNBT(@NotNull StringData data) {
        ItemSet set = MMOItems.plugin.getSets().get(data.toString());
        Validate.notNull(set, String.format("找不到 ID 为 '%s' 的物品套装", data));

        // Make Array
        ArrayList<ItemTag> ret = new ArrayList<>();

        // Add that tag
        ret.add(new ItemTag(getNBTPath(), data.toString()));

        // Thats it
        return ret;
    }

    @Override
    @NotNull
    public String getNBTPath() {
        return "MMOITEMS_ITEM_SET";
    }

    @Override
    public void whenInput(@NotNull EditionInventory inv, @NotNull String message, Object... info) {
        ItemSet set = MMOItems.plugin.getSets().get(message);
        Validate.notNull(set, String.format("找不到名为 '%s' 的套装", message));
        super.whenInput(inv, message, info);
    }
}
