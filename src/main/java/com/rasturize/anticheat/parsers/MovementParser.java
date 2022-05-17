package com.rasturize.anticheat.parsers;

import com.rasturize.anticheat.api.check.wrapper.CheckWrapper;
import com.rasturize.anticheat.api.check.Check;
import com.rasturize.anticheat.api.check.Priority;
import com.rasturize.anticheat.api.check.type.Parser;
import com.rasturize.anticheat.data.TimedLocation;
import com.rasturize.anticheat.data.playerdata.PlayerData;
import com.rasturize.anticheat.utils.Helper;
import com.rasturize.anticheat.utils.Pair;
import com.rasturize.anticheat.utils.world.CollisionHandler;
import com.rasturize.anticheat.utils.world.Material2;
import com.rasturize.anticheat.utils.world.types.SimpleCollisionBox;
import com.rasturize.anticheat.protocol.api.ProtocolVersion;
import com.rasturize.anticheat.protocol.packet.in.WrappedInFlyingPacket;
import com.rasturize.anticheat.protocol.packet.out.WrappedOutVelocityPacket;
import com.rasturize.anticheat.protocol.reflection.FieldAccessor;
import com.rasturize.anticheat.protocol.reflection.Reflection;
import com.rasturize.anticheat.utils.Materials;
import com.rasturize.anticheat.utils.MathUtils;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.ArrayList;
import java.util.List;

@Parser
public class MovementParser extends Check {
	private FieldAccessor<Boolean> fieldCheckMovement = Reflection.getField("{nms}.PlayerConnection", "checkMovement", boolean.class);

	@Override
	public void init(PlayerData playerData, CheckWrapper wrapper) {
		super.init(playerData, wrapper);
	}

	@Priority(-5)
	void parse(WrappedInFlyingPacket packet) {
		playerData.lag.packetDelay = (int) (System.currentTimeMillis() - playerData.lag.lastPacket);
		playerData.lag.lastPacket = System.currentTimeMillis();

		boolean checkMovement = fieldCheckMovement.get(playerData.playerConnection);

		if (checkMovement)
			playerData.movementTicks++;
		else playerData.movementTicks = 0;

		long offset = 50 - playerData.lag.packetDelay;

		playerData.lag.currentTime += playerData.lag.packetDelay;
		playerData.lag.differencial += offset;

		if (playerData.protocolVersion.isAbove(ProtocolVersion.V1_9)) {
			if (playerData.lag.packetDelay < 40) {
				playerData.lag.packetSkips += 2;
				playerData.lag.packetSkips = Math.max(playerData.lag.packetSkips, 5);
			}
		} else {
			if (playerData.lag.packetDelay > 75) {
				playerData.lag.packetSkips += (playerData.lag.packetDelay / 50) * 2;
				playerData.lag.packetSkips = Math.max(playerData.lag.packetSkips, 5);
			}
		}

		playerData.lag.packetSkips = Math.min(playerData.lag.packetSkips, 40);

		playerData.state.isTeleporting = playerData.movementTicks <= 0;

		if (!playerData.state.isTeleporting) {
			if (packet.isPos()) {
				playerData.state.isSettingBack = false;
				playerData.currentTick++;

				playerData.movement.fx = playerData.movement.tx;
				playerData.movement.fy = playerData.movement.ty;
				playerData.movement.fz = playerData.movement.tz;

				playerData.movement.tx = packet.getX();
				playerData.movement.ty = packet.getY();
				playerData.movement.tz = packet.getZ();

				if (playerData.velocity.velocityY > 0.0 && playerData.movement.ty > playerData.movement.fy) {
					playerData.velocity.velocityY = 0.0;
				}


				playerData.movement.lastDeltaH = playerData.movement.deltaH;
				playerData.movement.lastDeltaV = playerData.movement.deltaV;
				playerData.movement.deltaH = Math.hypot(playerData.movement.tx - playerData.movement.fx, playerData.movement.tz - playerData.movement.fz);
				playerData.movement.deltaV = playerData.movement.ty - playerData.movement.fy;

				if (playerData.locations.size() != 1)
					playerData.locations.removeIf(pair -> System.currentTimeMillis() - pair.getX().getTime() > 1000);

				synchronized (playerData.locations) {
					playerData.locations.add(new Pair<>(new TimedLocation(playerData), playerData.movement.deltaH + Math.abs(playerData.movement.deltaV)));
				}

				parseEnvironment();

				if (playerData.lag.packetSkips > 0)
					playerData.lag.packetSkips--;
			}

			if (packet.isLook()) {
				playerData.movement.fyaw = playerData.movement.tyaw;
				playerData.movement.fpitch = playerData.movement.tpitch;

				playerData.movement.tyaw = packet.getYaw();
				playerData.movement.tpitch = packet.getPitch();

				float yawDelta = MathUtils.getDistanceBetweenAngles(playerData.movement.tyaw, playerData.movement.fyaw);
				float pitchDelta = MathUtils.getDistanceBetweenAngles(playerData.movement.tpitch, playerData.movement.fpitch);

				playerData.movement.yawDifference = Math.abs(yawDelta - playerData.movement.yawDelta);
				playerData.movement.pitchDifference = Math.abs(pitchDelta - playerData.movement.pitchDelta);

				playerData.movement.lastYawDelta = playerData.movement.yawDelta;
				playerData.movement.lastPitchDelta = playerData.movement.pitchDelta;

				playerData.movement.yawDelta = yawDelta;
				playerData.movement.pitchDelta = pitchDelta;
			}
		}
	}

	void parse(WrappedOutVelocityPacket wrappedOutVelocityPacket) {
		double velocityX = wrappedOutVelocityPacket.getX(),
				velocityY = wrappedOutVelocityPacket.getY(),
				velocityZ = wrappedOutVelocityPacket.getZ();

		if (velocityY > 0.0 && player.getLocation().getY() % 1.0 == 0.0 && playerData.enviroment.onGround) {
			playerData.velocity.velocityX = velocityX;
			playerData.velocity.velocityY = velocityY;
			playerData.velocity.velocityZ = velocityZ;
		}
		playerData.velocity.velocityManager.addVelocityEntry(velocityX,velocityY, velocityZ);
	}

	void parse(PlayerRespawnEvent event) {
		moveTo(event.getRespawnLocation());
	}

	void parse(PlayerTeleportEvent event) {
		if (!event.isCancelled()) {
			moveTo(event.getTo());
		}
	}

	private void moveTo(Location loc) {
		playerData.movementTicks = 0;

		playerData.lag.packetDelay = 50;

		playerData.state.isTeleporting = false;

		playerData.state.isInventoryOpen = false;

		playerData.state.isPlacing = false;
		playerData.state.isDigging = false;

		playerData.movement.fx = loc.getX();
		playerData.movement.fy = loc.getY();
		playerData.movement.fz = loc.getZ();

		playerData.movement.tx = loc.getX();
		playerData.movement.ty = loc.getY();
		playerData.movement.tz = loc.getZ();

		playerData.movement.fyaw = loc.getYaw();
		playerData.movement.fpitch = loc.getPitch();

		playerData.movement.tyaw = loc.getYaw();
		playerData.movement.tpitch = loc.getPitch();

		playerData.movement.lastDeltaH = 0.0;
		playerData.movement.lastDeltaV = 0.0;

		playerData.movement.deltaH = 0.0;
		playerData.movement.deltaV = 0.0;

		parseEnvironment();
	}

	private void parseEnvironment() {
		if (playerData.movement.deltaH == 0.0 && playerData.movement.deltaV == 0.0)
			return;

		World world = player.getWorld();

		int startX = Location.locToBlock(playerData.movement.tx - 0.3 - playerData.movement.deltaH);
		int endX = Location.locToBlock(playerData.movement.tx + 0.3 + playerData.movement.deltaH);

		int startY = Location.locToBlock(playerData.movement.ty - 0.51 + playerData.movement.deltaV);
		int endY = Location.locToBlock(playerData.movement.ty + 1.99 + playerData.movement.deltaV);

		int startZ = Location.locToBlock(playerData.movement.tz - 0.3 - playerData.movement.deltaH);
		int endZ = Location.locToBlock(playerData.movement.tz + 0.3 + playerData.movement.deltaH);

		List<Block> blocks = new ArrayList<>();

		int it = 9 * 9;

		start:

		for (int chunkx = startX >> 4; chunkx <= endX >> 4; ++chunkx) {
			int cx = chunkx << 4;

			for (int chunkz = startZ >> 4; chunkz <= endZ >> 4; ++chunkz) {
				if (!world.isChunkLoaded(chunkx, chunkz)) {
					continue;
				}

				Chunk chunk = world.getChunkAt(chunkx, chunkz);

				if (chunk != null) {
					int cz = chunkz << 4;
					int xStart = startX < cx ? cx : startX;
					int xEnd = endX < cx + 16 ? endX : cx + 16;
					int zStart = startZ < cz ? cz : startZ;
					int zEnd = endZ < cz + 16 ? endZ : cz + 16;

					for (int x = xStart; x <= xEnd; ++x) {
						for (int z = zStart; z <= zEnd; ++z) {
							for (int y = startY < 0 ? 0 : startY; y <= endY; ++y) {
								if (it-- <= 0) {
									break start;
								}

								Block block = chunk.getBlock(x & 15, y, z & 15);

								if (block.getType() != Material.AIR) {
									blocks.add(block);
								}
							}
						}
					}
				}
			}
		}

		playerData.enviroment.wasOnGround = playerData.enviroment.onGround;
		playerData.enviroment.wasBelowBlock = playerData.enviroment.belowBlock;
		playerData.enviroment.wasInLiquid = playerData.enviroment.inLiquid;
		playerData.enviroment.wasOnLadder = playerData.enviroment.onLadder;
		playerData.enviroment.wasWeb = playerData.enviroment.inWeb;
		playerData.enviroment.wasOnSlime = playerData.enviroment.onSlime;
		playerData.enviroment.wasOnIce = playerData.enviroment.onIce;
		playerData.enviroment.wasOnSoulSand = playerData.enviroment.onSoulSand;
		playerData.enviroment.wasOnWeirdBlock = playerData.enviroment.onWeirdBlock;

		playerData.enviroment.onGround = false;
		playerData.enviroment.belowBlock = false;
		playerData.enviroment.inLiquid = false;
		playerData.enviroment.onLadder = false;
		playerData.enviroment.inWeb = false;
		playerData.enviroment.onSlime = false;
		playerData.enviroment.onIce = false;
		playerData.enviroment.onSoulSand = false;
		playerData.enviroment.onWeirdBlock  = false;

		CollisionHandler handler = new CollisionHandler(blocks, player.getNearbyEntities(1 + playerData.movement.deltaH, 2 + playerData.movement.deltaV, 1 + playerData.movement.deltaH), playerData);

		handler.setSize(0.6, 0.5);
		handler.setOffset(-0.49);

		playerData.enviroment.onGround = handler.isCollidedWith(Materials.SOLID) || handler.contains(EntityType.BOAT);

		if (handler.isCollidedWith(Materials.STAIRS | Materials.SLABS | Materials.FENCE))
			playerData.enviroment.onWeirdBlock = true;

		handler.setSingle(true);

		if (handler.isCollidedWith(Materials.ICE))
			playerData.enviroment.onIce = true;
		if (handler.isCollidedWith(Material.SOUL_SAND))
			playerData.enviroment.onSoulSand = true;
		if (handler.isCollidedWith(Material2.SLIME_BLOCK))
			playerData.enviroment.onSlime = true;

		handler.setSingle(false);
		handler.setOffset(0);

		handler.setSize(0.6, 1.8);

		boolean lava = handler.isCollidedWith(Materials.LAVA);
		boolean water = handler.isCollidedWith(Materials.WATER);

		if (lava || water) {
			playerData.enviroment.inLiquid = true;
		}

		if (handler.isCollidedWith(Material.WEB))
			playerData.enviroment.inWeb = true;

		handler.setSize(2.0, 0.0);

		handler.setSingle(true);

		if (handler.isCollidedWith(Materials.LADDER))
			playerData.enviroment.onLadder = true;

		handler.setSingle(false);

		handler.setSize(0.6, 2.01);

		if (handler.isCollidedWith(Materials.SOLID))
			playerData.enviroment.belowBlock = true;

		SimpleCollisionBox box = Helper.getMovementHitbox(player);

		box.expand(Math.abs(playerData.movement.fx - playerData.movement.tx) + 0.1, -0.1, Math.abs(playerData.movement.fz - playerData.movement.tz) + 0.1);

		if (!Helper.blockCollisions(handler.getBlocks(), box).isEmpty()) playerData.enviroment.collidedHorizontally = true;

		box = Helper.getMovementHitbox(player);
		box.expand(0, 0.1, 0);

		if (!Helper.blockCollisions(handler.getBlocks(), box).isEmpty())
			playerData.enviroment.collidedVertically = true;

		playerData.enviroment.lastHandler = playerData.enviroment.handler;
		playerData.enviroment.handler = handler;
	}
}
