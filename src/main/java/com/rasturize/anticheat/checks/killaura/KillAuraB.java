package com.rasturize.anticheat.checks.killaura;

import com.rasturize.anticheat.api.check.type.CheckType;
import com.rasturize.anticheat.api.config.ConfigValue;
import com.rasturize.anticheat.api.check.Check;
import com.rasturize.anticheat.protocol.packet.in.WrappedInArmAnimationPacket;
import com.rasturize.anticheat.protocol.packet.in.WrappedInFlyingPacket;
import com.rasturize.anticheat.protocol.packet.in.WrappedInUseEntityPacket;

@CheckType(id = "killaura:b", name = "KillAura B", type = CheckType.Type.KILLAURA)
public class KillAuraB extends Check {
    private int swings, attacks, doubleSwings, doubleAttacks, bareSwings, threshold;

    void check(WrappedInArmAnimationPacket wrappedInArmAnimationPacket) {
        if (!canCheck() || !playerData.state.isDigging || !playerData.state.isPlacing)
            this.swings++;
    }

    void check(WrappedInUseEntityPacket wrappedInUseEntityPacket) {
        if (wrappedInUseEntityPacket.getAction() == WrappedInUseEntityPacket.EnumEntityUseAction.ATTACK) {
            if (!canCheck() || !playerData.state.isDigging || !playerData.state.isPlacing)
                this.attacks++;
        }
    }

    void check(WrappedInFlyingPacket wrappedInFlyingPacket) {
        if (playerData.state.isDigging || playerData.state.isPlacing)
            return;

        if ((this.swings < this.attacks || (this.attacks != 0 && this.swings > this.attacks))) {
            fail("s=%d,a=%d", swings, attacks);
        }

        if (this.swings > 1 && this.attacks == 0) {
            ++this.doubleSwings;
        } else if (this.swings == 1 && this.attacks == 0) {
            ++this.bareSwings;
        } else if (this.attacks > 1) {
            ++this.doubleAttacks;
        }

        if (this.doubleSwings + this.doubleAttacks == 20) {
            if (this.doubleSwings == 0) {
                if (this.bareSwings > 10 && ++threshold > 3.0) {
                    fail("b=%d,d=%d", this.bareSwings, threshold);
                }
            }
        }
        this.swings = 0;
        this.attacks = 0;

        this.doubleSwings = 0;
        this.doubleAttacks = 0;
        this.bareSwings = 0;
    }
}
