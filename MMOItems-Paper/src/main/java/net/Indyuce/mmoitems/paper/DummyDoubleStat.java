package net.Indyuce.mmoitems.paper;

import net.Indyuce.mmoitems.stat.annotation.VersionDependant;
import net.Indyuce.mmoitems.stat.type.DoubleStat;
import net.Indyuce.mmoitems.stat.type.GemStoneStat;
import org.bukkit.Material;

@VersionDependant(version = {1, 21, 4})
public class DummyDoubleStat extends DoubleStat implements GemStoneStat {
    public DummyDoubleStat() {
        super("CONSUME_SECONDS", Material.CLOCK, "", new String[0], new String[0]);

        disable();
    }
}
