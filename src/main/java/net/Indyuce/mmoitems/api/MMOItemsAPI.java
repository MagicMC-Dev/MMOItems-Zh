package net.Indyuce.mmoitems.api;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.ability.Ability;
import org.bukkit.plugin.java.JavaPlugin;

public class MMOItemsAPI {
    private final JavaPlugin plugin;

    /**
     * @param plugin Plugin
     */
    public MMOItemsAPI(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Registers an ability in MMOItems. This must be called before MMOItems enables,
     * therefore either using a loadbefore of MMOItems and while the plugin enables,
     * or using a dependency and usign #onLoad().
     * <p>
     * This method does NOT register listeners.
     * <p>
     * Throws an IAE if anything goes wrong.
     *
     * @param ability Ability to register
     */
    public void registerAbility(Ability ability) {
        MMOItems.plugin.getAbilities().registerAbility(ability);
    }
}
