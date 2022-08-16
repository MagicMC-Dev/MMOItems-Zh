package net.Indyuce.mmoitems.stat.data.random;

import net.Indyuce.mmoitems.api.item.build.MMOItemBuilder;
import net.Indyuce.mmoitems.stat.data.type.StatData;

/**
 * RandomStatDatas are basically the bricks of the generation templates. They
 * are the first instances called when loading gen templates from config files.
 *
 * @author cympe
 */
public interface RandomStatData<S extends StatData> {

    /**
     * @param builder The builder of the random item being generated
     * @return A random stat data instance which will then be merged onto the
     *         base item template
     */
    S randomize(MMOItemBuilder builder);
}
