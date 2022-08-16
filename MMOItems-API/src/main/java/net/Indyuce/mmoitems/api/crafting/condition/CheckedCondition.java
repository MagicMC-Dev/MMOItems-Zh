package net.Indyuce.mmoitems.api.crafting.condition;

public class CheckedCondition {
    private final Condition condition;
    private final boolean met;

    /**
     * Instanciated everytime a condition needs to be evaluated for a player
     * when evaluating a CheckedRecipe (when a player is opening a crafting
     * station)
     *
     * @param condition Condition being evaluated
     * @param met       If the condition is met or not
     */
    public CheckedCondition(Condition condition, boolean met) {
        this.condition = condition;
        this.met = met;
    }

    public boolean isMet() {
        return met;
    }

    public Condition getCondition() {
        return condition;
    }

    public String format() {
        return condition.formatDisplay(condition.getDisplay().format(met));
    }
}