package com.rasturize.anticheat.checks.inventory;

import com.rasturize.anticheat.api.check.Check;
import com.rasturize.anticheat.api.check.type.CheckType;
import com.rasturize.anticheat.protocol.packet.in.WrappedInClientCommandPacket;
import com.rasturize.anticheat.protocol.packet.in.WrappedInFlyingPacket;
import com.rasturize.anticheat.protocol.packet.in.WrappedInWindowClickPacket;

@CheckType(id = "inventory:e", name = "Inventory E", type = CheckType.Type.INVENTORY)
public class InventoryE extends Check {
    private boolean sent;

    void check(WrappedInFlyingPacket wrappedInFlyingPacket) {
        this.sent = false;
    }

    void check(WrappedInWindowClickPacket wrappedInWindowClickPacket) {
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
