package com.rasturize.anticheat.protocol.packet.in;

import com.rasturize.anticheat.protocol.api.NMSObject;
import lombok.Getter;

@Getter
public class WrappedInArmAnimationPacket extends NMSObject {
    private static final String packet = Client.ARM_ANIMATION;
}
