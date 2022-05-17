package com.rasturize.anticheat.checks.badpackets;

import com.rasturize.anticheat.api.check.Check;
import com.rasturize.anticheat.api.check.type.CheckType;
import com.rasturize.anticheat.protocol.packet.in.WrappedInFlyingPacket;

@CheckType(id = "badpackets:b", name = "BadPackets B", type = CheckType.Type.BADPACKET)
public class BadPacketsB extends Check {

    void check(WrappedInFlyingPacket wrappedInFlyingPacket) {
        if (wrappedInFlyingPacket.isLook() && Math.abs(wrappedInFlyingPacket.getPitch()) > 90.0f)
            fail();
    }
}
