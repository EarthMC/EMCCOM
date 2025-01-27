package net.earthmc.emccom.combat.listener;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.event.teleport.SuccessfulTownyTeleportEvent;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.WorldCoord;
import net.earthmc.emccom.manager.ResidentMetadataManager;
import net.earthmc.emccom.object.SpawnProtPref;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SpawnProtectionListener implements Listener {
    private final Map<UUID, Integer> playerChunkCountMap = new HashMap<>();

    private static final int CHUNK_DISTANCE = 8;

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onTeleport(SuccessfulTownyTeleportEvent event) {
        playerChunkCountMap.put(event.getResident().getUUID(), CHUNK_DISTANCE);
    }

    @EventHandler
    public void onRespawnEvent(PlayerRespawnEvent event) {
        playerChunkCountMap.put(event.getPlayer().getUniqueId(), CHUNK_DISTANCE);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        Location from = event.getFrom();
        final int fromChunkX = from.getBlockX() >> 4;
        final int fromChunkZ = from.getBlockZ() >> 4;

        Location to = event.getTo();
        final int toChunkX = to.getBlockX() >> 4;
        final int toChunkZ = to.getBlockZ() >> 4;

        if (!playerChunkCountMap.containsKey(playerId) || (fromChunkX == toChunkX && fromChunkZ == toChunkZ)) return;

        int remainingChunks = getRemainingChunks(playerId);
        if (remainingChunks <= 0) return;

        remainingChunks--;
        updateRemainingChunks(playerId, remainingChunks);

        Resident playerAsResident = TownyAPI.getInstance().getResident(player);
        SpawnProtPref spawnProtPrefOfResident = ResidentMetadataManager.getResidentSpawnProtPref(playerAsResident);

        if (spawnProtPrefOfResident == SpawnProtPref.HIDE) return;

        String message;
        if (remainingChunks > 0) {
            message = "§6[Towny] §cYou will be protected from losing your items in case of death for " + remainingChunks + " more chunks";
        } else {
            message = "§6[Towny] §cYou will now lose your items if you die! \nRun §a/spawnprotpref HIDE §cto disable chunk notification warnings";
        }
        TownyMessaging.sendMessage(playerAsResident, message);
    }

    @EventHandler
    public void onPlayerTeleportWithEnderPearl(PlayerTeleportEvent event) {
        if (!event.getCause().equals(PlayerTeleportEvent.TeleportCause.ENDER_PEARL)) return;

        UUID uuid = event.getPlayer().getUniqueId();

        Integer chunkCount = playerChunkCountMap.get(uuid);
        if (chunkCount == null || chunkCount == 0) return;

        Location from = event.getFrom();
        Location to = event.getTo();

        WorldCoord fromWorldCoord = WorldCoord.parseWorldCoord(from);
        WorldCoord toWorldCoord = WorldCoord.parseWorldCoord(to);
        if (fromWorldCoord.equals(toWorldCoord)) return; // Player didn't move chunks

        double distance = from.distance(to);
        int numChunksMoved = (int) Math.ceil(distance / 16);

        updateRemainingChunks(uuid, chunkCount - numChunksMoved);
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
        if (TownyAPI.getInstance().isWilderness(coord)) return;

        if (!playerChunkCountMap.containsKey(playerId)) return;

        int remainingChunks = getRemainingChunks(playerId);
        if (remainingChunks <= 0) return;

        event.setKeepInventory(true);
        event.setKeepLevel(true);
        event.getDrops().clear();
        event.setDroppedExp(0);

        playerChunkCountMap.remove(playerId);
    }
}

