package com.rasturize.anticheat.checks.omnisprint;

import com.rasturize.anticheat.api.check.Check;
import com.rasturize.anticheat.api.check.type.CheckType;
import com.rasturize.anticheat.data.TimedLocation;
import com.rasturize.anticheat.protocol.packet.in.WrappedInFlyingPacket;
import com.rasturize.anticheat.utils.Pair;
import com.rasturize.anticheat.utils.Utils;
import org.bukkit.Bukkit;

@CheckType(id = "omnisprint:a", name = "OmniSprint A", type = CheckType.Type.MOVEMENT, maxVl = 2)
public class OmniSprint extends Check {

    void check(WrappedInFlyingPacket wrappedInFlyingPacket) {
        if (!wrappedInFlyingPacket.isPos() || !canCheck() || !canCheckMovement())
            return;

        if (this.getPacketOffset() < 5 && playerData.locations.size() > 3) {
            Pair<TimedLocation, Double> current = playerData.locations.get(playerData.locations.size() - 1);
            Pair<TimedLocation, Double> latter = playerData.locations.get(playerData.locations.size() - 2);

            double angle = Math.abs(Utils.getAngle(current.getX().toLocation(player.getWorld()), latter.getX().toLocation(player.getWorld())));

            if (angle < 100.0d && playerData.movement.deltaH > playerData.movement.lastDeltaH) {
                double horizontalMove = (playerData.movement.deltaH - playerData.movement.lastDeltaH) - playerData.movement.lastMoveSpeed;

                double omniSprintMove = horizontalMove + (playerData.movement.lastMoveSpeed * 0.6);
                omniSprintMove -= playerData.velocity.velocityManager.getMaxHorizontal();

                if (omniSprintMove > -0.1 && omniSprintMove < -0.041) {
                    fail("%.2f,a=%.2f", omniSprintMove, angle);
                }
            }
        }
    }
}
