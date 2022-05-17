package com.rasturize.anticheat.checks.autoclicker;

import com.rasturize.anticheat.api.check.Check;
import com.rasturize.anticheat.api.check.type.CheckType;
import com.rasturize.anticheat.protocol.packet.in.WrappedInArmAnimationPacket;
import com.rasturize.anticheat.protocol.packet.in.WrappedInBlockDigPacket;
import com.rasturize.anticheat.protocol.packet.in.WrappedInFlyingPacket;

@CheckType(id = "autoclicker:a", name = "AutoClicker A", type = CheckType.Type.AUTOCLICKER, maxVl = 5)
public class AutoClickerA extends Check {
    private int clicks, outliers, flyingCount;
    private boolean release;
    private double vl;

    void check(WrappedInFlyingPacket wrappedInFlyingPacket) {
        flyingCount++;
    }

    void check(WrappedInBlockDigPacket wrappedInBlockDigPacket) {
        if (wrappedInBlockDigPacket.getAction() == WrappedInBlockDigPacket.EnumPlayerDigType.RELEASE_USE_ITEM) {
            this.release = true;
        }
    }

    void check(WrappedInArmAnimationPacket wrappedInArmAnimationPacket) {
        if (!canCheck())
            return;

        if (!playerData.state.isDigging && !playerData.state.isPlacing) {
            if (this.flyingCount < 10) {
                if (this.release) {
                    this.release = false;
                    this.flyingCount = 0;
                    return;
                }
                if (this.flyingCount > 3) {
                    ++this.outliers;
                } else if (this.flyingCount == 0) {
                    return;
                }
                if (++this.clicks == 40) {
                    if (this.outliers == 0) {
                        if ((vl += 1.4) >= 4.0) {
                            fail("o=%s,d=%.2f", this.outliers, vl);
                        }
                    } else {
                        vl -= 0.8;
                    }
                    this.outliers = 0;
                    this.clicks = 0;
                }
            }
            this.flyingCount = 0;
        }
    }
}
