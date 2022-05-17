package com.rasturize.anticheat.checks.killaura;

import com.rasturize.anticheat.api.check.type.CheckType;
import com.rasturize.anticheat.api.check.Check;
import com.rasturize.anticheat.protocol.packet.in.WrappedInCloseWindowPacket;
import com.rasturize.anticheat.protocol.packet.in.WrappedInFlyingPacket;
import com.rasturize.anticheat.protocol.packet.in.WrappedInUseEntityPacket;

@CheckType(id = "killaura:g", name = "KillAura G", type = CheckType.Type.KILLAURA, maxVl = 3)
public class KillAuraG extends Check {
    private boolean sent;

    void check(WrappedInFlyingPacket wrappedInFlyingPacket) {
        this.sent = false;
    }

    void check(WrappedInCloseWindowPacket wrappedInCloseWindowPacket) {
        this.sent = true;
    }

    void check(WrappedInUseEntityPacket wrappedInUseEntityPacket) {
        if (!canCheck())
            return;

        if (wrappedInUseEntityPacket.getAction() == WrappedInUseEntityPacket.EnumEntityUseAction.ATTACK && this.sent) {
            if (fail())
                wrappedInUseEntityPacket.setCancelled(true);
        }
    }
}
