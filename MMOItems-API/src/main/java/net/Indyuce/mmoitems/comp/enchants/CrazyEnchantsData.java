package net.Indyuce.mmoitems.comp.enchants;

import me.badbones69.crazyenchantments.api.objects.CEnchantment;
import net.Indyuce.mmoitems.stat.data.type.StatData;

import java.util.Map;

public class CrazyEnchantsData implements StatData {
    private final Map<CEnchantment, Integer> enchants;

    public CrazyEnchantsData(Map<CEnchantment, Integer> enchants) {
        this.enchants = enchants;
    }

    public Map<CEnchantment, Integer> getEnchants() {
        return enchants;
    }

    @Override
    public boolean isEmpty() {
        return enchants.isEmpty();
    }
}
