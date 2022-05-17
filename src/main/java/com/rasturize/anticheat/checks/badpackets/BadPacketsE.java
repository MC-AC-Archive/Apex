package com.rasturize.anticheat.checks.badpackets;

import com.rasturize.anticheat.api.check.Check;
import com.rasturize.anticheat.api.check.type.CheckType;
import com.rasturize.anticheat.protocol.packet.in.WrappedInHeldItemSlotPacket;

@CheckType(id = "badpackets:e", name = "BadPackets E", type = CheckType.Type.BADPACKET, maxVl = 3)
public class BadPacketsE extends Check {

    void check(WrappedInHeldItemSlotPacket wrappedInHeldItemSlotPacket) {
        if (playerData.state.isPlacing)
            fail();
    }
}
