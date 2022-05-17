package com.rasturize.anticheat.data.playerdata.handler;

import com.rasturize.anticheat.data.playerdata.PlayerData;
import org.bukkit.entity.Player;

public interface PlayerDataManager {
    PlayerDataManager instance = getInstance();

    static PlayerData getData(Player player) {
        return instance._getData(player);
    }

    static void setData(Player player, PlayerData data) {
        instance._setData(player, data);
    }

    static void removeData(Player player) { instance._removeData(player); }

    PlayerData _getData(Player player);

    void _setData(Player player, PlayerData data);

    void _removeData(Player player);

    static PlayerDataManager getInstance() {
        if (instance != null) {
            return instance;
        }
        return new PlayerDataManagerLegacy();
//        try {
//            return (PlayerDataManager) Reflective.c(PlayerDataManagerModern.class.getName()).newInstance();
//        } catch (Exception e) {
//            System.err.println("Failed to instantiate PlayerDataManagerModern, falling back to legacy system. (ignore this message if you are using 1.7)");
//            return new PlayerDataManagerLegacy();
//        }
    }
}
