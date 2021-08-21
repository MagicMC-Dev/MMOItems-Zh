package net.Indyuce.mmoitems.comp.denizen;

import com.denizenscript.depenizen.bukkit.Depenizen;
import net.Indyuce.mmoitems.MMOItems;
import org.bukkit.Bukkit;

public class DenizenHook {

    /**
     * Putting this here so that the Depenizen import does not
     * appear in the main MMOItems class which would cause an issue
     * for all servers without Denizen
     */
    public DenizenHook() {
        Depenizen depenizen = (Depenizen) Bukkit.getPluginManager().getPlugin("Depenizen");
        depenizen.registerBridge(MMOItems.plugin.getName(), () -> new DenizenBridge());
    }
}
