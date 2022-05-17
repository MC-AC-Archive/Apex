package com.rasturize.anticheat.checks.killaura;

import com.rasturize.anticheat.api.check.type.CheckType;
import com.rasturize.anticheat.api.check.Check;
import com.rasturize.anticheat.protocol.packet.in.WrappedInUseEntityPacket;

@CheckType(id = "killaura:d", name = "KillAura D", maxVl = 3, type = CheckType.Type.KILLAURA)
public class KillAuraD extends Check {

    void check(WrappedInUseEntityPacket wrappedInUseEntityPacket) {
        if (!canCheck())
            return;

        if (wrappedInUseEntityPacket.getAction() == WrappedInUseEntityPacket.EnumEntityUseAction.ATTACK && playerData.state.isPlacing) {
            if (fail())
                wrappedInUseEntityPacket.setCancelled(true);
        }
    }
}
