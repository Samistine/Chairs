package com.cnaude.chairs.core;

import com.cnaude.chairs.api.ChairsAPI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.cnaude.chairs.commands.ChairsCommand;
import com.cnaude.chairs.listeners.NANLoginListener;
import com.cnaude.chairs.listeners.TrySitEventListener;
import com.cnaude.chairs.listeners.TryUnsitEventListener;
import com.cnaude.chairs.sitaddons.ChairEffects;
import com.cnaude.chairs.sitaddons.CommandRestrict;
import com.cnaude.chairs.vehiclearrow.NMSAccess;
import java.util.UUID;

public class Chairs extends JavaPlugin {

    private HashSet<UUID> sitDisabled = new HashSet<>();
    private ChairEffects chairEffects;
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

    private PlayerSitData psitdata;

    public PlayerSitData getPlayerSitData() {
        return psitdata;
    }
    private NMSAccess nmsaccess = new NMSAccess();

    protected NMSAccess getNMSAccess() {
        return nmsaccess;
    }

    @Override
    public void onEnable() {
        try {
            nmsaccess.setupChairsArrow();
        } catch (Exception e) {
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        chairEffects = new ChairEffects(this);
        psitdata = new PlayerSitData(this);
        getConfig().options().copyDefaults(true);
        saveConfig();
        loadConfig();
        if (sitHealEnabled) {
            chairEffects.startHealing();
        }
        if (sitPickupEnabled) {
            chairEffects.startPickup();
        }
        getServer().getPluginManager().registerEvents(new NANLoginListener(), this);
        getServer().getPluginManager().registerEvents(new TrySitEventListener(this), this);
        getServer().getPluginManager().registerEvents(new TryUnsitEventListener(this), this);
        getServer().getPluginManager().registerEvents(new CommandRestrict(this), this);
        getCommand("chairs").setExecutor(new ChairsCommand(this));
        ChairsAPI.init(getPlayerSitData());
    }

    @Override
    public void onDisable() {
        if (psitdata != null) {
            for (Player player : getServer().getOnlinePlayers()) {
                if (psitdata.isSitting(player)) {
                    psitdata.unsitPlayerForce(player);
                }
            }
        }
        if (chairEffects != null) {
            chairEffects.cancelHealing();
            chairEffects.cancelPickup();
            chairEffects = null;
        }
        nmsaccess = null;
        psitdata = null;
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();
        loadConfig();
    }

    private void loadConfig() {
        autoRotate = getConfig().getBoolean("auto-rotate");
        signCheck = getConfig().getBoolean("sign-check");
        distance = getConfig().getDouble("distance");
        maxChairWidth = getConfig().getInt("max-chair-width");
        notifyplayer = getConfig().getBoolean("notify-player");
        ignoreIfBlockInHand = getConfig().getBoolean("ignore-if-item-in-hand");

        sitHealEnabled = getConfig().getBoolean("sit-effects.healing.enabled", false);
        sitHealInterval = getConfig().getInt("sit-effects.healing.interval", 20);
        sitMaxHealth = getConfig().getInt("sit-effects.healing.max-percent", 100);
        sitHealthPerInterval = getConfig().getInt("sit-effects.healing.amount", 1);

        sitPickupEnabled = getConfig().getBoolean("sit-effects.itempickup.enabled", false);

        sitDisableAllCommands = getConfig().getBoolean("sit-restrictions.commands.all");
        sitDisabledCommands = new HashSet<>(getConfig().getStringList("sit-restrictions.commands.list"));

        msgSitting = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.sitting"));
        msgStanding = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.standing"));
        msgOccupied = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.occupied"));
        msgNoPerm = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.no-permission"));
        msgEnabled = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.enabled"));
        msgDisabled = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.disabled"));
        msgReloaded = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.reloaded"));
        msgCommandRestricted = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.command-restricted"));

        allowedBlocks = new ArrayList<>();
        for (String s : getConfig().getStringList("sit-blocks")) {
            double sh = 0.7;
            String tmp[] = s.split("[:]");
            String type = tmp[0];
            if (tmp.length == 2) {
                sh = Double.parseDouble(tmp[1]);
            }
            Material mat = Material.matchMaterial(type);
            if (mat != null) {
                logInfo("Allowed block: " + mat.toString() + " => " + sh);
                allowedBlocks.add(new ChairBlock(mat, sh));
            } else {
                logError("Invalid block: " + type);
            }
        }

        validSigns = new ArrayList<>();
        for (String type : getConfig().getStringList("valid-signs")) {
            try {
                validSigns.add(Material.matchMaterial(type));
            } catch (Exception e) {
                logError(e.getMessage());
            }
        }
    }

    /**
     * @return the sitDisabled
     */
    public HashSet<UUID> getSitDisabled() {
        return sitDisabled;
    }

    /**
     * @return the chairEffects
     */
    public ChairEffects getChairEffects() {
        return chairEffects;
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

    private void logInfo(String _message) {
        getLogger().log(Level.INFO, _message);
    }

    private void logError(String _message) {
        getLogger().log(Level.SEVERE, _message);
    }

}
