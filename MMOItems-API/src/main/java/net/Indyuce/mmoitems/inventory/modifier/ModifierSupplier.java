package net.Indyuce.mmoitems.inventory.modifier;

import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.player.modifier.ModifierSource;
import io.lumine.mythic.lib.player.modifier.PlayerModifier;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public interface ModifierSupplier {

    @NotNull
    public EquipmentSlot getEquipmentSlot();

    @NotNull
    public ModifierSource getModifierSource();

    @NotNull
    public Collection<PlayerModifier> getModifierCache();
}
