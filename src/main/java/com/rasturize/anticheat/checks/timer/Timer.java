package com.rasturize.anticheat.checks.timer;

import com.rasturize.anticheat.api.check.type.CheckType;
import com.rasturize.anticheat.api.check.Check;
import com.rasturize.anticheat.api.check.Priority;
import com.rasturize.anticheat.protocol.api.ProtocolVersion;
import com.rasturize.anticheat.protocol.packet.in.WrappedInFlyingPacket;
import com.rasturize.anticheat.utils.MovingStats;
import org.bukkit.Bukkit;

@CheckType(id = "timer:a", name = "Timer A", type = CheckType.Type.CONNECTION, maxVl = 3, timeout = 20 * 10)
public class Timer extends Check {
   private final MovingStats movingStats = new MovingStats(20);

    @Priority(Priority.Value.MIN)
    void check(WrappedInFlyingPacket wrappedInFlyingPacket) {
        if (System.currentTimeMillis() - playerData.state.lastLogin <= 3000L)
            return;

        boolean lagging = playerData.lag.transactionPing - playerData.lag.keepAlivePing >= 20 && playerData.lag.transactionPing - playerData.lag.keepAlivePing <= 30;

        if (playerData.isSkippingTicks() || playerData.state.isTeleporting || lagging)
            return;

        if (playerData.protocolVersion.isAbove(ProtocolVersion.V1_8_9) && !isMoving())
            return;

        movingStats.add(playerData.lag.packetDelay);

        double max = 7.07;
        double stdDev = movingStats.getStdDev(max);

        if (!Double.isNaN(stdDev) && stdDev < max) {
            fail(2, 20 * 40, "%.0f", stdDev);
        }
    }
}
