package com.rasturize.anticheat.checks.inventory;

import com.rasturize.anticheat.api.check.Check;
import com.rasturize.anticheat.api.check.type.CheckType;
import com.rasturize.anticheat.data.TimedLocation;
import com.rasturize.anticheat.protocol.packet.in.WrappedInWindowClickPacket;

import java.util.LinkedList;

@CheckType(id = "inventory:c", name = "Inventory C", type = CheckType.Type.INVENTORY)
public class InventoryC extends Check {
    private double threshold;
    private final LinkedList<Long> delays = new LinkedList<>();

    void check(WrappedInWindowClickPacket wrappedInWindowClickPacket) {
        if (!canCheck())
            return;

        TimedLocation lastMovePacket = playerData.locations.getLast().getX();

        if (lastMovePacket == null)
            return;

        long delay = System.currentTimeMillis() - lastMovePacket.getTime();

        this.delays.add(delay);

        if (this.delays.size() == 10) {
            double average = this.delays.stream().mapToLong(loopDelay -> loopDelay).asDoubleStream().sum();
            average /= this.delays.size();

            this.delays.clear();

            if (average <= 35.0) {
                if ((threshold += 1.25) >= 4.0) {
                    fail(2, 20 * 60, "a=%.1f,d=%.2f", average, threshold);

                    threshold = 0.0;
                }
            } else {
                threshold -= 0.5;
            }
        }
    }
}
