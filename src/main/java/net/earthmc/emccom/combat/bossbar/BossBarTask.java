package net.earthmc.emccom.combat.bossbar;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import net.earthmc.emccom.combat.CombatHandler;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class BossBarTask implements Consumer<ScheduledTask> {

    private static final Map<UUID, BossBar> bossBarMap = new ConcurrentHashMap<>();

    @Override
    public void accept(ScheduledTask task) {
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online == null) {
                continue;
            }

            if (CombatHandler.isTagged(online)) {
                BossBar bossBar = bossBarMap.computeIfAbsent(online.getUniqueId(), uuid -> {
                    BossBar newBar = BossBar.bossBar(Component.empty(), 0F, BossBar.Color.RED, BossBar.Overlay.PROGRESS);
                    update(online, newBar);
                    return newBar;
                });

                update(online, bossBar);
            } else {
                remove(online);
            }
        }
    }

    public static void update(Player player, BossBar bossBar) {
        long remaining = CombatHandler.getRemaining(player);
        if (remaining < 0)
            return;

        bossBar.name(Component.text("Combat Tag", NamedTextColor.RED, TextDecoration.BOLD).append(Component.text(": ", NamedTextColor.GRAY)).append(Component.text((remaining / 1000) + "s", NamedTextColor.RED)));
        bossBar.progress((float) remaining / CombatHandler.TAG_TIME);
    }

    public static void remove(Player player) {
        if (player == null) {
            return;
        }

        BossBar bossBar = bossBarMap.remove(player.getUniqueId());
        if (bossBar != null) {
            player.hideBossBar(bossBar);
        }
    }
}
