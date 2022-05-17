package com.rasturize.anticheat.checks.flight;

import com.rasturize.anticheat.api.check.type.CheckType;
import com.rasturize.anticheat.api.check.Check;
import com.rasturize.anticheat.protocol.packet.in.WrappedInFlyingPacket;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

@CheckType(id = "flight:a", name = "Flight A", type = CheckType.Type.MOVEMENT)
public class Flight extends Check {
    private double jumpLimit, jumpMultiplier, fallSpeedLimit;
    private int fallViolations;

    void check(WrappedInFlyingPacket wrappedInFlyingPacket) {
        if (!wrappedInFlyingPacket.isPos() || !canCheck() || isGliding() || playerData.enviroment.lastHandler == null) return;

        double deltaY = playerData.movement.ty - playerData.movement.fy;

        if (player.isFlying() || player.isInsideVehicle() || playerData.velocity.velocityManager.getMaxVertical() > 0.0 || playerData.enviroment.onGround || playerData.enviroment.inLiquid || playerData.enviroment.onLadder) {
            this.resetLimits(player);
            return;
        }

        if (playerData.enviroment.wasOnGround) {
            this.resetLimits(player);
        }

        if (playerData.enviroment.wasInLiquid) {
            this.jumpLimit += 0.18;
        }


        if (deltaY > 0.0) {
            if (deltaY > this.jumpLimit + playerData.velocity.velocityManager.getMaxVertical()) {
                fail("t=jump,d=%.5f,l=%.5f", deltaY, jumpLimit + playerData.velocity.velocityManager.getMaxVertical());

                this.jumpLimit *= this.jumpMultiplier;
            } else {
                this.jumpMultiplier -= 0.025;
            }
        } else if (deltaY < 0.0) {
            if (this.fallSpeedLimit - deltaY < 0.2 && ++this.fallViolations == 6) {
                fail("t=glide,fsl=%.5f,fv=%.5f", deltaY, this.fallSpeedLimit - deltaY, this.fallViolations);

                this.resetLimits(player);
            }

            this.fallSpeedLimit -= 0.01;
        } else if (deltaY == 0.0 && ++this.fallViolations == 5) {
            fail("t=hover");

            this.resetLimits(player);
        }

    }

    private void resetLimits(Player player) {
        int jumpAmplifier = this.getJumpBoostAmplifier(player);

        this.jumpMultiplier = 0.8 + 0.03 * jumpAmplifier;
        this.jumpLimit = 0.42 + 0.11 * jumpAmplifier;

        this.fallSpeedLimit = 0.078;
        this.fallViolations = 0;
    }

    private int getJumpBoostAmplifier(Player player) {
        if (player.hasPotionEffect(PotionEffectType.JUMP)) {
            return player.getActivePotionEffects().stream().filter(effect -> effect.getType().equals(PotionEffectType.JUMP)).findFirst().map(effect -> effect.getAmplifier() + 1).orElse(0);
        }
        return 0;
    }
}
