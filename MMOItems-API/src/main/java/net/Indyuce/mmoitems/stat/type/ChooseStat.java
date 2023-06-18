package net.Indyuce.mmoitems.stat.type;

import io.lumine.mythic.lib.api.util.AltChar;
import io.lumine.mythic.lib.api.util.ui.SilentNumbers;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.data.StringData;
import net.Indyuce.mmoitems.stat.data.random.RandomStatData;
import net.Indyuce.mmoitems.util.StatChoice;
import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

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
        Validate.isTrue(choices.size() > 0, "Invalid choice-based stat '" + getId() + ": no options to choose from.");

        // If removing, reset to default
        if (event.getAction() == InventoryAction.PICKUP_HALF) {

            // Edits into persistent files
            inv.getEditedSection().set(getPath(), null);
            inv.registerTemplateEdition();

            // Mention that it was removed
            inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "Successfully removed " + getName() + ".");

        } else {

            // Get current
            StatChoice current = getChoice(inv.getEditedSection().getString(getPath()));

            // Included?
            int currentIndex = current != null ? Math.max(0, choices.indexOf(current)) : 0;

            // Increase and Cap
            if (++currentIndex >= choices.size()) currentIndex = 0;

            // Get
            current = choices.get(currentIndex);

            // Edits into persistent files
            inv.getEditedSection().set(getPath(), current.getId());
            inv.registerTemplateEdition();

            // Sends a message
            inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + getName() + " successfully changed to " + current + ChatColor.GRAY + ".");
        }
    }

    @Override
    public void whenDisplayed(List<String> lore, Optional<StringData> statData) {
        Validate.isTrue(choices.size() > 0, "Invalid choice-based stat '" + getId() + ": no options to choose from.");

        // To display current choosing, gets the very first element
        StatChoice def = statData.isPresent() ? getChoice(statData.get().toString()) : choices.get(0);
        lore.add(ChatColor.GRAY + "Current Value: " + (statData.isPresent() ? ChatColor.GREEN : ChatColor.RED) + def);

        // Display Definition
        for (String definition : SilentNumbers.chop(def.getHint(), 50, ""))
            lore.add(ChatColor.GRAY + " " + definition);

        lore.add("");
        lore.add(ChatColor.YELLOW + AltChar.listDash + " Right click to return to default value.");
        lore.add(ChatColor.YELLOW + AltChar.listDash + " Left click to cycle through the available options:");
        for (StatChoice existing : choices) {

            // Is it the one?
            String pick = existing.equals(def) ? ChatColor.RED.toString() + ChatColor.BOLD : ChatColor.GOLD.toString();
            lore.add(pick + "  " + AltChar.smallListDash + " " + ChatColor.GRAY + existing.getId());
        }
    }
}