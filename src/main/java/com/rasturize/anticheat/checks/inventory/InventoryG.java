package com.rasturize.anticheat.checks.inventory;

import com.rasturize.anticheat.api.check.Check;
import com.rasturize.anticheat.api.check.type.CheckType;
import com.rasturize.anticheat.protocol.packet.in.WrappedInClientCommandPacket;
import com.rasturize.anticheat.protocol.packet.in.WrappedInEntityActionPacket;
import com.rasturize.anticheat.protocol.packet.in.WrappedInFlyingPacket;
import com.rasturize.anticheat.protocol.packet.in.WrappedInSteerVehiclePacket;

@CheckType(id = "inventory:g", name = "Inventory G", type = CheckType.Type.INVENTORY)
public class InventoryG extends Check {
    private boolean sent, vehicle;

    void check(WrappedInFlyingPacket wrappedInFlyingPacket) {
        if (!canCheck())
            return;

        if (this.sent) {
            fail();
        }
        this.vehicle = false;
        this.sent = false;
    }

    void check(WrappedInSteerVehiclePacket wrappedInSteerVehiclePacket) {
        this.vehicle = true;
    }

    void check(WrappedInEntityActionPacket wrappedInEntityActionPacket) {
        if (wrappedInEntityActionPacket.getAction() == WrappedInEntityActionPacket.EnumPlayerAction.STOP_SPRINTING) {
            this.sent = false;
        }
    }

    void check(WrappedInClientCommandPacket wrappedInClientCommandPacket) {
        if (wrappedInClientCommandPacket.getCommand() == WrappedInClientCommandPacket.EnumClientCommand.OPEN_INVENTORY_ACHIEVEMENT) {
            if (playerData.state.isSprinting && !this.vehicle) {
                this.sent = true;
            }
        }
    }
}
