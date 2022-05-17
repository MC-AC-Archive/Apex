package com.rasturize.anticheat.checks.badpackets;

import com.rasturize.anticheat.api.check.Check;
import com.rasturize.anticheat.api.check.type.CheckType;
import com.rasturize.anticheat.protocol.packet.in.WrappedInWindowClickPacket;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

@CheckType(id = "badpackets:k", name = "BadPackets K", type = CheckType.Type.BADPACKET, maxVl = 2)
public class BadPacketsK extends Check {
    private int lastSlot;
    private double vl;
    private long lastClick, lastDelay;

    void check(WrappedInWindowClickPacket wrappedInWindowClickPacket) {
        ItemStack itemStack = wrappedInWindowClickPacket.getItem();

        if (itemStack.getType() == Material.POTION) { // We do not want stupid shit happening
            int slot = wrappedInWindowClickPacket.getSlot(),
                    slotChange = Math.abs(slot - lastSlot);

            long now = System.currentTimeMillis(),
                    delay = now - lastClick;

            long difference = Math.abs(delay - lastDelay);

            if (slotChange == 1 && delay == lastDelay) {
                vl += 2;

                if (vl > 2) {
                    fail("A");
                }
            }

            if (difference <= 4L && delay <= 110L) {
                vl += 0.5;

                if (vl > 2) {
                    fail("B");
                }
            } else {
                vl = Math.max(vl - 1, 0);
            }

            if (difference <= 4 && delay <= 110L && slotChange == 1) {
                vl += 2;

                if (vl > 2) {
                    fail("C");
                }
            }

                /*
                 Theory behind this is that you cannot have a big slot change and flag the difference check
                 As it would take around 3ms + the click time to change 4 slots
                  */
            if (difference <= 4 && delay <= 110L && slot >= 4) {
                vl += 2;

                if (vl > 3) {
                    fail("D");
                }
            }

            lastClick = now;
            lastSlot = slot;
            lastDelay = delay;
        }
    }
}
