package com.rasturize.anticheat.checks.killaura;

import com.rasturize.anticheat.api.check.type.CheckType;
import com.rasturize.anticheat.api.check.Check;
import com.rasturize.anticheat.protocol.packet.in.WrappedInFlyingPacket;
import com.rasturize.anticheat.protocol.packet.in.WrappedInHeldItemSlotPacket;
import com.rasturize.anticheat.protocol.packet.in.WrappedInUseEntityPacket;

@CheckType(id = "killaura:i", name = "KillAura I", type = CheckType.Type.KILLAURA, maxVl = 3)
public class KillAuraI extends Check {
    private boolean sent;

    void check(WrappedInFlyingPacket wrappedInFlyingPacket) {
        this.sent = false;
    }

    void check(WrappedInUseEntityPacket wrappedInUseEntityPacket) {
        if (wrappedInUseEntityPacket.getAction() == WrappedInUseEntityPacket.EnumEntityUseAction.ATTACK) {
            this.sent = true;
        }
    }

    void check(WrappedInHeldItemSlotPacket wrappedInHeldItemSlotPacket) {
        if (!canCheck())
            return;

        if (this.sent) {
            if (fail())
                wrappedInHeldItemSlotPacket.setCancelled(true);
        }
    }
}
