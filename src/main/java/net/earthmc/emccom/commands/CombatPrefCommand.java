package net.earthmc.emccom.commands;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Resident;
import net.earthmc.emccom.manager.ResidentMetadataManager;
import net.earthmc.emccom.object.CombatPref;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CombatPrefCommand implements TabExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only players can use this command", NamedTextColor.RED));
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(Component.text("No arguments provided", NamedTextColor.RED));
            return true;
        }

        Resident resident = TownyAPI.getInstance().getResident(player);
        if (resident == null) {
            player.sendMessage(Component.text("Command failed as your player has no associated Towny resident", NamedTextColor.RED));
            return true;
        }

        CombatPref combatPref;
        switch (args[0]) {
            case "safe" -> combatPref = CombatPref.SAFE;
            case "unsafe" -> combatPref = CombatPref.UNSAFE;
            default -> {
                player.sendMessage(Component.text("Invalid argument", NamedTextColor.RED));
                return true;
            }
        }

        ResidentMetadataManager.setResidentCombatPref(resident, combatPref);
        if (args[0].equals("safe")){
            player.sendMessage(Component.text("Successfully changed your combat preference to SAFE.\n" +
                    "You will no longer be able to hit tagged players within claims to initiate combat.", NamedTextColor.GREEN));
        }
        if (args[0].equals("unsafe")){
            player.sendMessage(Component.text("Successfully changed your combat preference to UNSAFE.\n" +
                    "You will now be able to hit tagged players within claims to initiate combat.", NamedTextColor.GREEN));
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        List<String> availableArguments = List.of("safe", "unsafe");

        if (args.length == 0) {
            return availableArguments; // If there are no arguments, provide all available options
        } else if (args.length == 1) {
            // Provide completions for the first argument
            if (args[0].isEmpty()) {
                return availableArguments;
            } else {
                return availableArguments.stream()
                        .filter(string -> string.startsWith(args[0].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }

        return Collections.emptyList(); // Return an empty list if the number of arguments is greater than 1
    }
}

