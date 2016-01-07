package com.cnaude.chairs.sitaddons;

import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerLevelChangeEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;

import com.cnaude.chairs.core.Chairs;
import org.bukkit.scheduler.BukkitRunnable;

public class ChairEffects {

    private Chairs plugin;
    private int healTaskID = -1;
    private int pickupTaskID = -1;

    public ChairEffects(Chairs plugin) {
        this.plugin = plugin;
    }

    public void startHealing() {
        healEffectsTask();
    }

    public void cancelHealing() {
        if (healTaskID != -1) {
            plugin.getServer().getScheduler().cancelTask(healTaskID);
            healTaskID = -1;
        }
    }

    public void restartHealing() {
        cancelHealing();
        startHealing();
    }

    private void healEffectsTask() {
        healTaskID = new HealEffectsTask().runTaskTimer(plugin, plugin.getSitHealInterval(), plugin.getSitHealInterval()).getTaskId();
    }

    private double getPlayerHealth(Player player) {
        return player.getHealth();
    }

    private double getMaxPlayerHealth(Player player) {
        return player.getMaxHealth();
    }

    public void startPickup() {
        pickupEffectsTask();
    }

    public void cancelPickup() {
        if (pickupTaskID != -1) {
            plugin.getServer().getScheduler().cancelTask(pickupTaskID);
        }
        pickupTaskID = -1;
    }

    public void restartPickup() {
        cancelPickup();
        startPickup();
    }

    private void pickupEffectsTask() {
        pickupTaskID = new PickupEffectsTask().runTaskTimer(plugin, 0, 1).getTaskId();
    }

    private class HealEffectsTask extends BukkitRunnable {

        @Override
        public void run() {
            for (Player p : plugin.getPlayerSitData().getSittingPlayers()) {
                if (plugin.getPlayerSitData().isSitting(p)) {
                    if (p.hasPermission("chairs.sit.health")) {
                        double pHealthPcnt = (getPlayerHealth(p)) / getMaxPlayerHealth(p) * 100d;
                        if ((pHealthPcnt < plugin.getSitMaxHealth()) && (getPlayerHealth(p) < getMaxPlayerHealth(p))) {
                            double newHealth = plugin.getSitHealthPerInterval() + getPlayerHealth(p);
                            if (newHealth > getMaxPlayerHealth(p)) {
                                newHealth = getMaxPlayerHealth(p);
                            }
                            p.setHealth(newHealth);
                        }
                    }
                }
            }
        }
    }

    private class PickupEffectsTask extends BukkitRunnable {

        @Override
        public void run() {
            for (Player p : plugin.getServer().getOnlinePlayers()) {
                if (plugin.getPlayerSitData().isSitting(p)) {
                    for (Entity entity : p.getNearbyEntities(1, 2, 1)) {
                        if (entity instanceof Item) {
                            Item item = (Item) entity;
                            if (item.getPickupDelay() == 0) {
                                if (p.getInventory().firstEmpty() != -1) {
                                    PlayerPickupItemEvent pickupevent = new PlayerPickupItemEvent(p, item, 0);
                                    plugin.getServer().getPluginManager().callEvent(pickupevent);
                                    if (!pickupevent.isCancelled()) {
                                        p.getInventory().addItem(item.getItemStack());
                                        entity.remove();
                                    }
                                }
                            }
                        } else if (entity instanceof ExperienceOrb) {
                            ExperienceOrb eorb = (ExperienceOrb) entity;
                            int exptoadd = eorb.getExperience();
                            while (exptoadd > 0) {
                                int localexptoadd = 0;
                                if (p.getExpToLevel() < exptoadd) {
                                    localexptoadd = p.getExpToLevel();
                                    PlayerExpChangeEvent expchangeevent = new PlayerExpChangeEvent(p, localexptoadd);
                                    plugin.getServer().getPluginManager().callEvent(expchangeevent);
                                    p.giveExp(expchangeevent.getAmount());
                                    if (p.getExpToLevel() <= 0) {
                                        PlayerLevelChangeEvent levelchangeevent = new PlayerLevelChangeEvent(p, p.getLevel(), p.getLevel() + 1);
                                        plugin.getServer().getPluginManager().callEvent(levelchangeevent);
                                        p.setExp(0);
                                        p.giveExpLevels(1);
                                    }
                                } else {
                                    localexptoadd = exptoadd;
                                    PlayerExpChangeEvent expchangeevent = new PlayerExpChangeEvent(p, localexptoadd);
                                    plugin.getServer().getPluginManager().callEvent(expchangeevent);
                                    p.giveExp(expchangeevent.getAmount());
                                }
                                exptoadd -= localexptoadd;
                            }
                            entity.remove();
                        }
                    }
                }
            }
        }
    }
}
