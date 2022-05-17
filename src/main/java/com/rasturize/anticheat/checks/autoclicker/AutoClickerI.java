package com.rasturize.anticheat.checks.autoclicker;

import com.rasturize.anticheat.api.check.Check;
import com.rasturize.anticheat.api.check.type.CheckType;
import com.rasturize.anticheat.protocol.packet.in.WrappedInArmAnimationPacket;
import com.rasturize.anticheat.protocol.packet.in.WrappedInFlyingPacket;
import com.rasturize.anticheat.utils.PlayerTimer;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@CheckType(id = "autoclicker:i", name = "AutoClicker I", type = CheckType.Type.AUTOCLICKER, maxVl = 5)
public class AutoClickerI extends Check {

    private int cps, clicks, zeroDelayClicks, concurrentClicks, ticksPassedSinceClick;
    private double lastDistance;

    private List<Integer> cpsStorage = new ArrayList<>();

    private PlayerTimer lastCheck;

    void check(WrappedInFlyingPacket wrappedInFlyingPacket) {
        if (this.lastCheck == null) {
            this.lastCheck = new PlayerTimer(playerData);
        }

        if (this.concurrentClicks > 0) {
            if (this.ticksPassedSinceClick == 3)
                this.concurrentClicks -= 5;
            else if (this.ticksPassedSinceClick == 5)
                this.concurrentClicks -= 15;
            else
                this.concurrentClicks--;
        }
        this.zeroDelayClicks = 0;
        this.ticksPassedSinceClick++;
    }

    void check(WrappedInArmAnimationPacket wrappedInArmAnimationPacket) {
        if (this.ticksPassedSinceClick == 20) {
            this.cps = 0;
            this.lastCheck.reset();
        }

        this.clicks++;
        this.concurrentClicks += 2;

        this.zeroDelayClicks++;

        if (this.zeroDelayClicks > 2)
            fail("zdc=%.0f", this.zeroDelayClicks);

        if (this.concurrentClicks > 50)
            fail("con=%.0f", this.concurrentClicks);

        if (this.clicks >= 2)
            this.clicks--;

        if (this.lastCheck.hasPassed(20)) {
            this.lastCheck.add(cps);

            this.cpsStorage.add(cps);

            if (this.cpsStorage.size() >= 5) {
                double average = this.cpsStorage.stream().collect(Collectors.averagingDouble(d -> d));

                if (average > 7.0) {
                    double distance = 0.0;

                    for (int cps : this.cpsStorage)
                        distance += Math.abs(average - cps);

                    distance /= this.cpsStorage.size() * 0.8;
                    distance *= (20 - average) * 0.1;

                    double change = this.lastDistance - distance;
                    this.lastDistance = distance;

                    if (distance < 0.25 && distance > 0) {
                        fail("dist=%.0f", distance);
                    } else if (distance == lastDistance && change == 0.0) {
                        fail("d=%.0f,ld=%.0f,ch=%.0f", distance, lastDistance, change);
                    }
                }
            }
            if (this.cpsStorage.size() > 20) {
                this.cpsStorage.remove(0);
            }

            this.cps = 0;
            this.lastCheck.reset();
        }
        this.cps++;
        this.ticksPassedSinceClick = 0;
    }
}
