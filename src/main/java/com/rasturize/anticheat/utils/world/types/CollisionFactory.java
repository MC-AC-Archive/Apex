package com.rasturize.anticheat.utils.world.types;

import com.rasturize.anticheat.utils.world.CollisionBox;
import com.rasturize.anticheat.protocol.api.ProtocolVersion;
import org.bukkit.block.Block;

public interface CollisionFactory {
    CollisionBox fetch(ProtocolVersion version, Block block);
}