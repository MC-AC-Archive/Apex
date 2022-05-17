package com.rasturize.anticheat.data.playerdata.handler;

import com.rasturize.anticheat.data.playerdata.PlayerData;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerDataManagerLegacy implements PlayerDataManager {
	private Map<UUID, PlayerData> playerDataMap = new ConcurrentHashMap<>();

	public PlayerData _getData(Player player) {
		return playerDataMap.get(player.getUniqueId());
	}

	public void _setData(Player player, PlayerData data) {
		playerDataMap.put(player.getUniqueId(), data);
	}

	public void _removeData(Player player) {
		playerDataMap.remove(player.getUniqueId());
	}
}
