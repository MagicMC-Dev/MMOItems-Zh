package net.Indyuce.mmoitems.stat.data;

import net.Indyuce.mmoitems.stat.data.type.Mergeable;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import org.apache.commons.lang.Validate;
import org.jetbrains.annotations.NotNull;

public class RestoreData implements StatData, Mergeable<RestoreData> {
    private double health, food, saturation;

    public RestoreData(double health, double food, double saturation) {
        this.health = health;
        this.food = food;
        this.saturation = saturation;
    }

    public double getHealth() {
        return health;
    }

    public double getFood() {
        return food;
    }

    public double getSaturation() {
        return saturation;
    }

    public void setHealth(double value) {
        health = value;
    }

    public void setFood(double value) {
        food = value;
    }

    public void setSaturation(double value) {
        saturation = value;
    }

    @Override
    public boolean isEmpty() {
        return food == 0 && health == 0 && saturation == 0;
    }

    @Override
    public void mergeWith(@NotNull RestoreData targetData) {
        health += targetData.health;
        food += targetData.food;
        saturation += targetData.saturation;
    }

    @Override
    @NotNull
    public RestoreData clone() {
        return new RestoreData(health, food, saturation);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof RestoreData)) {
            return false;
        }
        return ((RestoreData) obj).getFood() == getFood() && ((RestoreData) obj).getHealth() == getHealth() && ((RestoreData) obj).getSaturation() == getSaturation();
    }
}