package com.rasturize.anticheat.checks.killaura;

import com.rasturize.anticheat.api.check.type.CheckType;
import com.rasturize.anticheat.api.config.ConfigValue;
import com.rasturize.anticheat.api.check.Check;
import com.rasturize.anticheat.protocol.api.ProtocolVersion;
import com.rasturize.anticheat.protocol.packet.in.WrappedInBlockDigPacket;
import com.rasturize.anticheat.protocol.packet.in.WrappedInFlyingPacket;
import com.rasturize.anticheat.protocol.packet.in.WrappedInUseEntityPacket;

@CheckType(id = "killaura:f", name = "KillAura F", type = CheckType.Type.KILLAURA, maxVl = 2)
public class KillAuraF extends Check {
    private boolean sent;

    void check(WrappedInFlyingPacket wrappedInFlyingPacket) {
        this.sent = false;
    }

    void check(WrappedInUseEntityPacket wrappedInUseEntityPacket) {
        this.sent = true;
    }

    void check(WrappedInBlockDigPacket wrappedInBlockDigPacket) {
        if (!canCheck() || playerData.protocolVersion.isAbove(ProtocolVersion.V1_8_9))
            return;

        if ((wrappedInBlockDigPacket.getAction() == WrappedInBlockDigPacket.EnumPlayerDigType.START_DESTROY_BLOCK
                || wrappedInBlockDigPacket.getAction() == WrappedInBlockDigPacket.EnumPlayerDigType.RELEASE_USE_ITEM) && this.sent) {
            fail();
        }
    }
}
