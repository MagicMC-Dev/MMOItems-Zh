package net.Indyuce.mmoitems.api;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.util.MMOUtils;
import io.lumine.mythic.lib.version.VersionMaterial;

public enum CustomSound {
    ON_ATTACK(Material.IRON_SWORD, 19, "Plays when attacking an entity."),
    ON_RIGHT_CLICK(Material.STONE_HOE, 22, "Plays when item is right-clicked."),
    ON_BLOCK_BREAK(Material.COBBLESTONE, 25, "Plays when a block is broken with the item."),
    ON_PICKUP(Material.IRON_INGOT, 28, "Plays when you pickup the item from the ground."),
    ON_LEFT_CLICK(Material.STONE_AXE, 31, "Plays when item is left-clicked."),
    ON_CRAFT(VersionMaterial.CRAFTING_TABLE.toMaterial(), 34, "Plays when item is crafted in a crafting inventory", "or when smelted from someting in a furnace."),
    ON_CONSUME(Material.APPLE, 37, "Plays when item has been consumed.", "(After eating/drinking animation)"),
    ON_ITEM_BREAK(Material.FLINT, 40, "Plays when the item breaks."),
    ON_CROSSBOW(Material.ARROW, 38, "Plays when a crossbow shoots an arrow."),
    ON_PLACED(Material.STONE, 43, "Plays when the block is placed.");

    private final ItemStack item;
    private final String[] lore;
    private final int slot;

    CustomSound(Material material, int slot, String... lore) {
        this.item = new ItemStack(material);
        this.lore = lore;
        this.slot = slot;
    }

    public ItemStack getItem() {
        return item;
    }

    public String getName() {
        return MMOUtils.caseOnWords(name().toLowerCase().replace('_', ' '));
    }

    public String[] getLore() {
        return lore;
    }

    public int getSlot() {
        return slot;
    }
}