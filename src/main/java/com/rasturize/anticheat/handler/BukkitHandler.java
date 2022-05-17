/*
 * Copyright (c) 2018 NGXDEV.COM. Licensed under MIT.
 */

package com.rasturize.anticheat.handler;

import com.rasturize.anticheat.Apex;
import com.rasturize.anticheat.data.playerdata.PlayerData;
import com.rasturize.anticheat.data.playerdata.handler.PlayerDataManager;
import com.rasturize.anticheat.utils.Init;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

@Init
public class BukkitHandler implements Listener {

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onEvent(PlayerJoinEvent event) {
		PlayerData playerData;

		try {
			playerData = PlayerData.getData(event.getPlayer());
		} catch (Exception ex) {
			event.getPlayer().kickPlayer("[Apex] The server is still starting up! Please wait a few moments!");
			return;
		}

		Bukkit.getScheduler().scheduleSyncDelayedTask(Apex.instance, () -> PlayerDataManager.setData(event.getPlayer(), playerData), 1);

		playerData.state.lastLogin = System.currentTimeMillis();
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onEvent(PlayerQuitEvent event) {
		PlayerData playerData = PlayerData.getOrNull(event.getPlayer());

		if (playerData == null) {
			System.out.println("[Apex] Failed to delete PlayerData class for " + event.getPlayer().getName());
			return;
		}

		playerData.methods.clear();
		playerData.validChecks.clear();

		playerData.allChecks.clear();

		PlayerDataManager.removeData(event.getPlayer());
	}
}
