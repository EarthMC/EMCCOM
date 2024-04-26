package net.earthmc.emccom.combat.listener;
import com.google.common.collect.ImmutableSet;
import com.palmergames.bukkit.towny.object.CommandList;
import net.earthmc.emccom.combat.CombatHandler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class CommandListener implements Listener {

    private static final CommandList BLACKLISTED_COMMANDS = new CommandList(ImmutableSet.of(
            "t spawn", "n spawn", "warp", "trade", "res spawn", "home", "tradesystem:trade","town spawn","nation spawn","resident spawn","homes","towny:nation spawn","towny:town spawn","towny:resident spawn","player spawn","towny:player spawn","towny:n spawn","towny:nat spawn","towny:tw spawn","towny:res spawn","towny:t spawn","suicide"));

    @EventHandler
    public void onPreProcessCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();

        // Check if the player is in combat and doesn't have bypass permission
        if (CombatHandler.isTagged(player) && !player.hasPermission("emccom.combattag.bypass")) {

            // Check if the command is blacklisted
            if (BLACKLISTED_COMMANDS.containsCommand(event.getMessage())) {
                event.setCancelled(true);
                player.sendMessage(Component.text("You cannot use that command while in combat!", NamedTextColor.RED));
            }
        }
    }
}
