package com.rasturize.anticheat.checks.killaura;

import com.rasturize.anticheat.api.check.type.CheckType;
import com.rasturize.anticheat.api.config.ConfigValue;
import com.rasturize.anticheat.api.check.Check;
import com.rasturize.anticheat.protocol.packet.in.WrappedInBlockPlacePacket;
import com.rasturize.anticheat.protocol.packet.in.WrappedInFlyingPacket;
import com.rasturize.anticheat.protocol.packet.in.WrappedInUseEntityPacket;

@CheckType(id = "killaura:c", name = "KillAura C", type = CheckType.Type.KILLAURA)
public class KillAuraC extends Check {
    private boolean sentAttack, sentInteract;

    void check(WrappedInFlyingPacket wrappedInFlyingPacket) {
        this.sentAttack = false;
        this.sentInteract = false;
    }

    void check(WrappedInUseEntityPacket wrappedInUseEntityPacket) {
        if (wrappedInUseEntityPacket.getAction() == WrappedInUseEntityPacket.EnumEntityUseAction.ATTACK) {
            this.sentAttack = true;
        } else if (wrappedInUseEntityPacket.getAction() == WrappedInUseEntityPacket.EnumEntityUseAction.INTERACT) {
            this.sentInteract = true;
        }
    }

    void check(WrappedInBlockPlacePacket wrappedInBlockPlacePacket) {
        if (!canCheck())
            return;

        if (this.sentAttack && !this.sentInteract) {
            fail();
        }
    }
}
