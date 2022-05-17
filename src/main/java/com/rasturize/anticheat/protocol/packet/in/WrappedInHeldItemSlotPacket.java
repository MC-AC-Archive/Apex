package com.rasturize.anticheat.protocol.packet.in;

import com.rasturize.anticheat.protocol.api.NMSObject;
import com.rasturize.anticheat.protocol.api.ProtocolVersion;
import com.rasturize.anticheat.protocol.reflection.FieldAccessor;
import lombok.Getter;
import org.bukkit.entity.Player;

@Getter
public class WrappedInHeldItemSlotPacket extends NMSObject {
    private static final String packet = Client.HELD_ITEM;

    // Fields
    private static FieldAccessor<Integer> fieldHeldSlot = fetchField(packet, int.class, 0);

    // Decoded data
    private int slot;


    public WrappedInHeldItemSlotPacket(Object packet) {
        super(packet);
    }

    @Override
    public void process(Player player, ProtocolVersion version) {
        slot = fetch(fieldHeldSlot);
    }
}
