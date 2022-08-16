package net.Indyuce.mmoitems.comp.mmoinventory;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.comp.mmoinventory.stat.AccessorySet;

public class MMOInventorySupport {
    public MMOInventorySupport() {
        MMOItems.plugin.getStats().register(new AccessorySet());
    }
}
