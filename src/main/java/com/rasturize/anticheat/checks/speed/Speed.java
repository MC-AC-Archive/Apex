package com.rasturize.anticheat.checks.speed;

import com.rasturize.anticheat.Apex;
import com.rasturize.anticheat.api.check.Check;
import com.rasturize.anticheat.api.check.type.CheckType;
import com.rasturize.anticheat.protocol.packet.in.WrappedInFlyingPacket;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

@CheckType(id = "speed:a", name = "Speed A", type = CheckType.Type.MOVEMENT, maxVl = 3)
public class Speed extends Check {

    private double friction = 0.0, previousHorizontal = 0.0;

    void check(WrappedInFlyingPacket wrappedInFlyingPacket) {
        if (!wrappedInFlyingPacket.isPos() || !canCheck() || !canCheckMovement() || isGliding())
            return;

        double deltaY = playerData.movement.deltaV;

        double horizontalSpeed = 1.f;
        double blockFriction = this.friction;

        boolean onGround = Apex.instance.getBridge().onGround(player);

        double speedLimit = playerData.enviroment.belowBlock ? 3.6 : 1.f;

        if (onGround) {
            blockFriction *= 0.91f;

            horizontalSpeed *= blockFriction > 0.708 ? 1.3 : 0.23315;
            horizontalSpeed *= 0.16277136 / Math.pow(blockFriction, 3);
            horizontalSpeed -= deltaY > 0.4199 ? -0.2 : 0.1;
        } else {
            horizontalSpeed = 0.026;
            blockFriction = 0.91f;
        }

        double previousHorizontal = this.previousHorizontal;
        double horizontalDistance = playerData.movement.deltaH;

        if (horizontalDistance > 0.27 && horizontalDistance - previousHorizontal > horizontalSpeed) {
            if (horizontalDistance - previousHorizontal > horizontalSpeed) {
                horizontalSpeed += playerData.velocity.velocityManager.getMaxHorizontal();

                double moveSpeed = (horizontalDistance - previousHorizontal) / horizontalSpeed;
                moveSpeed *= 0.98;

                if (player.hasPotionEffect(PotionEffectType.SPEED)) {
                    moveSpeed -= (this.getSpeedBoostAmplifier(player) + 1) * .17; //from minecraft src
                }

                moveSpeed -= playerData.velocity.velocityX + playerData.velocity.velocityY + playerData.velocity.velocityZ;

                if (moveSpeed > speedLimit) {
                    fail("ch+=%.0f", (moveSpeed - speedLimit));
                }
            }
        }

        this.previousHorizontal = horizontalDistance * blockFriction;
        this.friction = Apex.instance.getBridge().getBlockFriction(player);
    }

    private int getSpeedBoostAmplifier(Player player) {
        if (player.hasPotionEffect(PotionEffectType.SPEED)) {
            return player.getActivePotionEffects().stream().filter(effect -> effect.getType().equals(PotionEffectType.SPEED)).findFirst().map(effect -> effect.getAmplifier() + 1).orElse(0);
        }
        return 0;
    }
}
