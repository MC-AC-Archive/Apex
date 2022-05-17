package com.rasturize.anticheat.checks.badpackets;

import com.rasturize.anticheat.api.check.Check;
import com.rasturize.anticheat.api.check.type.CheckType;
import com.rasturize.anticheat.protocol.packet.in.WrappedInBlockPlacePacket;
import com.rasturize.anticheat.protocol.packet.in.WrappedInEntityActionPacket;
import com.rasturize.anticheat.protocol.packet.in.WrappedInFlyingPacket;
import com.rasturize.anticheat.protocol.packet.in.WrappedInSteerVehiclePacket;
import org.bukkit.inventory.ItemStack;

@CheckType(id = "badpackets:g", name = "BadPackets G", type = CheckType.Type.BADPACKET, maxVl = 2)
public class BadPacketsG extends Check {
    private boolean sent, vehicle;

    void check(WrappedInFlyingPacket wrappedInFlyingPacket) {
        if (!canCheck()) return;

        if (this.sent)
            fail();

        this.vehicle = false;
        this.sent = false;
    }

    void check(WrappedInBlockPlacePacket wrappedInBlockPlacePacket) {
        if (wrappedInBlockPlacePacket.getFace() == 255) {
            ItemStack itemStack = wrappedInBlockPlacePacket.getItemStack();

            if (itemStack != null && itemStack.getType().name().toLowerCase().contains("sword") && playerData.state.isSprinting && !this.vehicle) {
                this.sent = true;
            }
        }
    }

    void check(WrappedInEntityActionPacket wrappedInEntityActionPacket) {
        this.sent = false;
    }

    void check(WrappedInSteerVehiclePacket wrappedInSteerVehiclePacket) {
        this.vehicle = true;
    }
}
