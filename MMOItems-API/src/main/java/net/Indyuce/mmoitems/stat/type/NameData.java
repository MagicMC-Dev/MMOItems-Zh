package net.Indyuce.mmoitems.stat.type;

import io.lumine.mythic.lib.api.item.ItemTag;
import net.Indyuce.mmoitems.stat.data.StringData;
import net.Indyuce.mmoitems.stat.data.type.Mergeable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

/**
 * Data that stores what an item is originally named like and prefixes or whatever.
 */
public class NameData extends StringData implements Mergeable<NameData> {
    public NameData(@NotNull String str) {
        super(str);
    }

    public void readPrefixes(@Nullable ItemTag tag) {
        if (tag == null) { return; }

        for (String str : ItemTag.getStringListFromTag(tag)) {
            if (str == null) { continue; }

            addPrefix(str);
        }
    }

    @NotNull public String getMainName() { return getString(); }

    public boolean hasPrefixes() { return prefixes.size() > 0; }
    public boolean hasSuffixes() { return suffixes.size() > 0; }

    /**
     * @return The full, built name.
     */
    @NotNull
    public String bake() {
        StringBuilder sb = new StringBuilder();
        for (String prefix : getPrefixes()) { if (sb.length() > 0) { sb.append(" "); } sb.append(prefix); }
        if (sb.length() > 0) { sb.append(" "); } sb.append(getMainName());
        for (String suffix : getSuffixes()) { if (sb.length() > 0) { sb.append(" "); } sb.append(suffix); }

        // Built
        return sb.toString();
    }

    /**
     * @return The built prefix
     */
    @NotNull
    public String bakePrefix() {

        StringBuilder sb = new StringBuilder();
        for (String prefix : getPrefixes()) { if (sb.length() > 0) { sb.append(" "); } sb.append(prefix); }

        // Built
        return sb.toString();
    }

    /**
     * @return The built suffix
     */
    @NotNull
    public String bakeSuffix() {

        StringBuilder sb = new StringBuilder();
        for (String suffix : getSuffixes()) { if (sb.length() > 0) { sb.append(" "); } sb.append(suffix); }

        // Built
        return sb.toString();
    }

    @Override
    public String toString() {
        return bake();
    }

    @NotNull public ItemTag compressPrefixes(@NotNull String path) {

        return ItemTag.fromStringList(path, getPrefixes());
    }

    @NotNull public ItemTag compressSuffixes(@NotNull String path) {

        return ItemTag.fromStringList(path, getSuffixes());
    }

    public void readSuffixes(@Nullable ItemTag tag) {
        if (tag == null) { return; }

        for (String str : ItemTag.getStringListFromTag(tag)) {
            if (str == null) { continue; }

            addSuffix(str);
        }
    }

    @NotNull ArrayList<String> prefixes = new ArrayList<>();
    public void addPrefix(@NotNull String str) { prefixes.add(str); }
    public void clearPrefixes() { prefixes.clear(); }
    @NotNull public ArrayList<String> getPrefixes() { return prefixes; }

    @NotNull ArrayList<String> suffixes = new ArrayList<>();
    public void addSuffix(@NotNull String str) { suffixes.add(str); }
    public void clearSuffixes() { suffixes.clear(); }
    @NotNull public ArrayList<String> getSuffixes() { return suffixes; }

    @Override
    public void mergeWith(NameData data) {
        if (!data.getMainName().isEmpty()) setString(data.getMainName());
        for (String p : data.getPrefixes()) addPrefix(p);
        for (String p : data.getSuffixes()) addSuffix(p);
     /*   } else if (data instanceof StringData) {
            // Replace name if not empty
            if (data.toString().isEmpty()) { return; }
            setString(data.toString());
        }*/
    }

    @NotNull
    @Override
    public NameData clone() {

        NameData c = new NameData(getMainName());
        for (String p : getPrefixes()) { c.addPrefix(p);}
        for (String p : getSuffixes()) { c.addSuffix(p);}

        return c;
    }

    @Override
    public boolean isEmpty() {
        return super.isEmpty() && prefixes.isEmpty() && suffixes.isEmpty();
    }
}
