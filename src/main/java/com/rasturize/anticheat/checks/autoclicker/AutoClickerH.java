package com.rasturize.anticheat.checks.autoclicker;

import com.rasturize.anticheat.api.check.Check;
import com.rasturize.anticheat.api.check.type.CheckType;
import com.rasturize.anticheat.protocol.packet.in.WrappedInArmAnimationPacket;

import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;

@CheckType(id = "autoclicker:h", name = "AutoClicker H", type = CheckType.Type.AUTOCLICKER, maxVl = 10)
public class AutoClickerH extends Check {
    private long lastClick;
    private int firstVl, secondVl;

    private final Deque<Long> delayDeque = new LinkedList<>();

    void check(WrappedInArmAnimationPacket wrappedInArmAnimationPacket) {
        if (!canCheck())
            return;

        long now = System.currentTimeMillis(),
                delay = now - lastClick;

        delayDeque.add(delay);

        if (delayDeque.size() == 5) {
            AtomicInteger invalidClicks = new AtomicInteger(), doubleClicks = new AtomicInteger();

            delayDeque.stream().filter(l -> l == 0L).forEach(l -> invalidClicks.getAndIncrement());
            delayDeque.stream().filter(l -> l == 1L).forEach(l -> doubleClicks.getAndIncrement());

            // Clicking over an autoclicker (Triple Clicking)
            if (invalidClicks.get() > 0) {
                if (++firstVl > 3) {
                    fail("A");
                }
            } else {
                firstVl = 0;
            }

            // Using a cps-helper (Double Clicking)
            if (doubleClicks.get() >= 3) {
                if (secondVl > 3) {
                    fail("B");
                }
            } else {
                secondVl = Math.max(secondVl - 1, 0);
            }

            delayDeque.clear();
        }

        lastClick = now;
    }
}
