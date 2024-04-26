package net.earthmc.emccom.combat.listener;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.event.SpawnEvent;
import com.palmergames.bukkit.towny.event.teleport.CancelledTownyTeleportEvent;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.WorldCoord;
import net.earthmc.emccom.manager.ResidentMetadataManager;
import net.earthmc.emccom.object.SpawnProtPref;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SpawnProtectionListener implements Listener {
    private final Map<UUID, Integer> playerChunkCountMap = new HashMap<>();

    private static final int CHUNK_DISTANCE = 8;

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onSpawnEvent(SpawnEvent event) {
        playerChunkCountMap.put(event.getPlayer().getUniqueId(), CHUNK_DISTANCE);
    }

    @EventHandler
    public void onRespawnEvent(PlayerRespawnEvent event) {
        playerChunkCountMap.put(event.getPlayer().getUniqueId(), CHUNK_DISTANCE);
    }

    @EventHandler
    public void onSpawnEventCancelled(CancelledTownyTeleportEvent event) {
        playerChunkCountMap.remove(event.getResident().getUUID());
    }

    // 😭😭😭
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        Chunk fromChunk = event.getFrom().getChunk();
        Chunk toChunk = event.getTo().getChunk();

        if (playerChunkCountMap.containsKey(playerId) && !fromChunk.equals(toChunk)) {
            int remainingChunks = getRemainingChunks(playerId);
            if (remainingChunks > 0) {
                remainingChunks--;
                updateRemainingChunks(playerId, remainingChunks);

                Resident playerAsResident = TownyAPI.getInstance().getResident(player);
                SpawnProtPref SpawnProtPrefOfResident = ResidentMetadataManager.getResidentSpawnProtPref(playerAsResident);
                if (!(SpawnProtPrefOfResident == SpawnProtPref.HIDE)) {
                    Component message;
                    if (remainingChunks > 0) {
                        message = Component.text()
                                .append(Component.text("[Towny] ", NamedTextColor.GOLD))
                                .append(Component.text("You have " + remainingChunks + " chunks left before losing items on death!", NamedTextColor.RED))
                                .build();
                    } else {
                        message = Component.text()
                                .append(Component.text("[Towny] ", NamedTextColor.GOLD))
                                .append(Component.text("You will now lose your items if you die! Run ", NamedTextColor.RED))
                                .append(Component.text("/spawnprotpref HIDE", NamedTextColor.GREEN))
                                .append(Component.text(" to disable chunk notification warnings", NamedTextColor.RED))
                                .build();
                    }
                    player.sendMessage(message);
                }
            }
        }
    }

    private int getRemainingChunks(UUID playerId) {
        return playerChunkCountMap.getOrDefault(playerId, CHUNK_DISTANCE);
    }

    private void updateRemainingChunks(UUID playerId, int remainingChunks) {
        playerChunkCountMap.put(playerId, remainingChunks);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        final WorldCoord coord = WorldCoord.parseWorldCoord(event.getEntity().getLocation());
        if (TownyAPI.getInstance().isWilderness(coord))
            return;

        if (playerChunkCountMap.containsKey(playerId)) {
            int remainingChunks = getRemainingChunks(playerId);
            if (remainingChunks > 0) {
                event.setKeepInventory(true);
                event.setKeepLevel(true);
                event.getDrops().clear();
                event.setDroppedExp(0);

                playerChunkCountMap.remove(playerId);
            }
        }
    }
}

