package com.rasturize.anticheat.api.bridge;

import org.bukkit.entity.Player;

public interface Bridge {
    double getBlockFriction(Player player);
    double getAttributeSpeed(Player player);
    float getPlayerBoost(Player player);
    boolean onGround(Player player);
}
