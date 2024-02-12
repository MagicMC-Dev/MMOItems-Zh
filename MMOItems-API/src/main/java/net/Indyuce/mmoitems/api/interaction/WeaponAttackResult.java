package net.Indyuce.mmoitems.api.interaction;

public enum WeaponAttackResult {

    /**
     * Attack was performed successfully
     */
    SUCCESS,

    /**
     * Attakc was not performed due to missing resource (mana or stamina)
     * or ongoing cooldown.
     */
    WEAPON_COSTS,

    /**
     * Attack was canceled because the item is broken
     */
    DURABILITY,

    /**
     * Attack was canceled due to a precondition of the executed skill
     */
    SKILL_SPECIFIC,

    /**
     * Attack was canceled due to an event
     */
    BUKKIT_EVENT,

    /**
     * No attack effect was detected on the item.
     */
    NO_ATTACK,
}
