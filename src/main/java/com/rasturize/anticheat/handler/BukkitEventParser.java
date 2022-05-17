package com.rasturize.anticheat.handler;

import com.rasturize.anticheat.data.playerdata.PlayerData;
import com.rasturize.anticheat.utils.Init;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

// This is the fastest...
@Init
public class BukkitEventParser implements Listener {
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onEvent(BlockPistonExtendEvent event) {
		event.getBlock().getWorld().getPlayers()
				.stream()
				.filter(player -> event.getBlock().getLocation().distance(player.getLocation()) < 10)
				.forEach(player -> event.getBlocks()
						.stream()
						.filter(block -> block.getType() == Material.SLIME_BLOCK)
						.filter(block -> player.getLocation().distance(block.getLocation()) < 2.5)
						.forEach(block -> PlayerData.getData(player).enviroment.lastSlimePush = System.currentTimeMillis()));
	}

	@EventHandler(priority = EventPriority.MONITOR)
	void onEvent(PlayerInteractEvent event) {
		if (isInvalidPlayer(event.getPlayer()))
			return;
		PlayerData.getData(event.getPlayer()).fireChecks(event);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	void onEvent(PlayerGameModeChangeEvent event) {
		if (isInvalidPlayer(event.getPlayer()))
			return;

		PlayerData playerData = PlayerData.getData(event.getPlayer());

		playerData.gameMode = event.getNewGameMode();

		playerData.fireChecks(event);
	}

	private boolean isInvalidPlayer(Player player) {
		if (PlayerData.getOrNull(player) != null)
			return false;

		return !TinyProtocolHandler.instance.hasInjected(player);
	}
}
