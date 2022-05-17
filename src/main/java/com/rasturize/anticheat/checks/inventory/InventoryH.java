package com.rasturize.anticheat.checks.inventory;

import com.rasturize.anticheat.api.check.Check;
import com.rasturize.anticheat.api.check.type.CheckType;
import com.rasturize.anticheat.protocol.packet.in.WrappedInFlyingPacket;
import com.rasturize.anticheat.protocol.packet.in.WrappedInWindowClickPacket;
import org.bukkit.Material;

@CheckType(id = "inventory:h", name = "Inventory H", type = CheckType.Type.INVENTORY)
public class InventoryH extends Check {

	void check(WrappedInWindowClickPacket wrappedInWindowClickPacket) {
		if (!canCheckMovement())
			return;

		WrappedInWindowClickPacket.ClickType c = wrappedInWindowClickPacket.getAction();

		if ((wrappedInWindowClickPacket.getItem() != null || c.isShiftClick()) && playerData.movement.deltaH > playerData.movement.lastDeltaH) {
			if (fail(6, 20 * 5, "C")) {
				player.closeInventory();
			}
		}

		if (wrappedInWindowClickPacket.getItem() != null) {
			if (player.getLocation().getBlock().getType() == Material.PORTAL || player.getLocation().add(0, 1, 0).getBlock().getType() == Material.PORTAL) {
				if (fail(6, 20 * 5, "P")) {
					player.closeInventory();
				}
			}
		}
	}

	void check(WrappedInFlyingPacket packet) {
		if (!canCheckMovement())
			return;

		if (playerData.movement.deltaH > playerData.movement.lastDeltaH && isUsingTheCursor()) {
			if (fail(6, 20 * 5, "M")) {
				player.closeInventory();
			}
		}
	}

	private boolean isUsingTheCursor() {
		return player != null && player.getOpenInventory().getCursor().getType() != Material.AIR;
	}
}
