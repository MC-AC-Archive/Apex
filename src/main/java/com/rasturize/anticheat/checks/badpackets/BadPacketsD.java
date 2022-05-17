package com.rasturize.anticheat.checks.badpackets;

import com.rasturize.anticheat.api.check.Check;
import com.rasturize.anticheat.api.check.type.CheckType;
import com.rasturize.anticheat.protocol.packet.in.WrappedInEntityActionPacket;
import com.rasturize.anticheat.protocol.packet.in.WrappedInFlyingPacket;

@CheckType(id = "badpackets:d", name = "BadPackets D", type = CheckType.Type.BADPACKET, maxVl = 5)
public class BadPacketsD extends Check {

    private boolean sentAction;

    void check(WrappedInEntityActionPacket wrappedInEntityActionPacket) {
        if (wrappedInEntityActionPacket.getAction() == WrappedInEntityActionPacket.EnumPlayerAction.START_SNEAKING
                || wrappedInEntityActionPacket.getAction() == WrappedInEntityActionPacket.EnumPlayerAction.STOP_SNEAKING) {
            if (this.sentAction)
                fail();
            else this.sentAction = true;
        }
    }

    void check (WrappedInFlyingPacket wrappedInFlyingPacket) {
        this.sentAction = false;
    }
}
