package com.rasturize.anticheat.checks.autoclicker;

import com.rasturize.anticheat.api.check.Check;
import com.rasturize.anticheat.api.check.type.CheckType;
import com.rasturize.anticheat.protocol.packet.in.WrappedInArmAnimationPacket;
import com.rasturize.anticheat.protocol.packet.in.WrappedInFlyingPacket;

import java.util.Deque;
import java.util.LinkedList;

@CheckType(id = "autoclicker:f", name = "AutoClicker F", type = CheckType.Type.AUTOCLICKER, maxVl = 5)
public class AutoClickerF extends Check {
    private Deque<Integer> cpsSamples = new LinkedList<>();

    private int cps, ticks, vl;

    void check(WrappedInArmAnimationPacket wrappedInArmAnimationPacket) {
        if (playerData.state.isDigging)
            return;

        this.cps++;
    }

    void check(WrappedInFlyingPacket wrappedInFlyingPacket) {
        if (++ticks == 20) {
            cpsSamples.add(cps);

            double cpsAverage = cpsSamples.stream().mapToDouble(Integer::doubleValue).average().orElse(0.0),
                    cpsRatio = cpsAverage / cps;

            if (cps > 8 && cpsAverage >= 8) {
                if (cpsRatio > 0.99) {
                    if (++vl > 3) {
                        fail();
                    }
                } else {
                    vl = 0;
                }
            }

            ticks = 0;
            cps = 0;
        }

    }
}
