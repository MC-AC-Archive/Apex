/*
 * Copyright (c) 2018 NGXDEV.COM. Licensed under MIT.
 */

package com.rasturize.anticheat.handler;

import com.rasturize.anticheat.Apex;
import com.rasturize.anticheat.api.HumanNPC;
import com.rasturize.anticheat.data.playerdata.PlayerData;
import com.rasturize.anticheat.protocol.api.AbstractTinyProtocol;
import com.rasturize.anticheat.protocol.api.NMSObject;
import com.rasturize.anticheat.protocol.api.ProtocolVersion;
import com.rasturize.anticheat.protocol.packet.in.*;
import com.rasturize.anticheat.protocol.packet.out.*;
import com.rasturize.anticheat.protocol.reflection.Reflection;
import com.rasturize.anticheat.protocol.version.v1_7_R4.TinyProtocolLegacy;
import com.rasturize.anticheat.protocol.version.v1_8_R3.TinyProtocol;
import com.rasturize.anticheat.utils.exception.ExceptionLog;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

public class TinyProtocolHandler {
	public static AbstractTinyProtocol instance;

	// Purely for making the code cleaner
	public static void sendPacket(Player player, Object packet) {
		instance.sendPacket(player, packet);
	}

	public static int getProtocolVersion(Player player) {
		return instance.getProtocolVersion(player);
	}

	public TinyProtocolHandler() {
		TinyProtocolHandler self = this;
		instance = ProtocolVersion.getGameVersion().isBelow(ProtocolVersion.V1_8_9) || Reflection.VERSION.equals("v1_8_R1") ? new TinyProtocolLegacy(Apex.instance) {
			@Override
			public Object onPacketOutAsync(Player receiver, Object packet) {
				return self.onPacketOutAsync(receiver, packet);
			}

			@Override
			public Object onPacketInAsync(Player sender, Object packet) {
				return self.onPacketInAsync(sender, packet);
			}
		} : new TinyProtocol(Apex.instance) {
			@Override
			public Object onPacketOutAsync(Player receiver, Object packet) {
				return self.onPacketOutAsync(receiver, packet);
			}

			@Override
			public Object onPacketInAsync(Player sender, Object packet) {
				return self.onPacketInAsync(sender, packet);
			}
		};
	}

	private Object onPacketOutAsync(Player receiver, Object packet) {
		if (receiver == null) return packet;
		
		boolean cancel = false;
		
		String name = packet.getClass().getName();
		int index = name.lastIndexOf(".");
		String packetName = name.substring(index + 1);
		
		try {
			PlayerData playerData = PlayerData.getData(receiver);
			switch (packetName) {
				case NMSObject.Server.KEEP_ALIVE: {
					WrappedOutKeepAlivePacket wrapped = new WrappedOutKeepAlivePacket(packet);
					wrapped.process(receiver, playerData.protocolVersion);

					playerData.fireChecks(wrapped);

					cancel = wrapped.isCancelled();
					break;
				}
				case NMSObject.Server.ENTITY_VELOCITY: {
					WrappedOutVelocityPacket wrapped = new WrappedOutVelocityPacket(packet);
					wrapped.process(receiver, playerData.protocolVersion);

					if (wrapped.getId() == receiver.getEntityId()) playerData.fireChecks(wrapped);

					cancel = wrapped.isCancelled();
					break;
				}
				case NMSObject.Server.GAME_STATE: {
					WrappedOutGameState wrapped = new WrappedOutGameState(packet);
					wrapped.process(receiver, playerData.protocolVersion);

					playerData.fireChecks(wrapped);

					playerData.gameMode = GameMode.getByValue((int) wrapped.getValue());

					cancel = wrapped.isCancelled();
					break;
				}
				case NMSObject.Server.ENTITY_TELEPORT: {
					WrappedOutEntityTeleport wrapped = new WrappedOutEntityTeleport(packet);
					playerData.npc.npcs.forEach(npc -> npc.teleportEntity(playerData, wrapped));

					break;
				}
				case NMSObject.Server.REL_LOOK:
				case NMSObject.Server.REL_POSITION:
				case NMSObject.Server.REL_POSITION_LOOK:
				case NMSObject.Server.LEGACY_REL_LOOK:
				case NMSObject.Server.LEGACY_REL_POSITION:
				case NMSObject.Server.LEGACY_REL_POSITION_LOOK: {
					WrappedOutRelativePosition wrapped = new WrappedOutRelativePosition(packet);

					playerData.npc.npcs.forEach(npc -> npc.moveEntity(playerData, wrapped));
					break;
				}
			}
		} catch (Exception e) {
			ExceptionLog.log(e);
		}
		return cancel ? null : packet;
	}

	private Object onPacketInAsync(Player sender, Object packet) {
		if (sender == null) return packet;

		boolean cancel = false;

		String name = packet.getClass().getName();
		int index = name.lastIndexOf(".");
		String packetName = name.substring(index + 1);

		try {
			PlayerData playerData = PlayerData.getData(sender);

			switch (packetName) {
				case NMSObject.Client.ARM_ANIMATION: {
					WrappedInArmAnimationPacket wrapped = new WrappedInArmAnimationPacket();
					wrapped.process(sender, playerData.protocolVersion);

					playerData.fireChecks(wrapped);

					cancel = wrapped.isCancelled();
					break;
				}
				case NMSObject.Client.BLOCK_DIG: {
					WrappedInBlockDigPacket wrapped = new WrappedInBlockDigPacket(packet);
					wrapped.process(sender, playerData.protocolVersion);

					playerData.fireChecks(wrapped);

					cancel = wrapped.isCancelled();
					break;
				}
				case NMSObject.Client.BLOCK_PLACE: {
					WrappedInBlockPlacePacket wrapped = new WrappedInBlockPlacePacket(packet);
					wrapped.process(sender, playerData.protocolVersion);

					playerData.fireChecks(wrapped);

					cancel = wrapped.isCancelled();
					break;
				}
				case NMSObject.Client.CLIENT_COMMAND: {
					WrappedInClientCommandPacket wrapped = new WrappedInClientCommandPacket(packet);
					wrapped.process(sender, playerData.protocolVersion);

					playerData.fireChecks(wrapped);

					cancel = wrapped.isCancelled();
					break;
				}
				case NMSObject.Client.CLOSE_WINDOW: {
					WrappedInCloseWindowPacket wrapped = new WrappedInCloseWindowPacket(packet);
					wrapped.process(sender, playerData.protocolVersion);

					playerData.fireChecks(wrapped);

					cancel = wrapped.isCancelled();
					break;
				}
				case NMSObject.Client.ENTITY_ACTION: {
					WrappedInEntityActionPacket wrapped = new WrappedInEntityActionPacket(packet);
					wrapped.process(sender, playerData.protocolVersion);

					playerData.fireChecks(wrapped);

					cancel = wrapped.isCancelled();
					break;
				}
				case NMSObject.Client.POSITION:
				case NMSObject.Client.LOOK:
				case NMSObject.Client.POSITION_LOOK:
				case NMSObject.Client.LEGACY_POSITION:
				case NMSObject.Client.LEGACY_LOOK:
				case NMSObject.Client.LEGACY_POSITION_LOOK:
				case NMSObject.Client.FLYING: {
					WrappedInFlyingPacket wrapped = new WrappedInFlyingPacket(packet);
					wrapped.process(sender, playerData.protocolVersion);

					playerData.fireChecks(wrapped);

					cancel = wrapped.isCancelled();
					break;
				}
				case NMSObject.Client.HELD_ITEM: {
					WrappedInHeldItemSlotPacket wrapped = new WrappedInHeldItemSlotPacket(packet);
					wrapped.process(sender, playerData.protocolVersion);

					playerData.fireChecks(wrapped);

					cancel = wrapped.isCancelled();
					break;
				}
				case NMSObject.Client.KEEP_ALIVE: {
					WrappedInKeepAlivePacket wrapped = new WrappedInKeepAlivePacket(packet);
					wrapped.process(sender, playerData.protocolVersion);

					playerData.fireChecks(wrapped);

					cancel = wrapped.isCancelled();
					break;
				}
				case NMSObject.Client.STEER_VEHICLE: {
					WrappedInSteerVehiclePacket wrapped = new WrappedInSteerVehiclePacket(packet);
					wrapped.process(sender, playerData.protocolVersion);

					playerData.fireChecks(wrapped);

					cancel = wrapped.isCancelled();
					break;
				}
				case NMSObject.Client.TRANSACTION: {
					WrappedInTransactionPacket wrapped = new WrappedInTransactionPacket(packet);
					wrapped.process(sender, playerData.protocolVersion);

					playerData.fireChecks(wrapped);

					cancel = wrapped.isCancelled();
					break;
				}
				case NMSObject.Client.USE_ENTITY: {
					WrappedInUseEntityPacket wrapped = new WrappedInUseEntityPacket(packet);
					wrapped.process(sender, playerData.protocolVersion);

					playerData.fireChecks(wrapped);

					cancel = wrapped.isCancelled();
					break;
				}
				case NMSObject.Client.WINDOW_CLICK: {
					WrappedInWindowClickPacket wrapped = new WrappedInWindowClickPacket(packet);
					wrapped.process(sender, playerData.protocolVersion);

					playerData.fireChecks(wrapped);

					cancel = wrapped.isCancelled();
					break;
				}
			}
		} catch (Exception e) {
			ExceptionLog.log(e);
		}
		return cancel ? null : packet;
	}
}
