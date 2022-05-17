package com.rasturize.anticheat.parsers;

import com.rasturize.anticheat.api.check.Check;
import com.rasturize.anticheat.api.check.type.Parser;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;

@Parser
public class StateParser extends Check implements Listener {

    void check(PlayerToggleSneakEvent event) {
        if (event.isCancelled())
            return;

        playerData.state.isSneaking = event.isSneaking();
    }
}
