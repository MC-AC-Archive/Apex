package com.rasturize.anticheat.protocol.packet.in;

import com.rasturize.anticheat.protocol.api.NMSObject;
import com.rasturize.anticheat.protocol.api.ProtocolVersion;
import com.rasturize.anticheat.protocol.reflection.FieldAccessor;
import lombok.Getter;
import org.bukkit.entity.Player;

@Getter
public class WrappedInCloseWindowPacket extends NMSObject {
    private static final String packet = Client.CLOSE_WINDOW;

    // Fields
    private static FieldAccessor<Integer> fieldId = fetchField(packet, int.class, 0);

    // Decoded data
    private int id;

    public WrappedInCloseWindowPacket(Object packet) {
        super(packet);
    }

    @Override
    public void process(Player player, ProtocolVersion version) {
        id = fetch(fieldId);
    }
}
