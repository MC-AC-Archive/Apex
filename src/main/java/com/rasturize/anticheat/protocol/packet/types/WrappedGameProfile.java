

package com.rasturize.anticheat.protocol.packet.types;

import com.rasturize.anticheat.protocol.api.NMSObject;
import com.rasturize.anticheat.protocol.api.ProtocolVersion;
import com.rasturize.anticheat.protocol.reflection.FieldAccessor;
import com.rasturize.anticheat.protocol.reflection.Reflection;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.UUID;

@Getter
public class WrappedGameProfile extends NMSObject {
    private static final String type = Type.GAMEPROFILE;

    // Fields
    private static FieldAccessor<UUID> fieldId = fetchField(type, UUID.class, 0);
    private static FieldAccessor<String> fieldName = fetchField(type, String.class, 0);
    private static FieldAccessor<?> fieldPropertyMap = fetchField(type, Reflection.getClass(Type.PROPERTYMAP), 0);

    // Decoded data
    public UUID id;
    public String name;
    public Object propertyMap;

    public WrappedGameProfile(Object type) {
        super(type);
    }

    @Override
    public void process(Player player, ProtocolVersion version) {
        id = fieldId.get(getObject());
        name = fieldName.get(getObject());
        propertyMap = fieldPropertyMap.get(getObject());
    }
}
