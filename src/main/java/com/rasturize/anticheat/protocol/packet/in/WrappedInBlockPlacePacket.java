package com.rasturize.anticheat.protocol.packet.in;

import com.rasturize.anticheat.protocol.api.NMSObject;
import com.rasturize.anticheat.protocol.api.ProtocolVersion;
import com.rasturize.anticheat.protocol.packet.types.BaseBlockPosition;
import com.rasturize.anticheat.protocol.reflection.FieldAccessor;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@Getter
public class WrappedInBlockPlacePacket extends NMSObject {
    private static final String packet = Client.BLOCK_PLACE;

    // Fields
    private static FieldAccessor<Integer> fieldFace = fetchField(packet, int.class, 0);
    private static FieldAccessor<Object> fieldBlockPosition = fetchField(packet, Object.class, 1);
    private static FieldAccessor<Object> fieldItemStack = fetchField(packet, Object.class, 2);
    private static FieldAccessor<Float> fieldVecX = fetchField(packet, float.class, 0);
    private static FieldAccessor<Float> fieldVecY = fetchField(packet, float.class, 1);
    private static FieldAccessor<Float> fieldVecZ = fetchField(packet, float.class, 2);

    // Decoded data
    private int face;
    private ItemStack itemStack;
    private BaseBlockPosition position;
    private float vecX, vecY, vecZ;

    public WrappedInBlockPlacePacket(Object packet) {
        super(packet);
    }

    @Override
    public void process(Player player, ProtocolVersion version) {
        face = fetch(fieldFace);
        position = new BaseBlockPosition(fetch(fieldBlockPosition));
        itemStack = toBukkitStack(fetch(fieldItemStack));
        vecX = fetch(fieldVecX);
        vecY = fetch(fieldVecY);
        vecZ = fetch(fieldVecZ);
    }
}
