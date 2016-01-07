package com.cnaude.chairs.vehiclearrow.v1_8_R2;

import net.minecraft.server.v1_8_R2.EntityArrow;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R2.CraftWorld;
import org.bukkit.entity.Arrow;

import com.cnaude.chairs.vehiclearrow.NMSArrowFactoryInterface;

public class NMSArrowFactory implements NMSArrowFactoryInterface {

    @Override
    public Arrow spawnArrow(Location location) {
        CraftWorld world = (CraftWorld) location.getWorld();
        EntityArrow arrow = new NMSChairsArrow(world, location);
        return (Arrow) arrow.getBukkitEntity();
    }

}
