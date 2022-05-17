package com.rasturize.anticheat.utils.world.blocks;

import com.rasturize.anticheat.utils.world.types.ComplexCollisionBox;
import com.rasturize.anticheat.utils.world.types.SimpleCollisionBox;

import static com.rasturize.anticheat.utils.Utils.EIGHTH;

public class HopperBounding extends ComplexCollisionBox {

    public HopperBounding() {
        this.add(new SimpleCollisionBox(0,0,0,1, EIGHTH*5,1));
        double thickness = EIGHTH;
        this.add(new SimpleCollisionBox(0, EIGHTH*5, 0, thickness, 1, 1));
        this.add(new SimpleCollisionBox(1-thickness, EIGHTH*5, 0, 1, 1, 1));
        this.add(new SimpleCollisionBox(0, EIGHTH*5, 0, 1, 1, thickness));
        this.add(new SimpleCollisionBox(0, EIGHTH*5, 1-thickness, 1, 1, 1));
    }
}
