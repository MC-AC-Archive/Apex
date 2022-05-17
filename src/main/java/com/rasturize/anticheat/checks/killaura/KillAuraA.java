package com.rasturize.anticheat.checks.killaura;

import com.rasturize.anticheat.api.check.type.CheckType;
import com.rasturize.anticheat.api.config.ConfigValue;
import com.rasturize.anticheat.api.check.Check;
import com.rasturize.anticheat.protocol.packet.in.WrappedInFlyingPacket;
import com.rasturize.anticheat.protocol.packet.in.WrappedInUseEntityPacket;

@CheckType(id = "killaura:a", name = "KillAura A", maxVl = 5, type = CheckType.Type.KILLAURA)
public class KillAuraA extends Check {
    private double threshold;
    private long lastFlying, lastAttack;

    void check(WrappedInUseEntityPacket wrappedInUseEntityPacket) {
        boolean lagging = playerData.lag.transactionPing - playerData.lag.keepAlivePing >= 20 && playerData.lag.transactionPing - playerData.lag.keepAlivePing <= 30;

        if (playerData.isSkippingTicks() || lagging)
            return;

        if (wrappedInUseEntityPacket.getAction() == WrappedInUseEntityPacket.EnumEntityUseAction.ATTACK) {
            if (this.lastFlying + 20L > System.currentTimeMillis()) {
                this.lastAttack = System.currentTimeMillis();
            } else if (this.threshold > 0) {
                this.threshold -= 0.5;
            }
        }
    }

    void check(WrappedInFlyingPacket wrappedInFlyingPacket) {
        boolean lagging = playerData.lag.transactionPing - playerData.lag.keepAlivePing >= 20 && playerData.lag.transactionPing - playerData.lag.keepAlivePing <= 30;

        if (playerData.isSkippingTicks() || lagging)
            return;

        if (this.lastAttack + 100L > System.currentTimeMillis()) {
            if (this.threshold++ > 4) {
                fail("t=%s,d=%.1f", this.lastAttack - System.currentTimeMillis(), threshold);
            }
        } else if (this.threshold > 0) {
            this.threshold -= 0.5;
        }

        this.lastFlying = System.currentTimeMillis();
    }
}
