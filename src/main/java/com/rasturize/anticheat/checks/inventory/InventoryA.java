package com.rasturize.anticheat.checks.inventory;

import com.rasturize.anticheat.api.check.Check;
import com.rasturize.anticheat.api.check.Priority;
import com.rasturize.anticheat.api.check.type.CheckType;
import com.rasturize.anticheat.protocol.packet.in.WrappedInWindowClickPacket;

@CheckType(id = "inventory:a", name = "Inventory A", type = CheckType.Type.INVENTORY, maxVl = 6)
public class InventoryA extends Check {

    @Priority(50)
    void check(WrappedInWindowClickPacket wrappedInWindowClickPacket) {
        if (!canCheck())
            return;

        if (wrappedInWindowClickPacket.getId() == 0 && !playerData.state.isInventoryOpen) {
            fail();

            playerData.state.isInventoryOpen = true;
        }
    }
}
