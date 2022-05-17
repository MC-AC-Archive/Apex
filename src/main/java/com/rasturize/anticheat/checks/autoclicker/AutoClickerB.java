package com.rasturize.anticheat.checks.autoclicker;

import com.rasturize.anticheat.api.check.Check;
import com.rasturize.anticheat.api.check.type.CheckType;
import com.rasturize.anticheat.protocol.api.ProtocolVersion;
import com.rasturize.anticheat.protocol.packet.in.WrappedInArmAnimationPacket;
import com.rasturize.anticheat.protocol.packet.in.WrappedInFlyingPacket;

@CheckType(id = "autoclicker:b", name = "AutoClicker B", type = CheckType.Type.AUTOCLICKER, maxVl = 5)
public class AutoClickerB extends Check {
    private int cps, swings, movements;

    void check(WrappedInFlyingPacket wrappedInFlyingPacket) {
        if (!canCheck() || playerData.protocolVersion.isAbove(ProtocolVersion.V1_8_9))
            return;

        if (++this.movements == 20) {
            if (this.swings > 20) {
                fail("c=%d", swings);
            }

            this.cps = swings;
            this.movements = this.swings = 0;
        }
    }

    void check(WrappedInArmAnimationPacket wrappedInArmAnimationPacket) {
        if (!playerData.state.isDigging && !playerData.state.isPlacing) {
            ++this.swings;
        }
    }
}
