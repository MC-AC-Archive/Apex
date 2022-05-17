package com.rasturize.anticheat.data;

import com.rasturize.anticheat.data.playerdata.PlayerData;
import lombok.Getter;

@Getter
public class TimedLocation extends SimpleLocation {
    long time;

    public TimedLocation(double x, double y, double z) {
        super(x, y, z, 0, 0);
        this.time = System.currentTimeMillis();
    }

    public TimedLocation(double x, double y, double z, float yaw, float pitch, long time) {
        super(x, y, z, yaw, pitch);
        this.time = time;
    }

    public TimedLocation(PlayerData playerData) {
        this(playerData.movement.tx, playerData.movement.ty, playerData.movement.tz, playerData.movement.tyaw, playerData.movement.tpitch, System.currentTimeMillis());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TimedLocation) {
            TimedLocation o = (TimedLocation) obj;
            return o.getX() == x && o.getY() == y && o.getZ() == z && o.getYaw() == yaw && o.getPitch() == pitch;
        } else return super.equals(obj);
    }
}
