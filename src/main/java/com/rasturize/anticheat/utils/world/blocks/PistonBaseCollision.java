package com.rasturize.anticheat.utils.world.blocks;

import com.rasturize.anticheat.utils.world.CollisionBox;
import com.rasturize.anticheat.utils.world.types.CollisionFactory;
import com.rasturize.anticheat.utils.world.types.SimpleCollisionBox;
import com.rasturize.anticheat.protocol.api.ProtocolVersion;
import org.bukkit.block.Block;

public class PistonBaseCollision implements CollisionFactory {
    @Override
    public CollisionBox fetch(ProtocolVersion version, Block block) {
        byte data = block.getState().getData().getData();

        if ((data & 8) != 0) {
            switch (data & 7) {
                case 0:
                    return new SimpleCollisionBox(0.0F, 0.25F, 0.0F, 1.0F, 1.0F, 1.0F);
                case 1:
                    return new SimpleCollisionBox(0.0F, 0.0F, 0.0F, 1.0F, 0.75F, 1.0F);
                case 2:
                    return new SimpleCollisionBox(0.0F, 0.0F, 0.25F, 1.0F, 1.0F, 1.0F);
                case 3:
                    return new SimpleCollisionBox(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 0.75F);
                case 4:
                    return new SimpleCollisionBox(0.25F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
                case 5:
                    return new SimpleCollisionBox(0.0F, 0.0F, 0.0F, 0.75F, 1.0F, 1.0F);
            }
        } else {
            return new SimpleCollisionBox(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        }
        return null;
    }
}