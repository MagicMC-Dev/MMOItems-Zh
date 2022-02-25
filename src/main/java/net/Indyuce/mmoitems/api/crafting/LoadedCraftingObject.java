package net.Indyuce.mmoitems.api.crafting;

import io.lumine.mythic.lib.api.MMOLineConfig;

import java.util.function.Function;

/**
 * Used to handle both ingredients and conditions because they share the same
 * properties:
 * - a way to display them in the recipe item lore.
 * - some string identifier
 * - a function which takes as input a line config and returns a loaded ingredient/condition
 *
 * @param <C> Either Condition or Ingredient
 */
public class LoadedCraftingObject<C> {
    private final String id;
    private final Function<MMOLineConfig, C> function;

    // Configurable parameters
    private ConditionalDisplay display;

    public LoadedCraftingObject(String id, Function<MMOLineConfig, C> function, ConditionalDisplay display) {
        this.id = id;
        this.function = function;
        this.display = display;
    }

    public String getId() {
        return id;
    }

    public void setDisplay(ConditionalDisplay display) {
        this.display = display;
    }

    public boolean hasDisplay() {
        return display != null;
    }

    public ConditionalDisplay getDisplay() {
        return display;
    }

    public C load(MMOLineConfig config) {
        return function.apply(config);
    }
}