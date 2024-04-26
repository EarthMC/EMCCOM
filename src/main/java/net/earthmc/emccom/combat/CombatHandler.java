package net.earthmc.emccom.combat;

import com.nametagedit.plugin.NametagEdit;
import com.nametagedit.plugin.api.data.Nametag;
import net.earthmc.emccom.EMCCOM;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent.Reason;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class CombatHandler {

    public static final long TAG_TIME = 30 * 1000;
    private static final Map<UUID, Long> combatTags = new ConcurrentHashMap<>();
    private static final boolean usingNametagEdit = Bukkit.getServer().getPluginManager().isPluginEnabled("NametagEdit");

    public static void startTask(EMCCOM plugin) {
        plugin.getServer().getAsyncScheduler().runAtFixedRate(plugin, task -> {
            Iterator<Entry<UUID, Long>> iterator = combatTags.entrySet().iterator();

            while (iterator.hasNext()) {
                Entry<UUID, Long> entry = iterator.next();

                if (entry.getValue() > System.currentTimeMillis())
                    continue;

                iterator.remove();

                UUID uuid = entry.getKey();
                Player player = Bukkit.getPlayer(uuid);
                if (player == null || !player.isOnline())
                    continue;

                player.sendMessage(Component.text("You are no longer in combat.", NamedTextColor.GREEN));

                // Remove the player from the combat tagged team
                removePlayerFromCombatTeam(player);
            }
        }, 500L, 500L, TimeUnit.MILLISECONDS);
    }

    public static void applyTag(Player player) {
        if (!isTagged(player)) {
            player.closeInventory(Reason.PLUGIN);
            player.sendMessage(Component.text("You have been combat tagged for " + (TAG_TIME / 1000) + " seconds! Do not log out or you will get killed instantly.", NamedTextColor.RED));

            addPlayerToCombatTeam(player);
        }

        combatTags.put(player.getUniqueId(), System.currentTimeMillis() + TAG_TIME);
    }

    public static void removeTag(Player player) {
        combatTags.remove(player.getUniqueId());

        removePlayerFromCombatTeam(player);
    }

    public static boolean isTagged(Player player) {
        Long untagTime = combatTags.get(player.getUniqueId());

        return untagTime != null && untagTime > System.currentTimeMillis();
    }

    public static long getRemaining(Player player) {
        if (!combatTags.containsKey(player.getUniqueId()))
            return -1;

        return combatTags.get(player.getUniqueId()) - System.currentTimeMillis();
    }

    private static void addPlayerToCombatTeam(Player player) {
        if (!usingNametagEdit)
            return;

        Nametag tag = NametagEdit.getApi().getNametag(player);
        NametagEdit.getApi().setNametag(player, tag.getPrefix(), tag.getSuffix() + "§c ⚔"); // chat color :(
    }

    private static void removePlayerFromCombatTeam(Player player) {
        if (!usingNametagEdit)
            return;

        NametagEdit.getApi().applyTagToPlayer(player, false);
    }
}
