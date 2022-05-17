package com.rasturize.anticheat.checks.reach;

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

@CheckType(id = "reach:a", name = "Reach A", type = CheckType.Type.COMBAT, maxVl = 5)
public class Reach extends Check {

    @Setting
    private static double banReach = ConfigValue.REACH_BAN.asDouble();
    @Setting
    private static double cancelReach = ConfigValue.REACH_CANCEL.asDouble();
    @Setting
    private static int pingSensitivity = 150;
    @Setting
    private static int streakSensitivity = 2;

    private double reachStreak = 0;
    private double trust = 1;

    void check(WrappedInArmAnimationPacket wrappedInArmAnimationPacket) {
        reachStreak = Math.max(0, reachStreak - 0.1);
        trust = Math.min(1, trust + 0.005);
    }


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

                double lowest = 0;
                int collided = 0;

                for (Pair<SimpleCollisionBox, Double> pair : boxes) {
                    Pair<Double, Double> collision = new Pair<>();

                    if (RayCollision.intersect(ray, pair.getX(), collision)) {
                        double reach = collision.getX() - 0.0625;
                        reach -= targetData.velocity.velocityManager.getMinHorizontal();
                        lowest = lowest == 0 ? reach : Math.min(lowest, reach);
                        collided++;
                    }
                }

                if (lowest > 3.0) {
                    reachStreak = Math.min(streakSensitivity + 2, reachStreak + 1);

                    if (reachStreak >= streakSensitivity) {
                        if (lowest > banReach)
                            fail("r=%.3f,b=%d,l=%d,c=%d", lowest, boxes.size(), locations.size(), collided);
                        if (lowest > cancelReach) trust -= 0.5;
                    }
                } else {
                    reachStreak = Math.max(0, reachStreak - 0.1);
                    trust = Math.min(1, trust + 0.005);
                }
            }
        }
    }

    private static SimpleCollisionBox getHitbox(SimpleLocation simpleLocation) {
        return new SimpleCollisionBox().offset(simpleLocation.getX(), simpleLocation.getY(), simpleLocation.getZ()).expand(.3, 0, .3)
                .expandMax(0, 1.85, 0);
    }
}
