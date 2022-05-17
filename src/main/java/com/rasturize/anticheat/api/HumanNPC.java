package com.rasturize.anticheat.api;

import com.mojang.authlib.GameProfile;
import com.rasturize.anticheat.data.playerdata.PlayerData;
import com.rasturize.anticheat.handler.TinyProtocolHandler;
import com.rasturize.anticheat.utils.EntityIdCache;
import com.rasturize.anticheat.utils.PreDefined;
import com.rasturize.anticheat.protocol.packet.out.WrappedOutEntityDestroy;
import com.rasturize.anticheat.protocol.packet.out.WrappedOutEntityTeleport;
import com.rasturize.anticheat.protocol.packet.out.WrappedOutNamedEntitySpawn;
import com.rasturize.anticheat.protocol.packet.out.WrappedOutRelativePosition;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Location;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class HumanNPC {
	private final int id = EntityIdCache.getNextId();
	private UUID uuid;
	private String name;

	public void spawn(PlayerData playerData, Location loc) {
		TinyProtocolHandler.instance.sendPacket(playerData.player,
				new WrappedOutNamedEntitySpawn(
						playerData.protocolVersion,
						id, new GameProfile(uuid, name),
						loc.getX(), loc.getY(), loc.getZ(),
						PreDefined.emptyDatawatcher, PreDefined.emptyEntityWatchables
				)
		);
	}

	public void moveEntity(PlayerData playerData, WrappedOutRelativePosition packet) {
		packet.setId(id);
		TinyProtocolHandler.sendPacket(playerData.player, packet);
	}

	public void teleportEntity(PlayerData playerData, WrappedOutEntityTeleport packet) {
		packet.setId(id);
		TinyProtocolHandler.sendPacket(playerData.player, packet);
	}

	public void destroyEntity(PlayerData playerData) {
		TinyProtocolHandler.sendPacket(playerData.player, new WrappedOutEntityDestroy(new int[]{id}));
	}
}
