package net.Indyuce.mmoitems.api.crafting;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

public class Layout {
    private final String id;

    private final List<Integer> recipeSlots, queueSlots;

    private final int size, recipePreviousSlot, recipeNextSlot, queuePreviousSlot, queueNextSlot;

    public Layout(String id, FileConfiguration config) {
        this.id = id;
        this.size = config.getInt("slots");

        ConfigurationSection section = config.getConfigurationSection("layout");

        // array slots
        this.recipeSlots = section.getIntegerList("recipe-slots");
        this.queueSlots = section.getIntegerList("queue-slots");

        // singular slots
        this.recipePreviousSlot = section.getInt("recipe-previous-slot", 18);
        this.recipeNextSlot = section.getInt("recipe-next-slot", 26);
        this.queuePreviousSlot = section.getInt("queue-previous-slot", 37);
        this.queueNextSlot = section.getInt("queue-next-slot", 43);

    }

    public String getId() {
        return id;
    }

    public int getSize() {
        return size;
    }

    public List<Integer> getRecipeSlots() {
        return recipeSlots;
    }

    public List<Integer> getQueueSlots() {
        return queueSlots;
    }

    public int getRecipePreviousSlot() {
        return recipePreviousSlot;
    }

    public int getRecipeNextSlot() {
        return recipeNextSlot;
    }

    public int getQueuePreviousSlot() {
        return queuePreviousSlot;
    }

    public int getQueueNextSlot() {
        return queueNextSlot;
    }
}
