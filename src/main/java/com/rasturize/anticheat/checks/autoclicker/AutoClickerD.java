package com.rasturize.anticheat.checks.autoclicker;

import com.rasturize.anticheat.api.check.Check;
import com.rasturize.anticheat.api.check.type.CheckType;
import com.rasturize.anticheat.data.TimedLocation;
import com.rasturize.anticheat.protocol.packet.in.WrappedInArmAnimationPacket;

import java.util.ArrayList;
import java.util.List;

@CheckType(id = "autoclicker:d", name = "AutoClicker D", type = CheckType.Type.AUTOCLICKER, maxVl = 3)
public class AutoClickerD extends Check {
    private List<Long> recentDelays = new ArrayList<>();

    void check(WrappedInArmAnimationPacket wrappedInArmAnimationPacket) {
        if (!canCheck())
            return;

        TimedLocation lastMovePacket = playerData.locations.getLast().getX();

        if (lastMovePacket == null)
            return;

        long delay = System.currentTimeMillis() - lastMovePacket.getTime();

        this.recentDelays.add(delay);

        if (this.recentDelays.size() == 40) {
            double averageDelay = this.recentDelays.stream()
                    .mapToDouble(Long::doubleValue)
                    .average()
                    .orElse(0.0);

            if (averageDelay <= this.recentDelays.size()) {
                fail("ad=%.0f,s=%.0f", averageDelay, this.recentDelays.size());
            }

            this.recentDelays.clear();
        }
    }
}
