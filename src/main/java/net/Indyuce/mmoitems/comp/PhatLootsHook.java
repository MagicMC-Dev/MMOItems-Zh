package net.Indyuce.mmoitems.comp;

import com.codisimus.plugins.phatloots.events.LootEvent;
import com.codisimus.plugins.phatloots.events.MobDropLootEvent;
import com.codisimus.plugins.phatloots.events.PhatLootsEvent;
import com.codisimus.plugins.phatloots.events.PlayerLootEvent;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.api.util.ui.SilentNumbers;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.Indyuce.mmoitems.api.player.RPGPlayer;
import net.Indyuce.mmoitems.api.util.MMOItemReforger;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

/**
 * Its absolute <b><i><u>pain</u></i></b> that PhatLoots keeps giving outdated
 * MMOItems that don't stack, so that now MMOItems shall support fixing those items
 * as they are generated.
 */
public class PhatLootsHook implements Listener {


    @EventHandler
    public void OnLootBeLooted(MobDropLootEvent event) { handle(event);}

    @EventHandler
    public void OnLootBeLooted(PlayerLootEvent event) { handle(event);}

    public void handle(LootEvent event) {

        // Fix stacks
        for (ItemStack itm : event.getItemList()) {
            //UPD//MMOItems.log("\u00a79*\u00a77 Looted " + SilentNumbers.getItemName(itm));

            // Skip
            if (SilentNumbers.isAir(itm)) {
                //UPD//MMOItems.log("\u00a71*\u00a78 Air");
                continue; }

            // NBT-Ize
            NBTItem loot = NBTItem.get(itm);

            // Not MMOItems I sleep
            if (!loot.hasType()) {
                //UPD//MMOItems.log("\u00a71*\u00a78 Vanilla");
                continue; }

            // The MMOItem does not exist anymore (must have been deleted after being added
            if (MMOItems.plugin.getTemplates().getTemplate(MMOItems.plugin.getType(loot), MMOItems.plugin.getID(loot)) == null) { continue; }

            // All right update the bitch
            MMOItemReforger mod = new MMOItemReforger(loot);
            mod.update((RPGPlayer) null, MMOItems.plugin.getLanguage().phatLootsOptions);

            // Changes?
            if (mod.hasChanges()) {

                // LEts go
                ItemStack gen = mod.toStack();
                gen.setAmount(itm.getAmount());
                ItemMeta genMeta = gen.getItemMeta();
                //UPD//MMOItems.log("\u00a73+*\u00a77 Output " + SilentNumbers.getItemName(gen));

                // Completely Replace
                itm.setType(gen.getType());
                itm.setItemMeta(genMeta);
                itm.setData(gen.getData()); }

        } }
}
