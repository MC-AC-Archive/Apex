package com.rasturize.anticheat.checks.autoclicker;

import com.rasturize.anticheat.api.check.Check;
import com.rasturize.anticheat.api.check.type.CheckType;
import com.rasturize.anticheat.protocol.packet.in.WrappedInBlockDigPacket;
import com.rasturize.anticheat.protocol.packet.in.WrappedInFlyingPacket;
import com.rasturize.anticheat.protocol.packet.types.BaseBlockPosition;

import java.util.LinkedList;

@CheckType(id = "autoclicker:c", name = "AutoClicker C", type = CheckType.Type.AUTOCLICKER, maxVl = 3)
public class AutoClickerC extends Check {
    private final LinkedList<Integer> recentCounts = new LinkedList<>();
    private BaseBlockPosition lastBlock;

    private int flyingCount;
    private double vl;

    void check(WrappedInFlyingPacket wrappedInFlyingPacket) {
        this.flyingCount++;
    }

    void check(WrappedInBlockDigPacket wrappedInBlockDigPacket) {
        if (!canCheck())
            return;

        if (wrappedInBlockDigPacket.getAction() == WrappedInBlockDigPacket.EnumPlayerDigType.START_DESTROY_BLOCK) {
            if (this.lastBlock != null && this.lastBlock.equals(wrappedInBlockDigPacket.getBlockPosition())) {
                this.recentCounts.addLast(this.flyingCount);

                if (this.recentCounts.size() == 20) {
                    double average = this.recentCounts.stream().mapToInt(i -> i).asDoubleStream().sum();
                    average /= this.recentCounts.size();
                    double stdDev = 0.0;

                    for (final int j : this.recentCounts) {
                        stdDev += Math.pow(j - average, 2.0);
                    }

                    stdDev /= this.recentCounts.size();
                    stdDev = Math.sqrt(stdDev);

                    if (stdDev < 0.45 && ++vl >= 3.0) {
                        fail("s=%.2f,d=%.1f", stdDev, vl);
                    } else {
                        vl -= 0.5;
                    }
                    this.recentCounts.clear();
                }
            }
            this.flyingCount = 0;
        } else if (wrappedInBlockDigPacket.getAction() == WrappedInBlockDigPacket.EnumPlayerDigType.ABORT_DESTROY_BLOCK) {
            this.lastBlock = wrappedInBlockDigPacket.getBlockPosition();
        }
    }
}
