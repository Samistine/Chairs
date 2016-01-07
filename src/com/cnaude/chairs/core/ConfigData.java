/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cnaude.chairs.core;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

/**
 * Handles all configuration aspects of this plugin.
 *
 * @author Samuel
 */
public class ConfigData {

    private List<ChairBlock> allowedBlocks;
    private List<Material> validSigns;
    private boolean autoRotate, signCheck, notifyplayer;
    private boolean ignoreIfBlockInHand;
    private double distance;
    private int maxChairWidth;
    private boolean sitHealEnabled;
    private int sitMaxHealth;
    private int sitHealthPerInterval;
    private int sitHealInterval;
    private boolean sitPickupEnabled;
    private boolean sitDisableAllCommands = false;
    private HashSet<String> sitDisabledCommands = new HashSet<>();
    private String msgSitting, msgStanding, msgOccupied, msgNoPerm, msgReloaded, msgDisabled, msgEnabled, msgCommandRestricted;

    private final Plugin plugin;
    private final FileConfiguration config;
    private final Logger logger;

    protected ConfigData(Plugin plugin) {
        plugin.getConfig().options().copyDefaults(true);
        plugin.saveConfig();
        this.plugin = plugin;
        this.config = plugin.getConfig();
        this.logger = plugin.getLogger();
        load();
    }

    public void reload() {
        plugin.reloadConfig();
        load();
    }

    private void load() {
        autoRotate = config.getBoolean("auto-rotate");
        signCheck = config.getBoolean("sign-check");
        distance = config.getDouble("distance");
        maxChairWidth = config.getInt("max-chair-width");
        notifyplayer = config.getBoolean("notify-player");
        ignoreIfBlockInHand = config.getBoolean("ignore-if-item-in-hand");

        sitHealEnabled = config.getBoolean("sit-effects.healing.enabled", false);
        sitHealInterval = config.getInt("sit-effects.healing.interval", 20);
        sitMaxHealth = config.getInt("sit-effects.healing.max-percent", 100);
        sitHealthPerInterval = config.getInt("sit-effects.healing.amount", 1);

        sitPickupEnabled = config.getBoolean("sit-effects.itempickup.enabled", false);

        sitDisableAllCommands = config.getBoolean("sit-restrictions.commands.all");
        sitDisabledCommands = new HashSet<>(config.getStringList("sit-restrictions.commands.list"));

        msgSitting = ChatColor.translateAlternateColorCodes('&', config.getString("messages.sitting"));
        msgStanding = ChatColor.translateAlternateColorCodes('&', config.getString("messages.standing"));
        msgOccupied = ChatColor.translateAlternateColorCodes('&', config.getString("messages.occupied"));
        msgNoPerm = ChatColor.translateAlternateColorCodes('&', config.getString("messages.no-permission"));
        msgEnabled = ChatColor.translateAlternateColorCodes('&', config.getString("messages.enabled"));
        msgDisabled = ChatColor.translateAlternateColorCodes('&', config.getString("messages.disabled"));
        msgReloaded = ChatColor.translateAlternateColorCodes('&', config.getString("messages.reloaded"));
        msgCommandRestricted = ChatColor.translateAlternateColorCodes('&', config.getString("messages.command-restricted"));

        allowedBlocks = new ArrayList<>();
        for (String s : config.getStringList("sit-blocks")) {
            double sh = 0.7;
            String tmp[] = s.split("[:]");
            String type = tmp[0];
            if (tmp.length == 2) {
                sh = Double.parseDouble(tmp[1]);
            }
            Material mat = Material.matchMaterial(type);
            if (mat != null) {
                logger.log(Level.INFO, "Allowed block: {0} => {1}", new Object[]{mat.toString(), sh});
                allowedBlocks.add(new ChairBlock(mat, sh));
            } else {
                logger.log(Level.SEVERE, "Invalid block: {0}", type);
            }
        }

        validSigns = new ArrayList<>();
        for (String type : config.getStringList("valid-signs")) {
            try {
                validSigns.add(Material.matchMaterial(type));
            } catch (Exception e) {
                logger.log(Level.SEVERE, e.getMessage());
            }
        }
    }

    /**
     * @return the allowedBlocks
     */
    public List<ChairBlock> getAllowedBlocks() {
        return allowedBlocks;
    }

    /**
     * @return the validSigns
     */
    public List<Material> getValidSigns() {
        return validSigns;
    }

    /**
     * @return the autoRotate
     */
    public boolean isAutoRotate() {
        return autoRotate;
    }

    /**
     * @return the signCheck
     */
    public boolean isSignCheck() {
        return signCheck;
    }

    /**
     * @return the notifyplayer
     */
    public boolean isNotifyplayer() {
        return notifyplayer;
    }

    /**
     * @return the ignoreIfBlockInHand
     */
    public boolean isIgnoreIfBlockInHand() {
        return ignoreIfBlockInHand;
    }

    /**
     * @return the distance
     */
    public double getDistance() {
        return distance;
    }

    /**
     * @return the maxChairWidth
     */
    public int getMaxChairWidth() {
        return maxChairWidth;
    }

    /**
     * @return the sitHealEnabled
     */
    public boolean isSitHealEnabled() {
        return sitHealEnabled;
    }

    /**
     * @return the sitMaxHealth
     */
    public int getSitMaxHealth() {
        return sitMaxHealth;
    }

    /**
     * @return the sitHealthPerInterval
     */
    public int getSitHealthPerInterval() {
        return sitHealthPerInterval;
    }

    /**
     * @return the sitHealInterval
     */
    public int getSitHealInterval() {
        return sitHealInterval;
    }

    /**
     * @return the sitPickupEnabled
     */
    public boolean isSitPickupEnabled() {
        return sitPickupEnabled;
    }

    /**
     * @return the sitDisableAllCommands
     */
    public boolean isSitDisableAllCommands() {
        return sitDisableAllCommands;
    }

    /**
     * @return the sitDisabledCommands
     */
    public HashSet<String> getSitDisabledCommands() {
        return sitDisabledCommands;
    }

    /**
     * @return the msgSitting
     */
    public String getMsgSitting() {
        return msgSitting;
    }

    /**
     * @return the msgStanding
     */
    public String getMsgStanding() {
        return msgStanding;
    }

    /**
     * @return the msgOccupied
     */
    public String getMsgOccupied() {
        return msgOccupied;
    }

    /**
     * @return the msgNoPerm
     */
    public String getMsgNoPerm() {
        return msgNoPerm;
    }

    /**
     * @return the msgReloaded
     */
    public String getMsgReloaded() {
        return msgReloaded;
    }

    /**
     * @return the msgDisabled
     */
    public String getMsgDisabled() {
        return msgDisabled;
    }

    /**
     * @return the msgEnabled
     */
    public String getMsgEnabled() {
        return msgEnabled;
    }

    /**
     * @return the msgCommandRestricted
     */
    public String getMsgCommandRestricted() {
        return msgCommandRestricted;
    }

}
