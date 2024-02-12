package net.Indyuce.mmoitems.manager;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ConfigFile;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.manager.ConfigManager.DefaultFile;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.logging.Level;

public class TypeManager {
    private final Map<String, Type> map = new LinkedHashMap<>();

    /**
     * Reloads the type manager. It entirely empties the currently registered
     * item types, registers default item types again and reads item-types.yml
     */
    public void reload(boolean clearBefore) {
        if (clearBefore) map.clear();

        // Load default types
        for (Field field : Type.class.getFields())
            try {
                if (Modifier.isStatic(field.getModifiers()) && Modifier.isFinal(field.getModifiers()) && field.get(null) instanceof Type)
                    register((Type) field.get(null));
            } catch (Exception exception) {
                MMOItems.plugin.getLogger().log(Level.WARNING, "Couldn't register type called '" + field.getName() + "': " + exception.getMessage());
            }

        // Load custom types
        DefaultFile.ITEM_TYPES.checkFile();
        FileConfiguration config = new ConfigFile("item-types").getConfig();
        for (String id : config.getKeys(false))
            if (!map.containsKey(id))
                try {
                    register(new Type(this, config.getConfigurationSection(id)));
                } catch (IllegalArgumentException exception) {
                    MMOItems.plugin.getLogger().log(Level.WARNING, "Could not register type '" + id + "': " + exception.getMessage());
                }

        for (Iterator<Type> iterator = map.values().iterator(); iterator.hasNext(); ) {
            Type type = iterator.next();

            try {
                ConfigurationSection section = config.getConfigurationSection(type.getId());
                Validate.notNull(section, "Could not find config section for type '" + type.getId() + "'");
                type.load(section);
                if (clearBefore) type.getPostLoadAction().performAction();
            } catch (RuntimeException exception) {
                MMOItems.plugin.getLogger().log(Level.WARNING, "Could not register type '" + type.getId() + "': " + exception.getMessage());
                iterator.remove();
                continue;
            }

            /*
             * caches all the stats which the type can have to reduce future
             * both item generation (and GUI) calculations. probably the thing
             * which takes the most time when loading item types.
             */
            type.getAvailableStats().clear();
            MMOItems.plugin.getStats().getAll().stream().filter(stat -> stat.isCompatible(type)).forEach(stat -> type.getAvailableStats().add(stat));
        }
    }

    public void postload() {
        for (Type type : map.values())
            try {
                type.getPostLoadAction().performAction();
            } catch (RuntimeException exception) {
                MMOItems.plugin.getLogger().log(Level.WARNING, "An error occured while post-loading type '" + type.getId() + "': " + exception.getMessage());
            }
    }

    public void register(Type type) {
        map.put(type.getId(), type);
    }

    public void registerAll(Type... types) {
        for (Type type : types)
            register(type);
    }

    /**
     * @param id Internal ID of the type
     * @return The MMOItem Type if it found.
     */
    @Nullable
    public Type get(@Nullable String id) {
        if (id == null) {
            return null;
        }
        return map.get(id);
    }

    @NotNull
    public Type getOrThrow(@Nullable String id) {
        Validate.isTrue(map.containsKey(id), "Could not find item type with ID '" + id + "'");
        return map.get(id);
    }

    public boolean has(String id) {
        return map.containsKey(id);
    }

    public Collection<Type> getAll() {
        return map.values();
    }

    /**
     * @return The names of all loaded types.
     */
    public ArrayList<String> getAllTypeNames() {
        ArrayList<String> ret = new ArrayList<>();
        for (Type t : getAll()) {
            ret.add(t.getId());
        }
        return ret;
    }
}
