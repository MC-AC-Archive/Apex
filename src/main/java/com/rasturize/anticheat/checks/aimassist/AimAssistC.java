package com.rasturize.anticheat.checks.aimassist;

import com.rasturize.anticheat.api.check.Check;
import com.rasturize.anticheat.api.check.Setting;
import com.rasturize.anticheat.api.check.type.CheckType;
import com.rasturize.anticheat.protocol.packet.in.WrappedInFlyingPacket;

import java.util.Deque;
import java.util.LinkedList;

@CheckType(id = "aimassist:c", name = "AimAssist C", type = CheckType.Type.COMBAT, maxVl = 2)
public class AimAssistC extends Check {

    private final Deque<Float> pitchSamples = new LinkedList<>();

    @Setting
    private static int minimumDuplicates = 9;

    @Setting
    private static int minimumStreak = 2;

    private int streak;

    void check(WrappedInFlyingPacket wrappedInFlyingPacket) {
        if (!canCheck() || !wrappedInFlyingPacket.isLook())
            return;

        float yawDelta = playerData.movement.yawDelta,
                pitchDelta = playerData.movement.pitchDelta;

        if (yawDelta > 1.f && pitchDelta > 0.0) {
            pitchSamples.add(pitchDelta);

            if (pitchSamples.size() == 120) {
                long distinct = pitchSamples.stream().distinct().count(),
                        duplicates = pitchSamples.size() - distinct;

                if (duplicates <= minimumDuplicates) {
                    if (++streak >= minimumStreak) {
                        fail("d=%d,s=%s", duplicates, streak);
                    }
                } else {
                    streak = 0;
                }

                pitchSamples.clear();
            }
        }
    }
}
