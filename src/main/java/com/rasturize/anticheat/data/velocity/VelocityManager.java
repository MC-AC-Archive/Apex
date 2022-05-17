package com.rasturize.anticheat.data.velocity;

import lombok.Getter;
import lombok.val;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.function.Predicate;

public class VelocityManager {

    @Getter
    private final List<Velocity> velocities = new ArrayList<>();

    private final Predicate<Velocity> shouldRemoveVelocity = velocity -> {
        val now = System.currentTimeMillis();

        return velocity.getCreationTime() + 2000L < now;
    };

    /**
     * Adds a velocity entry to our velocity list
     *
     * @param x - The X distance of the velocity
     * @param y  - The absolute Y distance from the velocity
     * @param z - The Z distance of the velocity
     */
    public void addVelocityEntry(double x, double y, double z) {
        this.velocities.add(new Velocity(x * x + z * z, Math.abs(y)));
    }

    /**
     * Gets the max horizontal squared velocity distance
     *
     * @return - The highest horizontal velocity distance within 2 seconds (40 ticks)
     */
    public double getMaxHorizontal() {
        this.velocities.removeIf(shouldRemoveVelocity);

        try {
            return Math.sqrt(this.velocities.stream()
                    .mapToDouble(Velocity::getHorizontal)
                    .max()
                    .orElse(0.f));
        } catch (ConcurrentModificationException e) {
            return 1.0;
        }
    }

    /**
     * Gets the max vertical absolute velocity distance
     *
     * @return - The highest vertical velocity absolute distance within 2 seconds (40 ticks)
     */
    public double getMaxVertical() {
        this.velocities.removeIf(shouldRemoveVelocity);

        return this.velocities.stream()
                .mapToDouble(Velocity::getVertical)
                .max()
                .orElse(0.f);
    }

    /**
     * Gets the min horizontal squared velocity distance
     *
     * @return - The smallest horizontal velocity distance within 2 seconds (40 ticks)
     */
    public double getMinHorizontal() {
        this.velocities.removeIf(shouldRemoveVelocity);

        try {
            return Math.sqrt(this.velocities.stream()
                    .mapToDouble(Velocity::getHorizontal)
                    .min()
                    .orElse(0.f));
        } catch (ConcurrentModificationException e) {
            return 1.0;
        }
    }

    /**
     * Gets the min vertical absolute velocity distance
     *
     * @return - The smallest vertical velocity absolute distance within 2 seconds (40 ticks)
     */
    public double getMinVertical() {
        this.velocities.removeIf(shouldRemoveVelocity);

        return this.velocities.stream()
                .mapToDouble(Velocity::getVertical)
                .min()
                .orElse(0.f);
    }
}
