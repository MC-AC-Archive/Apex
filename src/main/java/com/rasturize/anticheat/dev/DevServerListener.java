package com.rasturize.anticheat.dev;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class DevServerListener implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent event) {
        try {
            event.setJoinMessage(null);

            Player player = event.getPlayer();

            player.sendMessage(new String[]{
                    "Welcome to Apex AntiCheat's Test Server!",
                    "",
                    "Join our discord @ https://discord.gg/WWCrWSF"
            });

            Inventory inv = player.getInventory();

            inv.setItem(0, new ItemStack(Material.DIAMOND_SWORD));
            inv.setItem(1, new ItemStack(Material.DIAMOND_PICKAXE));
            inv.setItem(2, new ItemStack(Material.BOW));
            inv.setItem(9, new ItemStack(Material.ARROW, 64));
        } catch (Throwable ignored) {
        }
    }

    @EventHandler
    public void onFoodChange(FoodLevelChangeEvent event) {
        if (event.getFoodLevel() != 20.0)
            event.setFoodLevel(20);
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event instanceof EntityDamageByEntityEvent) {
            event.setDamage(0);
        }
    }
}
