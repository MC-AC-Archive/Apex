package com.rasturize.anticheat.utils.world;

import com.rasturize.anticheat.data.SimpleLocation;
import com.rasturize.anticheat.data.playerdata.PlayerData;
import com.rasturize.anticheat.utils.world.types.SimpleCollisionBox;
import com.rasturize.anticheat.protocol.api.ProtocolVersion;
import com.rasturize.anticheat.utils.Materials;
import com.rasturize.anticheat.utils.Utils;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import java.util.List;

@Getter
public class CollisionHandler {
	private List<Block> blocks;
	private List<Entity> entities;
	private SimpleLocation location;
	private PlayerData playerData;

	private double width, height;
	private double shift;
	@Setter
	private boolean single = false;
	@Setter
	private boolean debugging;

	public CollisionHandler(List<Block> blocks, List<Entity> entities, PlayerData playerData) {
		this.blocks = blocks;
		this.entities = entities;
		this.location = new SimpleLocation(playerData);
		this.playerData = playerData;
	}

	public void setSize(double width, double height) {
		this.width = width;
		this.height = height;
	}

	public void setOffset(double shift) {
		this.shift = shift;
	}

	public boolean containsFlag(int bitmask) {
		for (Block b : blocks) {
			if (Materials.checkFlag(b.getType(), bitmask)) return true;
		}
		return false;
	}

	public boolean contains(EntityType type) {
		return entities.stream().anyMatch(e -> e.getType() == type);
	}

	public boolean isCollidedWith(int bitmask) {
		SimpleCollisionBox playerBox = new SimpleCollisionBox()
				.offset(location.getX(), location.getY(), location.getZ())
				.expandMin(0, shift, 0)
				.expandMax(0, height, 0)
				.expand(width / 2, 0, width / 2);

		for (Block b : blocks) {
			Location block = b.getLocation();
			if (Materials.checkFlag(b.getType(), bitmask) && (!single || (block.getBlockX() == location.getBlockX() && block.getBlockZ() == location.getBlockZ()))) {
				if (BlockData.getData(b.getType()).getBox(b, ProtocolVersion.getGameVersion()).isCollided(playerBox))
					return true;
			}
		}

		return false;
	}

	public boolean isCollidedWith(CollisionBox box) {
		SimpleCollisionBox playerBox = new SimpleCollisionBox()
				.offset(location.getX(), location.getY(), location.getZ())
				.expandMin(0, shift, 0)
				.expandMax(0, height, 0)
				.expand(width / 2, 0, width / 2);

		return box.isCollided(playerBox);
	}

	public boolean isCollidedWith(Material... materials) {
		SimpleCollisionBox playerBox = new SimpleCollisionBox()
				.offset(location.getX(), location.getY(), location.getZ())
				.expandMin(0, shift, 0)
				.expandMax(0, height, 0)
				.expand(width / 2, 0, width / 2);

		for (Block b : blocks) {
			Location block = b.getLocation();
			if (Utils.contains(materials, b.getType()) && (!single || (block.getBlockX() == location.getBlockX() && block.getBlockZ() == location.getBlockZ()))) {
				if (BlockData.getData(b.getType()).getBox(b, ProtocolVersion.getGameVersion()).isCollided(playerBox))
					return true;
			}
		}

		return false;
	}
}
