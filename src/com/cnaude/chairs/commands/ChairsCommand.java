package com.cnaude.chairs.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.cnaude.chairs.core.Chairs;

public class ChairsCommand implements CommandExecutor {

    private final Chairs plugin;

    public ChairsCommand(Chairs instance) {
        this.plugin = instance;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            return false;
        }
        if (args[0].equalsIgnoreCase("reload")) {
            if (sender.hasPermission("chairs.reload")) {
                plugin.loadConfig();
                if (plugin.isSitHealEnabled()) {
                    plugin.getChairEffects().restartHealing();
                } else {
                    plugin.getChairEffects().cancelHealing();
                }
                if (plugin.isSitPickupEnabled()) {
                    plugin.getChairEffects().restartPickup();
                } else {
                    plugin.getChairEffects().cancelPickup();
                }
                sender.sendMessage(plugin.getMsgReloaded());
            } else {
                sender.sendMessage(plugin.getMsgNoPerm());
            }
        }
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (args[0].equalsIgnoreCase("off")) {
                plugin.getSitDisabled().add(player.getUniqueId());
                player.sendMessage("Disabled sitting");
            } else if (args[0].equalsIgnoreCase("on")) {
                plugin.getSitDisabled().remove(player.getUniqueId());
                player.sendMessage("Enabled sitting");
            }
        }
        return true;
    }

}
