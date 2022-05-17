package com.rasturize.anticheat.bridges.v1_7_R4;

import com.rasturize.anticheat.api.bridge.Bridge;
import net.minecraft.server.v1_7_R4.EntityPlayer;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.util.NumberConversions;

public class Spigot1_7_R4 implements Bridge {
    @Override
    public double getBlockFriction(Player player) {
        EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();

        int blockX = NumberConversions.floor(entityPlayer.locX);
        int blockY = NumberConversions.floor(entityPlayer.locY - 1);
        int blockZ = NumberConversions.floor(entityPlayer.locZ);

        return entityPlayer.world.getType(blockX, blockY, blockZ).frictionFactor;
    }

    @Override
    public double getAttributeSpeed(Player player) {
        EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();

        return entityPlayer.bl();
    }

    @Override
    public float getPlayerBoost(Player player) {
        EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();

        return entityPlayer.aQ;
    }

    @Override
    public boolean onGround(Player player) {
        EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();

        return entityPlayer.onGround;
    }
}
