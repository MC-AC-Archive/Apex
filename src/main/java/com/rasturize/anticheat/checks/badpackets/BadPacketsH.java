package com.rasturize.anticheat.checks.badpackets;

import com.rasturize.anticheat.api.check.Check;
import com.rasturize.anticheat.api.check.type.CheckType;
import com.rasturize.anticheat.protocol.packet.in.WrappedInBlockDigPacket;

import static com.rasturize.anticheat.protocol.packet.in.WrappedInBlockDigPacket.EnumPlayerDigType.RELEASE_USE_ITEM;

@CheckType(id = "badpackets:h", name = "BadPackets H", type = CheckType.Type.BADPACKET, maxVl = 5)
public class BadPacketsH extends Check {

    void check(WrappedInBlockDigPacket wrappedInBlockDigPacket) {
        if (!canCheck())
            return;

        if (wrappedInBlockDigPacket.getAction() == RELEASE_USE_ITEM && playerData.state.isPlacing) {
            fail();
        }
    }
}
