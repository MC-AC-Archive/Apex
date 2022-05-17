package com.rasturize.anticheat.checks.autoclicker;

import com.rasturize.anticheat.api.check.Check;
import com.rasturize.anticheat.api.check.type.CheckType;
import com.rasturize.anticheat.protocol.packet.in.WrappedInArmAnimationPacket;
import com.rasturize.anticheat.protocol.packet.in.WrappedInBlockDigPacket;
import com.rasturize.anticheat.protocol.packet.in.WrappedInFlyingPacket;

@CheckType(id = "autoclicker:e", name = "AutoClicker E", type = CheckType.Type.AUTOCLICKER, maxVl = 5)
public class AutoClickerE extends Check {
    private boolean isReceived;
    private long lastSwing, delay;

    private int vl;

    void check(WrappedInBlockDigPacket wrappedInBlockDigPacket) {
        if (!canCheck())
            return;

        switch (wrappedInBlockDigPacket.getAction()) {
            case START_DESTROY_BLOCK:
                this.isReceived = false;
                break;
            case ABORT_DESTROY_BLOCK:
                // No butterfly clicking falses
                if (!isReceived && delay > 10L) {
                    if (++vl >= 10) {
                        fail();
                    }
                } else {
                    vl = 0;
                }

                break;
        }
    }

    void check(WrappedInFlyingPacket wrappedInFlyingPacket) {
        this.isReceived = true;
    }

    void check(WrappedInArmAnimationPacket wrappedInArmAnimationPacket) {
        long now = System.currentTimeMillis();

        delay = now - lastSwing;
        lastSwing = now;
    }
}
