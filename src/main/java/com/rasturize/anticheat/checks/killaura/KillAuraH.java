package com.rasturize.anticheat.checks.killaura;

import com.rasturize.anticheat.api.check.type.CheckType;
import com.rasturize.anticheat.api.check.Check;
import com.rasturize.anticheat.protocol.packet.in.WrappedInClientCommandPacket;
import com.rasturize.anticheat.protocol.packet.in.WrappedInFlyingPacket;
import com.rasturize.anticheat.protocol.packet.in.WrappedInUseEntityPacket;
import org.bukkit.Bukkit;

@CheckType(id = "killaura:h", name = "KillAura H", type = CheckType.Type.KILLAURA, maxVl =  3)
public class KillAuraH extends Check {
    private boolean sent;

    void check(WrappedInUseEntityPacket wrappedInUseEntityPacket) {
        if (!canCheck()) return;

        if (playerData.state.isInventoryOpen) {
            if (fail()) {
                player.closeInventory();
                playerData.state.isInventoryOpen = false; //prevent falses due to force closing
            }
        }
    }
}
