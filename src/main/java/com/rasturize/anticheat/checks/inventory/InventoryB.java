package com.rasturize.anticheat.checks.inventory;


import com.rasturize.anticheat.api.check.Check;
import com.rasturize.anticheat.api.check.type.CheckType;
import com.rasturize.anticheat.protocol.api.NMSObject;
import com.rasturize.anticheat.protocol.packet.in.WrappedInArmAnimationPacket;
import com.rasturize.anticheat.protocol.packet.in.WrappedInEntityActionPacket;

@CheckType(id = "inventory:b", name = "Inventory B", type = CheckType.Type.INVENTORY, maxVl = 2, timeout = 20 * 60)
public class InventoryB extends Check {

    void check(WrappedInEntityActionPacket wrappedInEntityActionPacket) {
        check0(wrappedInEntityActionPacket);
    }

    void check(WrappedInArmAnimationPacket wrappedInArmAnimationPacket) {
        check0(wrappedInArmAnimationPacket);
    }

    private void check0(NMSObject nmsObject) {
        if (!canCheck())
            return;

        if (((nmsObject instanceof WrappedInEntityActionPacket && ((WrappedInEntityActionPacket) nmsObject).getAction() == WrappedInEntityActionPacket.EnumPlayerAction.START_SPRINTING) || nmsObject instanceof WrappedInArmAnimationPacket) && playerData.state.isInventoryOpen) {
            fail();

            playerData.state.isInventoryOpen = false;
        }
    }
}
