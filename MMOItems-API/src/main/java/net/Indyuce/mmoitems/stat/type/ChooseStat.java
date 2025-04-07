package net.Indyuce.mmoitems.stat.type;

import io.lumine.mythic.lib.api.util.AltChar;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.data.StringData;
import net.Indyuce.mmoitems.util.MMOUtils;
import net.Indyuce.mmoitems.util.StatChoice;
import io.lumine.mythic.lib.util.lang3.Validate;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Choose Stats present a list of options from which the user may choose one.
 * <p></p>
 * You could consider them a more advanced DisableStat, while DisableStat only
 * allows to choose <b>true</b> or <b>false</b>, alternating when clicked, Choose
 * Stats cycle through a list instead.
 *
 * @author Gunging
 */
public abstract class ChooseStat extends StringStat {

    /**
     * Contains the list of different options the player may choose from.
     * <b>Make sure its is always initialized and with at least 1 element</b>
     */
    private final List<StatChoice> choices = new ArrayList<>();

    public ChooseStat(String id, Material mat, String name, String[] lore, String[] types, Material... materials) {
        super(id, mat, name, lore, types, materials);
    }

    public void addChoices(StatChoice... choices) {
        this.choices.addAll(Arrays.asList(choices));
    }

    @Nullable
    public StatChoice getChoice(String id) {
        for (StatChoice choice : choices)
            if (choice.getId().equals(id))
                return choice;
        return null;
    }

    @Override
    public void whenClicked(@NotNull EditionInventory inv, @NotNull InventoryClickEvent event) {
        Validate.isTrue(!choices.isEmpty(), "基于选择的统计数据无效 '" + getId() + ": 没有可供选择的选项");

        // If removing, reset to default
        if (event.getAction() == InventoryAction.PICKUP_HALF) {

            // Edits into persistent files
            inv.getEditedSection().set(getPath(), null);
            inv.registerTemplateEdition();

            // Mention that it was removed
            inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "已成功删除" + getName() + ".");

        } else {

            // Get current
            String found = inv.getEditedSection().getString(getPath());
            int currentIndex = found == null ? -1 : choices.indexOf(getChoice(found));

            // Increase and Cap
            if (++currentIndex >= choices.size()) currentIndex = 0;

            // Edits into persistent files
            StatChoice current = choices.get(currentIndex);
            inv.getEditedSection().set(getPath(), current.getId());
            inv.registerTemplateEdition();

            // Sends a message
            inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + getName() + "成功更改为  " + current + ChatColor.GRAY + "");
        }
    }

    @Override
    public void whenDisplayed(List<String> lore, Optional<StringData> statData) {
        Validate.isTrue(!choices.isEmpty(), "基于选择的统计数据无效 '" + getId() + ": 没有可供选择的选项");

        // To display current choosing, gets the very first element
        @Nullable StatChoice found = statData.isPresent() ? getChoice(statData.get().toString()) : null;
        lore.add(ChatColor.GRAY + "当前值: " + (found != null ? ChatColor.GREEN + found.getId() : ChatColor.RED + "None"));

        // Display Definition
        if (found != null && found.getHint() != null) for (String definition : MMOUtils.trimString(LORE_LINE_WIDTH, found.getHint()))
            lore.add(ChatColor.GRAY + " " + definition);

        lore.add("");
        lore.add(ChatColor.YELLOW + AltChar.listDash + " 右键单击可返回默认值");
        lore.add(ChatColor.YELLOW + AltChar.listDash + " 左键单击可循环显示可用选项: ");
        for (StatChoice existing : choices) {

            // Is it the one?
            String pick = existing.equals(found) ? ChatColor.RED.toString() + ChatColor.BOLD : ChatColor.GOLD.toString();
            lore.add(pick + "  " + AltChar.smallListDash + " " + ChatColor.GRAY + existing.getId());
        }
    }
}