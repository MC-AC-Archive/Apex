package com.rasturize.anticheat.parsers;

import com.rasturize.anticheat.api.check.Check;
import com.rasturize.anticheat.api.check.type.Parser;
import com.rasturize.anticheat.protocol.packet.in.*;
import org.bukkit.Bukkit;

@Parser
public class MiscParser extends Check {
    boolean sprinting;

    void check(WrappedInEntityActionPacket wrappedInEntityActionPacket) {
        switch (wrappedInEntityActionPacket.getAction()) {
            case START_SPRINTING:
                playerData.state.isSprinting = true;
                break;
            case STOP_SPRINTING:
                playerData.state.isSprinting = false;
                break;
            case START_SNEAKING:
                playerData.state.isSneaking = true;
                break;
            case STOP_SNEAKING:
                playerData.state.isSneaking = false;
                break;
        }
    }

    void check(WrappedInUseEntityPacket wrappedInUseEntityPacket) {
        if (wrappedInUseEntityPacket.getAction() == WrappedInUseEntityPacket.EnumEntityUseAction.ATTACK) {
            playerData.state.isAttacking = true;
            playerData.state.lastAttack = System.currentTimeMillis();
        }
    }

    void check(WrappedInArmAnimationPacket wrappedInArmAnimationPacket) {
        playerData.state.isSwinging = true;
    }

    void check(WrappedInBlockPlacePacket wrappedInBlockPlacePacket) {
        playerData.state.isPlacing = true;
    }

    void check(WrappedInBlockDigPacket wrappedInBlockDigPacket) {
        switch (wrappedInBlockDigPacket.getAction()) {
            case START_DESTROY_BLOCK:
                playerData.state.isDigging = true;
                break;
            case STOP_DESTROY_BLOCK:
            case ABORT_DESTROY_BLOCK:
                playerData.state.isDigging = false;
                break;
        }
    }

    void check(WrappedInFlyingPacket wrappedInFlyingPacket) {
        playerData.state.isSwinging = false;
        playerData.state.isAttacking = false;

        playerData.state.isPlacing = false;
    }

    void check(WrappedInClientCommandPacket wrappedInClientCommandPacket) {
        if (wrappedInClientCommandPacket.getCommand() == WrappedInClientCommandPacket.EnumClientCommand.OPEN_INVENTORY_ACHIEVEMENT) {
            playerData.state.isInventoryOpen = true;
        }
    }

    void check(WrappedInCloseWindowPacket wrappedInCloseWindowPacket) {
        playerData.state.isInventoryOpen = false;
    }
}
