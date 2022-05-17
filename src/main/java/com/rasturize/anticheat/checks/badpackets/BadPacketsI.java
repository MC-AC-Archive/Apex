package com.rasturize.anticheat.checks.badpackets;

import com.rasturize.anticheat.api.check.Check;
import com.rasturize.anticheat.api.check.type.CheckType;
import com.rasturize.anticheat.protocol.packet.in.WrappedInHeldItemSlotPacket;

@CheckType(id = "badpackets:i", name = "BadPackets I", type = CheckType.Type.BADPACKET, maxVl = 3)
public class BadPacketsI extends Check {
    private int lastSlot;

    void check(WrappedInHeldItemSlotPacket wrappedInHeldItemSlotPacket) {
        if (!canCheck())
            return;
        int slot = wrappedInHeldItemSlotPacket.getSlot();

        if (this.lastSlot == slot)
            if (fail()) wrappedInHeldItemSlotPacket.setCancelled(true);

        this.lastSlot = slot;
    }
}
