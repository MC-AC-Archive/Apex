package com.rasturize.anticheat.checks.killaura;

import com.rasturize.anticheat.api.check.Check;
import com.rasturize.anticheat.api.check.Setting;
import com.rasturize.anticheat.api.check.type.CheckType;
import com.rasturize.anticheat.api.config.ConfigValue;
import com.rasturize.anticheat.data.SimpleLocation;
import com.rasturize.anticheat.data.TimedLocation;
import com.rasturize.anticheat.data.playerdata.PlayerData;
import com.rasturize.anticheat.protocol.packet.in.WrappedInArmAnimationPacket;
import com.rasturize.anticheat.protocol.packet.in.WrappedInUseEntityPacket;
import com.rasturize.anticheat.utils.Pair;
import com.rasturize.anticheat.utils.world.types.RayCollision;
import com.rasturize.anticheat.utils.world.types.SimpleCollisionBox;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

//@CheckType(id = "killaura:j", name = "KillAura J", type = CheckType.Type.COMBAT, maxVl = 5)
public class KillAuraJ extends Check {

    /*
    This checks if the attack collision has a collision on an over-expanded hitbox, this is not the best/most proper way to make a hitbox check, but it works pretty fine
    to my testing.
     */

    @Setting
    private static int pingSensitivity = 150;

    @Setting
    private static int streakSensitivity = 3;

    private int hitboxStreak;

    void check(WrappedInUseEntityPacket wrappedInUseEntityPacket) {
        if (wrappedInUseEntityPacket.getAction() == WrappedInUseEntityPacket.EnumEntityUseAction.ATTACK && wrappedInUseEntityPacket.getEntity() instanceof Player && playerData.gameMode != GameMode.CREATIVE) {
            PlayerData targetData = PlayerData.getData((Player) wrappedInUseEntityPacket.getEntity());

            synchronized (targetData.locations) {
                Location eyeLocation = new SimpleLocation(playerData).toLocation(player.getWorld()).add(0, 1.53, 0);
                RayCollision ray = new RayCollision(eyeLocation.toVector(), eyeLocation.getDirection());

                List<TimedLocation> locations = targetData.getEstimatedLocations(playerData.lag.currentTime, pingSensitivity + playerData.lag.differencial);
                List<Pair<SimpleCollisionBox, Double>> boxes = targetData.locations.stream()
                        .filter(pair -> locations.contains(pair.getX()))
                        .map(pair -> new Pair<>(getHitbox(pair.getX()), pair.getY()))
                        .collect(Collectors.toList());

                int collided = 0;

                for (Pair<SimpleCollisionBox, Double> pair : boxes) {
                    Pair<Double, Double> collision = new Pair<>();

                    if (RayCollision.intersect(ray, pair.getX(), collision)) {
                        collided++;
                    }
                }

                if (collided == 0) {
                    if (++hitboxStreak > streakSensitivity) {
                        fail("b=%d,l=%d,c=%d", boxes.size(), locations.size(), collided);
                    }
                } else {
                    hitboxStreak = 0;
                }
            }
        }
    }

    // Expand it a bit more than needed.
    private static SimpleCollisionBox getHitbox(SimpleLocation simpleLocation) {
        return new SimpleCollisionBox().offset(simpleLocation.getX(), simpleLocation.getY(), simpleLocation.getZ()).expand(.5, 0, .5)
                .expandMax(0, 1.85, 0);
    }
}
