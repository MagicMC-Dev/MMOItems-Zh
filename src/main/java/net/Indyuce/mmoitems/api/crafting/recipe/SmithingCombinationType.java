package net.Indyuce.mmoitems.api.crafting.recipe;

public enum SmithingCombinationType {

    /**
     * Adds the upgrade levels of both items
     */
    ADDITIVE,

    /**
     * Chooses the upgrade level of the item
     * with the greatest upgrade level
     */
    MAXIMUM,

    /**
     * Takes the average of the upgrade levels
     */
    EVEN,

    /**
     * Chooses the least upgrade level
     */
    MINIMUM,

    /**
     * Upgrade levels are lost
     */
    NONE
}
