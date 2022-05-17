package com.rasturize.anticheat.checks.badpackets;

import com.rasturize.anticheat.api.check.Check;
import com.rasturize.anticheat.api.check.type.CheckType;
import com.rasturize.anticheat.protocol.packet.in.WrappedInFlyingPacket;
import com.rasturize.anticheat.protocol.packet.in.WrappedInSteerVehiclePacket;

@CheckType(id = "badpackets:f", name = "BadPackets F", type = CheckType.Type.BADPACKET, maxVl = 3)
public class BadPacketsF extends Check {
    private float lastYaw, lastPitch;
    private boolean ignore;

    void check(WrappedInFlyingPacket wrappedInFlyingPacket) {
        if (!canCheck())
            return;

        if (!wrappedInFlyingPacket.isPos() && wrappedInFlyingPacket.isLook()) {
            if (this.lastYaw == wrappedInFlyingPacket.getYaw() && this.lastPitch == wrappedInFlyingPacket.getPitch()) {
                if (!this.ignore) {
                    fail();
                }
                this.ignore = false;
            }
            this.lastYaw = wrappedInFlyingPacket.getYaw();
            this.lastPitch = wrappedInFlyingPacket.getPitch();
        } else {
            this.ignore = true;
        }
    }

    void check(WrappedInSteerVehiclePacket wrappedInSteerVehiclePacket) {
        this.ignore = true;
    }
}
