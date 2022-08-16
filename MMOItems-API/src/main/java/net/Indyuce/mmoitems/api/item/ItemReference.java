package net.Indyuce.mmoitems.api.item;

import io.lumine.mythic.lib.player.cooldown.CooldownObject;
import net.Indyuce.mmoitems.api.Type;

/**
 * Referenced objects are either item templates or MMOItems. They contain the
 * reference for an item template and can be used as inputs in many methods to
 * find some data
 *
 * @author indyuce
 */
public interface ItemReference extends CooldownObject {

    /**
     * MMOItem templates have to identifiers: <b>TYPE</b> and ID
     * <p>
     * This method returns their TYPE.
     * <p>
     * Example: <b>GREATSWORD</b> STEEL_CLAYMORE
     */
    Type getType();

    /**
     * MMOItem templates have to identifiers: TYPE and <b>ID</b>
     * <p>
     * This method returns their TYPE.
     * <p>
     * Example: GREATSWORD <b>STEEL_CLAYMORE</b>
     */
    String getId();

    @Override
    default String getCooldownPath() {
        return "mmoitem_" + getId().toLowerCase();
    }
}
