package com.rasturize.anticheat.checks.aimassist;

import com.rasturize.anticheat.api.check.Check;
import com.rasturize.anticheat.api.check.type.CheckType;
import com.rasturize.anticheat.protocol.packet.in.WrappedInFlyingPacket;

@CheckType(id = "aimassist:b", name = "AimAssist B", type = CheckType.Type.COMBAT, maxVl = 3)
public class AimAssistB extends Check {
    private int threshold;

    void check(WrappedInFlyingPacket wrappedInFlyingPacket) {
        if (!canCheck() || !wrappedInFlyingPacket.isLook())
            return;

        float yawDelta = playerData.movement.yawDelta;
        float pitchDelta = playerData.movement.pitchDelta;

        if (yawDelta > 0.0 && pitchDelta > 0.0) {
            int roundedYaw = Math.round(yawDelta),
                    previousRoundedYaw = Math.round(playerData.movement.lastYawDelta);

            float yawDeltaChange = Math.abs(yawDelta - playerData.movement.lastYawDelta);

            if (roundedYaw == previousRoundedYaw && yawDelta > 0.01 && yawDelta > 1.5F && yawDeltaChange > 0.001) {
                if (++threshold > 5) {
                    fail();
                }
            } else {
                threshold = Math.max(threshold - 1, 0);
            }
        }
    }
}
