package com.rasturize.anticheat.bridges.v1_8_R3;

import com.rasturize.anticheat.api.bridge.Bridge;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.util.NumberConversions;


public class Spigot1_8_R3 implements Bridge {
    @Override
    public double getBlockFriction(Player player) {
        EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();

        int blockX = NumberConversions.floor(entityPlayer.locX);
        int blockY = NumberConversions.floor(entityPlayer.locY - 1);
        int blockZ = NumberConversions.floor(entityPlayer.locZ);

        return entityPlayer.world.getType(new BlockPosition(blockX, blockY, blockZ)).getBlock().frictionFactor;
    }

    @Override
    public double getAttributeSpeed(Player player) {
        EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();

        return entityPlayer.bI();
    }

    @Override
    public float getPlayerBoost(Player player) {
        EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();

        return entityPlayer.aM;
    }

    @Override
    public boolean onGround(Player player) {
        EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();

        return entityPlayer.onGround;
    }
}
