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
                plugin.reload();
                sender.sendMessage(plugin.getConfigData().getMsgReloaded());
            } else {
                sender.sendMessage(plugin.getConfigData().getMsgNoPerm());
            }
        }
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (args[0].equalsIgnoreCase("off")) {
                plugin.getSitDisabled().add(player.getUniqueId());
                player.sendMessage(plugin.getConfigData().getMsgDisabled());
            } else if (args[0].equalsIgnoreCase("on")) {
                plugin.getSitDisabled().remove(player.getUniqueId());
                player.sendMessage(plugin.getConfigData().getMsgEnabled());
            }
        }
        return true;
    }

}
