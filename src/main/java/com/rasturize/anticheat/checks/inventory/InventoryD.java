package com.rasturize.anticheat.checks.inventory;

import com.rasturize.anticheat.api.check.Check;
import com.rasturize.anticheat.api.check.type.CheckType;
import com.rasturize.anticheat.protocol.api.NMSObject;
import com.rasturize.anticheat.protocol.packet.in.WrappedInClientCommandPacket;
import com.rasturize.anticheat.protocol.packet.in.WrappedInFlyingPacket;

@CheckType(id = "inventory:d", name = "Inventory D", type = CheckType.Type.INVENTORY)
public class InventoryD extends Check {
    private int stage;

    void check(WrappedInFlyingPacket wrappedInFlyingPacket) {
        check0(wrappedInFlyingPacket);
    }

    void check(WrappedInClientCommandPacket wrappedInClientCommandPacket) {
        check0(wrappedInClientCommandPacket);
    }

    private void check0(NMSObject nmsObject) {
        if (!canCheck())
            return;

        if (this.stage == 0) {
            if (nmsObject instanceof WrappedInClientCommandPacket && ((WrappedInClientCommandPacket) nmsObject).getCommand() == WrappedInClientCommandPacket.EnumClientCommand.OPEN_INVENTORY_ACHIEVEMENT) {
                ++this.stage;
            }
        } else if (this.stage == 1) {
            if (nmsObject instanceof WrappedInFlyingPacket && ((WrappedInFlyingPacket) nmsObject).isLook()) {
                ++this.stage;
            } else {
                this.stage = 0;
            }
        } else if (this.stage == 2) {
            if (nmsObject instanceof WrappedInFlyingPacket && ((WrappedInFlyingPacket) nmsObject).isLook()) {
                fail();
            }
            this.stage = 0;
        }
    }
}
