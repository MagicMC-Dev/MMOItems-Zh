package net.Indyuce.mmoitems.comp.mythicenchants;

import io.lumine.mythicenchants.enchants.MythicEnchant;
import io.lumine.mythicenchants.util.MythicEnchantsHelper;
import io.lumine.xikage.mythicmobs.adapters.AbstractPlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

public class MythicEnchantsSupport {
    public MythicEnchantsSupport()   {}

    public void reparseWeapon(AbstractPlayer player)   {
        MythicEnchantsHelper.reparseWeapon(player);
    }

    public boolean handleEnchant(ItemStack item, Enchantment enchant, int level)	{
        if(enchant instanceof MythicEnchant)	{
            ((MythicEnchant)enchant).applyToItem(item, level);
            return true;
        }
        return false;
    }
}
