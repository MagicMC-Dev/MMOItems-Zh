package net.Indyuce.mmoitems.util;

import io.lumine.mythic.lib.element.Element;

public enum ElementStatType {
    DAMAGE("Flat Damage"),
    DAMAGE_PERCENT("额外伤害 (%)"),
    WEAKNESS("虚弱  (% ) "),
    DEFENSE("防御"),
    DEFENSE_PERCENT("额外防御  (% ) ");

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
