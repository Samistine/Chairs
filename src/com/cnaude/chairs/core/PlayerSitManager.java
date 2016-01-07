package com.cnaude.chairs.core;

import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.cnaude.chairs.api.PlayerChairSitEvent;
import com.cnaude.chairs.api.PlayerChairUnsitEvent;
import java.util.Set;

public class PlayerSitManager {

    private Chairs plugin;

    public PlayerSitManager(Chairs plugin) {
        this.plugin = plugin;
    }

    private HashMap<Player, SitData> sit = new HashMap<>();
    private HashMap<Block, Player> sitblock = new HashMap<>();

    public boolean isSitting(Player player) {
        return sit.containsKey(player) && sit.get(player).sitting;
    }

    public Set<Player> getSittingPlayers() {
        return sit.keySet();
    }

    public boolean isBlockOccupied(Block block) {
        return sitblock.containsKey(block);
    }

    public Player getPlayerOnChair(Block chair) {
        return sitblock.get(chair);
    }

    public boolean sitPlayer(final Player player, Block blocktooccupy, Location sitlocation) {
        PlayerChairSitEvent playersitevent = new PlayerChairSitEvent(player, sitlocation.clone());
        plugin.getServer().getPluginManager().callEvent(playersitevent);
        if (playersitevent.isCancelled()) {
            return false;
        }
        sitlocation = playersitevent.getSitLocation().clone();
        if (plugin.getConfigData().isNotifyplayer()) {
            player.sendMessage(plugin.getConfigData().getMsgSitting());
        }
        SitData sitdata = new SitData();
        Entity arrow = plugin.getNMSAccess().spawnArrow(sitlocation.getBlock().getLocation().add(0.5, 0, 0.5));
        sitdata.arrow = arrow;
        sitdata.block = blocktooccupy;
        sitdata.teleportloc = player.getLocation();
        int task = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(
                plugin,
                new Runnable() {
                    @Override
                    public void run() {
                        reSitPlayer(player);
                    }
                },
                1000, 1000
        );
        sitdata.resittask = task;
        player.teleport(sitlocation);
        arrow.setPassenger(player);
        sit.put(player, sitdata);
        sitblock.put(blocktooccupy, player);
        sitdata.sitting = true;
        return true;
    }

    public void reSitPlayer(final Player player) {
        SitData sitdata = sit.get(player);
        sitdata.sitting = false;
        Entity prevarrow = sit.get(player).arrow;
        Entity arrow = plugin.getNMSAccess().spawnArrow(prevarrow.getLocation());
        arrow.setPassenger(player);
        sitdata.arrow = arrow;
        prevarrow.remove();
        sitdata.sitting = true;
    }

    public boolean unsitPlayer(Player player) {
        return unsitPlayer(player, true);
    }

    public void unsitPlayerForce(Player player) {
        unsitPlayer(player, false);
    }

    private boolean unsitPlayer(final Player player, boolean canCancel) {
        SitData sitdata = sit.get(player);
        final PlayerChairUnsitEvent playerunsitevent = new PlayerChairUnsitEvent(player, sitdata.teleportloc.clone(), canCancel);
        plugin.getServer().getPluginManager().callEvent(playerunsitevent);
        if (playerunsitevent.isCancelled() && playerunsitevent.canBeCancelled()) {
            return false;
        }
        sitdata.sitting = false;
        player.leaveVehicle();
        sitdata.arrow.remove();
        player.teleport(playerunsitevent.getTeleportLocation().clone());
        player.setSneaking(false);
        sitblock.remove(sitdata.block);
        plugin.getServer().getScheduler().cancelTask(sitdata.resittask);
        sit.remove(player);
        if (plugin.getConfigData().isNotifyplayer()) {
            player.sendMessage(plugin.getConfigData().getMsgStanding());
        }
        return true;
    }

    private class SitData {

        private boolean sitting;
        private Entity arrow;
        private Location teleportloc;
        private int resittask;
        private Block block;

    }

}
