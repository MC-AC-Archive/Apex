package com.rasturize.anticheat.checks.autoclicker;

import com.rasturize.anticheat.api.check.Check;
import com.rasturize.anticheat.api.check.type.CheckType;
import com.rasturize.anticheat.protocol.packet.in.WrappedInArmAnimationPacket;

import java.util.Deque;
import java.util.LinkedList;

@CheckType(id = "autoclicker:g", name = "AutoClicker G", type = CheckType.Type.AUTOCLICKER, maxVl = 3)
public class AutoClickerG extends Check {
    private long lastSwing;
    private double lastAverage;

    private final Deque<Long> swingSamples = new LinkedList<>();

    void check(WrappedInArmAnimationPacket wrappedInArmAnimationPacket) {
        if (!canCheck() || playerData.state.isDigging)
            return;

        long now = System.currentTimeMillis(),
                delay = now - lastSwing;

        swingSamples.add(delay);

        if (swingSamples.size() == 20) {
            double average = swingSamples.stream()
                    .mapToLong(l -> l)
                    .average()
                    .orElse(0.0);

            double totalSwings =  swingSamples.stream().mapToLong(change -> change).asDoubleStream().sum(),
                    mean = totalSwings / swingSamples.size(),
                    deviation = swingSamples.stream().mapToLong(change -> change).mapToDouble(change -> Math.pow(change - mean, 2)).sum();

            if (Math.sqrt(deviation) < 150.0 && average > 100.0 && Math.abs(average - this.lastAverage) <= 5.0) {
                fail();
            }

            lastAverage = average;
            swingSamples.clear();
        }

        lastSwing = now;
    }
}
