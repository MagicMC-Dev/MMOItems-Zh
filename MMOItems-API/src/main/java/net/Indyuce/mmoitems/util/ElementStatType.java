package net.Indyuce.mmoitems.util;

import io.lumine.mythic.lib.element.Element;

public enum ElementStatType {
    DAMAGE("Flat Damage"),
    DAMAGE_PERCENT("Extra Damage (%)"),
    WEAKNESS("Weakness (%)"),
    DEFENSE("Defense"),
    DEFENSE_PERCENT("Extra Defense (%)");

    private final String name;

    ElementStatType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String lowerCaseName() {
        return name().toLowerCase().replace("_", "-");
    }

    public String getConcatenatedTagPath(Element element) {
        return element.getId() + "_" + name();
    }

    public String getConcatenatedConfigPath(Element element) {
        return element.getId().toLowerCase().replace("_", "-") + "." + name().toLowerCase().replace("_", "-");
    }
}
