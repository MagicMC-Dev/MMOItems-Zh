package net.Indyuce.mmoitems.stat.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.lumine.mythic.lib.api.util.ui.SilentNumbers;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.item.build.MMOItemBuilder;
import net.Indyuce.mmoitems.stat.GemSockets;
import net.Indyuce.mmoitems.stat.data.random.RandomStatData;
import net.Indyuce.mmoitems.stat.data.type.Mergeable;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import org.apache.commons.lang.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * A class containing all the information about gemstones on
 * an MMOItem. This contains the information of all the item modifiers
 * applied by a gem stone as well as the empty gem sockets.
 * <p>
 * When used as a {@link RandomStatData}, the 'gems' set is useless
 * because items do not come with gems applied to it when generated.
 */
public class GemSocketsData implements StatData, Mergeable<GemSocketsData>, RandomStatData<GemSocketsData> {
    @NotNull
    private final Set<GemstoneData> gems = new HashSet<>();
    @NotNull
    private final List<String> emptySlots;

    public GemSocketsData(@NotNull List<String> emptySlots) {
        this.emptySlots = emptySlots;
    }

    public GemSocketsData(@NotNull JsonArray emptySlots) {
        this.emptySlots = new ArrayList<>();

        emptySlots.forEach(el -> this.emptySlots.add(el.getAsString()));
    }

    /**
     * Attempts to find a slot of the same color of this gem within the item.
     * <p></p>
     * To know the color of the socket pass the same argument to {@link #getEmptySocket(String)}
     * which checks in the same order as this method for the first success.
     */
    public boolean canReceive(@NotNull String gem) {
        return getEmptySocket(gem) != null;
    }

    /**
     * Get the first empty gem socket that matches this color.
     *
     * @return <code>null</code> if none matched.
     */
    @Nullable
    public String getEmptySocket(@NotNull String gem) {
        for (String slot : emptySlots)
            if (gem.equals("") || slot.equals(getUncoloredGemSlot()) || gem.equals(slot))
                return slot;
        return null;
    }

    @NotNull
    public static String getUncoloredGemSlot() {
        String s = MMOItems.plugin.getConfig().getString("gem-sockets.uncolored");
        return s == null ? "Uncolored" : s;
    }

    public void add(GemstoneData gem) {
        gems.add(gem);
    }

    public void apply(String gem, GemstoneData gemstone) {
        emptySlots.remove(getEmptySocket(gem));
        gems.add(gemstone);
    }

    public void addEmptySlot(@NotNull String slot) {
        emptySlots.add(slot);
    }

    @NotNull
    public List<String> getEmptySlots() {
        return emptySlots;
    }

    @NotNull
    public Set<GemstoneData> getGemstones() {
        return gems;
    }

    /**
     * Removes such gem from this GemSocketsData, if it exists and
     * registers again an empty gem socket if required
     *
     * @param gemId  The unique ID of the gem to remove
     * @param socket The socket color to replace the gem with, <code>null</code> for no socket.
     * @return Whether a gem was removed from the data.
     */
    public boolean removeGem(@NotNull UUID gemId, @Nullable String socket) {
        for (GemstoneData data : getGemstones())
            if (data.getHistoricUUID().equals(gemId)) {
                if (socket != null)
                    addEmptySlot(socket);
                gems.remove(data);
                return true;
            }

        return false;
    }

    public JsonObject toJson() {
        JsonObject object = new JsonObject();

        JsonArray empty = new JsonArray();
        getEmptySlots().forEach(empty::add);
        object.add("EmptySlots", empty);

        JsonArray array = new JsonArray();
        gems.forEach(gem -> array.add(gem.toJson()));
        object.add("Gemstones", array);

        return object;
    }

    @Override
    public void merge(GemSocketsData data) {

        // Combine both actual gems, and empty slots
        emptySlots.addAll(data.emptySlots);
        gems.addAll(data.gems);
    }

    @Override
    public GemSocketsData cloneData() {

        // Clone empty slots
        GemSocketsData ret = new GemSocketsData(new ArrayList<>(emptySlots));

        // Clone gems
        for (GemstoneData gem : getGemstones())
            ret.add(gem.cloneGem());

        return ret;
    }

    @Override
    public boolean isEmpty() {
        return gems.isEmpty() && emptySlots.isEmpty();
    }

    @Override
    public GemSocketsData randomize(MMOItemBuilder builder) {
        return new GemSocketsData(new ArrayList<>(emptySlots));
    }

    @Override
    public String toString() {
        return "Empty:\u00a7b " + getEmptySlots().size() + "\u00a77, Gems:\u00a7b " + getGemstones().size();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof GemSocketsData)) {
            return false;
        }
        if (((GemSocketsData) obj).getEmptySlots().size() != getEmptySlots().size()) {
            return false;
        }
        if (((GemSocketsData) obj).getGemstones().size() != getGemstones().size()) {
            return false;
        }
        if (!SilentNumbers.hasAll(((GemSocketsData) obj).getEmptySlots(), getEmptySlots())) {
            return false;
        }

        for (GemstoneData objGem : ((GemSocketsData) obj).getGemstones()) {

            if (objGem == null) {
                continue;
            }

            // Validate with ours
            boolean unmatched = true;
            for (GemstoneData thisGem : getGemstones()) {

                // Test match
                if (objGem.equals(thisGem)) {
                    unmatched = false;
                    break;
                }
            }
            if (unmatched) {
                return false;
            }
        }

        // All equal
        return true;
    }
}
