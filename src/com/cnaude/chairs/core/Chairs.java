package com.cnaude.chairs.core;

import com.cnaude.chairs.api.ChairsAPI;
import java.util.HashSet;

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
    private ConfigData configData;

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

        configData = new ConfigData(this);
        if (configData.isSitHealEnabled()) {
            chairEffects.startHealing();
        }
        if (configData.isSitPickupEnabled()) {
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

    /**
     * Handles all the aspects of reloading the plugin if you change the config
     * file and want the plugin to act accordingly to your new settings
     */
    public void reload() {
        configData.reload();
        if (configData.isSitHealEnabled()) {
            chairEffects.restartHealing();
        } else {
            chairEffects.cancelHealing();
        }
        if (configData.isSitPickupEnabled()) {
            chairEffects.restartPickup();
        } else {
            chairEffects.cancelPickup();
        }
    }

    public ConfigData getConfigData() {
        return configData;
    }

    /**
     * @return the sitDisabled
     */
    public HashSet<UUID> getSitDisabled() {
        return sitDisabled;
    }

}
