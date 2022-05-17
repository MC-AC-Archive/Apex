package com.rasturize.anticheat.checks.autoclicker;

import com.rasturize.anticheat.api.check.Check;
import com.rasturize.anticheat.api.check.type.CheckType;
import com.rasturize.anticheat.protocol.packet.in.WrappedInArmAnimationPacket;
import com.rasturize.anticheat.protocol.packet.in.WrappedInFlyingPacket;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

@CheckType(id = "autoclicker:j", name = "AutoClicker J", type = CheckType.Type.AUTOCLICKER, maxVl = 7)
public class AutoClickerJ extends Check {

    private int motionPackets = 0, armAnimations = 0, lastArmAnimation = 0;
    private long lastAnimation;
    private List<Integer> animationStorage = new ArrayList<>();

    void check(WrappedInFlyingPacket wrappedInFlyingPacket) {
        this.motionPackets++;

        if (this.armAnimations <= 0)
            return;
        if (this.motionPackets < 20)
            return;
        if (System.currentTimeMillis() - this.lastAnimation > 200L)
            return;

        this.armAnimations = 0;
        this.motionPackets = 0;
    }

    void check(WrappedInArmAnimationPacket wrappedInArmAnimationPacket) {
        if (playerData.state.isDigging) {
            this.armAnimations = 0;
            this.motionPackets = 0;
            this.animationStorage.clear();
            return;
        }

        this.armAnimations++;

        if (this.lastArmAnimation > this.armAnimations) {
            this.animationStorage.add(this.lastArmAnimation);

            if (this.animationStorage.size() == 5) {
                int[] cps = new int[5];

                IntStream.range(0, 5).forEach(i -> {
                    int swing = this.animationStorage.get(i);
                    cps[i] = i == 0 ? swing - 1 : swing;
                });

                int rate = 1;

                for (int i2 = 0; i2 < 4; rate += cps[i2] - cps[i2 + 1], ++i2) ;

                rate = Math.abs(rate);

                double averageCps = Arrays.stream(cps).average().orElse(0.0);

                if (rate == 1 && averageCps > 8.0 && (double) Math.round(averageCps) == averageCps) {
                    fail("avg=%.0f", averageCps);
                }
                this.animationStorage.clear();
            }
        }

        this.lastArmAnimation = this.armAnimations;
        this.lastAnimation = System.currentTimeMillis();
    }
}
