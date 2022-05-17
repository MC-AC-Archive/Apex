package com.rasturize.anticheat.protocol.packet.in;

import com.rasturize.anticheat.protocol.api.NMSObject;
import com.rasturize.anticheat.protocol.api.ProtocolVersion;
import lombok.Getter;
import org.bukkit.entity.Player;

@Getter
public class WrappedInSteerVehiclePacket extends NMSObject {
    private static final String packet = Client.STEER_VEHICLE;

    // Fields

    // Decoded data


    public WrappedInSteerVehiclePacket(Object packet) {
        super(packet);
    }

    @Override
    public void process(Player player, ProtocolVersion version) {

    }
}
