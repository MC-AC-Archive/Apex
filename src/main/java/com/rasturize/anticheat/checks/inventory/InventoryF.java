package com.rasturize.anticheat.checks.inventory;

import com.rasturize.anticheat.api.check.Check;
import com.rasturize.anticheat.api.check.type.CheckType;
import com.rasturize.anticheat.protocol.packet.in.WrappedInClientCommandPacket;
import com.rasturize.anticheat.protocol.packet.in.WrappedInCloseWindowPacket;
import com.rasturize.anticheat.protocol.packet.in.WrappedInFlyingPacket;

@CheckType(id = "inventory:f", name = "Inventory F", type = CheckType.Type.INVENTORY)
public class InventoryF extends Check {
    private boolean sent;

    void check(WrappedInFlyingPacket wrappedInFlyingPacket) {
        this.sent = false;
    }

    void check(WrappedInCloseWindowPacket wrappedInCloseWindowPacket) {
        if (!canCheck())
            return;

        if (this.sent) {
            fail();
        }
    }

    void check(WrappedInClientCommandPacket wrappedInClientCommandPacket) {
        if (wrappedInClientCommandPacket.getCommand() == WrappedInClientCommandPacket.EnumClientCommand.OPEN_INVENTORY_ACHIEVEMENT) {
            this.sent = true;
        }
    }
}
