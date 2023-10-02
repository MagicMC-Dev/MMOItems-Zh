package net.Indyuce.mmoitems.api;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.util.MMOUtils;
import io.lumine.mythic.lib.version.VersionMaterial;

public enum CustomSound {
    ON_ATTACK(Material.IRON_SWORD, 19, "攻击实体时播放。"),
    ON_RIGHT_CLICK(Material.STONE_HOE, 22, "右键单击物品时播放。"),
    ON_BLOCK_BREAK(Material.COBBLESTONE, 25, "当方块被物品破坏时播放。"),
    ON_PICKUP(Material.IRON_INGOT, 28, "当从地上捡起物品时播放。"),
    ON_LEFT_CLICK(Material.STONE_AXE, 31, "左键单击项目时播放。"),
    ON_CRAFT(VersionMaterial.CRAFTING_TABLE.toMaterial(), 34, "在制作物品栏中制作物品时播放", "or when smelted from someting in a furnace."),
    ON_CONSUME(Material.APPLE, 37, "物品被消耗后播放。", "（吃/喝后动画）"),
    ON_ITEM_BREAK(Material.FLINT, 40, "当物品损坏时播放。"),
    ON_CROSSBOW(Material.ARROW, 38, "当弩射出箭时播放。"),
    ON_PLACED(Material.STONE, 43, "放置方块时播放。");

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