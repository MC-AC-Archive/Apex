package com.rasturize.anticheat.checks.badpackets;

import com.rasturize.anticheat.api.check.Check;
import com.rasturize.anticheat.api.check.type.CheckType;
import com.rasturize.anticheat.protocol.packet.in.WrappedInUseEntityPacket;
import com.rasturize.anticheat.protocol.packet.types.Vec3D;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

@CheckType(id = "badpackets:j", name = "BadPackets J", type = CheckType.Type.BADPACKET, maxVl = 5)
public class BadPacketsJ extends Check {

    void check(WrappedInUseEntityPacket wrappedInUseEntityPacket) {
        if (!canCheck())
            return;

        if (wrappedInUseEntityPacket.getAction() == WrappedInUseEntityPacket.EnumEntityUseAction.INTERACT_AT) {
            Entity entity = wrappedInUseEntityPacket.getEntity();

            if (entity instanceof Player) {
                Vec3D vec3D = wrappedInUseEntityPacket.getVector();

                if ((Math.abs(vec3D.a) > 0.41 || Math.abs(vec3D.b) > 1.91 || Math.abs(vec3D.c) > 0.41)) {
                    if (fail())
                        wrappedInUseEntityPacket.setCancelled(true);
                }
            }
        }
    }
}
