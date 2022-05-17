package com.rasturize.anticheat.utils.world;

import com.rasturize.anticheat.utils.world.types.SimpleCollisionBox;
import com.rasturize.anticheat.utils.packet.WrappedPacketPlayOutWorldParticles;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;

public interface CollisionBox {
    boolean isCollided(CollisionBox other);
    void draw(WrappedPacketPlayOutWorldParticles.EnumParticle particle, Collection<? extends Player> players);
    CollisionBox copy();
    CollisionBox offset(double x, double y, double z);
    void downCast(List<SimpleCollisionBox> list);
    boolean isNull();
}