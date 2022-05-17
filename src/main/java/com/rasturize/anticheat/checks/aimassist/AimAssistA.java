package com.rasturize.anticheat.checks.aimassist;

import com.rasturize.anticheat.api.check.Check;
import com.rasturize.anticheat.api.check.type.CheckType;
import com.rasturize.anticheat.protocol.packet.in.WrappedInFlyingPacket;
import org.bukkit.Bukkit;
import org.bukkit.util.Vector;

@CheckType(id = "aimassist:a", name = "AimAssist A", type = CheckType.Type.COMBAT)
public class AimAssistA extends Check {
    private int current = 0;
    private double multiplier = Math.pow(2.0, 24.0);
    private float lastPitch = -1;
    
    private long[] gcdLog = new long[10];

    void check(WrappedInFlyingPacket packet) {
        if (!canCheck() || !packet.isLook() || System.currentTimeMillis() - playerData.state.lastAttack > 500L)
            return;

        Vector first = new Vector(playerData.movement.yawDelta, 0, playerData.movement.pitchDelta);
        Vector second = new Vector(playerData.movement.lastYawDelta, 0, playerData.movement.lastPitchDelta);

        double angle = Math.pow(first.angle(second) * 180, 2);

        boolean flagged = false;

        long deviation = getDeviation(playerData.movement.pitchDelta);

        gcdLog[current % gcdLog.length] = deviation;
        current++;

        if (playerData.movement.tpitch > -20 && playerData.movement.tpitch < 20
                && playerData.movement.pitchDelta > 0
                && playerData.movement.yawDelta > 1
                && playerData.movement.yawDelta < 10
                && playerData.movement.lastYawDelta <= playerData.movement.yawDelta
                && playerData.movement.yawDifference != 0
                && playerData.movement.yawDifference < 1
                && angle > 2500) {
            if (current > gcdLog.length) {
                long maxDeviation = 0;

                for (long l : gcdLog) {
                    if (deviation != 0 && l != 0)
                        maxDeviation = Math.max(Math.max(l, deviation) % Math.min(l, deviation), maxDeviation);
                }

                if (maxDeviation > 0.0) {
                    flagged = true;
                    fail(2, 20 * 5);

                    reset();
                }
            }

            if (deviation > 0.0) {
                flagged = true;
                fail(2, 20 * 5);

                reset();
            }
        }
        debug("y1=%.5f,y2=%.5f,a=%.5f" + (flagged ? " §c§lFLAGGED" : ""), playerData.movement.yawDelta, playerData.movement.yawDifference, angle);
    }

    private long getDeviation(float pitchChange) {
        if (lastPitch != -1) {
            long current = (long) (pitchChange * multiplier);

            long last = (long) (lastPitch * multiplier);
            long value = convert(current, last);

            if (value < 0x20000) {
                return value;
            }
        }

        lastPitch = pitchChange;
        return -1;
    }

    public void reset() {
        lastPitch = -1;
    }

    private long convert(long current, long last) {
        if (last <= 16384) return current;
        return convert(last, current % last);
    }
}