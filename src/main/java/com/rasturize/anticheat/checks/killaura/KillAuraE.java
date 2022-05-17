package com.rasturize.anticheat.checks.killaura;

import com.rasturize.anticheat.api.check.type.CheckType;
import com.rasturize.anticheat.api.check.Check;
import com.rasturize.anticheat.protocol.api.ProtocolVersion;
import com.rasturize.anticheat.protocol.packet.in.WrappedInBlockDigPacket;
import com.rasturize.anticheat.protocol.packet.in.WrappedInFlyingPacket;
import com.rasturize.anticheat.protocol.packet.in.WrappedInUseEntityPacket;

@CheckType(id = "killaura:e", name = "KillAura E", maxVl = 3, type = CheckType.Type.KILLAURA)
public class KillAuraE extends Check {
    private boolean sent;

    void check(WrappedInFlyingPacket wrappedInFlyingPacket) {
        this.sent = false;
    }

    void check(WrappedInBlockDigPacket wrappedInBlockDigPacket) {
        if (wrappedInBlockDigPacket.getAction() == WrappedInBlockDigPacket.EnumPlayerDigType.START_DESTROY_BLOCK
                || wrappedInBlockDigPacket.getAction() == WrappedInBlockDigPacket.EnumPlayerDigType.ABORT_DESTROY_BLOCK
                || wrappedInBlockDigPacket.getAction() == WrappedInBlockDigPacket.EnumPlayerDigType.RELEASE_USE_ITEM) {
            this.sent = true;
        }
    }

    void check(WrappedInUseEntityPacket wrappedInUseEntityPacket) {
        if (!canCheck() || playerData.protocolVersion.isAbove(ProtocolVersion.V1_8_9))
            return;

        if (this.sent) {
            if (fail())
                wrappedInUseEntityPacket.setCancelled(true);
        }
    }
}
