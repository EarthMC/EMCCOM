package net.earthmc.emccom.combat.listener;

import io.papermc.paper.event.player.PlayerItemCooldownEvent;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PlayerItemCooldownListener implements Listener {
    private final FileConfiguration config;

    public PlayerItemCooldownListener(FileConfiguration config) {
        this.config = config;
    }

    @EventHandler
    public void onPlayerItemCooldown(PlayerItemCooldownEvent event) {
        if (event.getType() != Material.ENDER_PEARL) return;

        event.setCooldown(config.getInt("ender_pearl_cooldown_ticks"));
    }
}
