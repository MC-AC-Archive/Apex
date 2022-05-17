package com.rasturize.anticheat.checks.badpackets;

import com.rasturize.anticheat.api.check.Check;
import com.rasturize.anticheat.api.check.type.CheckType;
import com.rasturize.anticheat.protocol.api.ProtocolVersion;
import com.rasturize.anticheat.protocol.packet.in.WrappedInFlyingPacket;
import com.rasturize.anticheat.protocol.packet.in.WrappedInSteerVehiclePacket;
import org.bukkit.Bukkit;

@CheckType(id = "badpackets:a", name = "BadPackets A", type = CheckType.Type.BADPACKET, maxVl = 3)
public class BadPacketsA extends Check {
    private int flyingStreak;

    void check(WrappedInFlyingPacket wrappedInFlyingPacket) {
        if (!canCheck() || playerData.protocolVersion.isAbove(ProtocolVersion.V1_8_9))
            return;

        if (wrappedInFlyingPacket.isPos()) {
            this.flyingStreak = 0;
        } else if (this.flyingStreak++ > 20) {
            fail();
        }
    }

    void check(WrappedInSteerVehiclePacket packet) {
        this.flyingStreak = 0;
    }
}
