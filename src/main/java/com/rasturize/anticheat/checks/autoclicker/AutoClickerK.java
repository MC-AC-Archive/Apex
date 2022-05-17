package com.rasturize.anticheat.checks.autoclicker;

import com.rasturize.anticheat.api.check.Check;
import com.rasturize.anticheat.api.check.type.CheckType;
import com.rasturize.anticheat.protocol.packet.in.WrappedInArmAnimationPacket;
import com.rasturize.anticheat.protocol.packet.in.WrappedInFlyingPacket;

import java.util.ArrayList;
import java.util.List;

@CheckType(id = "autoclicker:k", name = "AutoClicker K", type = CheckType.Type.AUTOCLICKER, maxVl = 3)
public class AutoClickerK extends Check {

    private int clickerTicks = 0;
    private long lastSwing;
    private List<Boolean> clickLongStorage = new ArrayList<>(),
            clickShortStorage = new ArrayList<>();

    void check(WrappedInFlyingPacket wrappedInFlyingPacket) {
        if (playerData.state.isDigging) {
            return;
        }

        this.clickerTicks++;

        if (this.clickerTicks> 3)
            return;

        boolean add = this.clickerTicks < 3;

        if (!add) {
            this.clickLongStorage.add(false);
            this.clickShortStorage.add(false);
        } else {
            this.clickLongStorage.add(true);
            this.clickShortStorage.add(true);
        }

        if (this.clickLongStorage.size() > 275) {
            if (!this.clickLongStorage.contains(false)) {
                fail();
            }
            this.clickLongStorage.clear();
        }

        if (this.clickShortStorage.size() <= 100)
            return;

        if (!this.clickShortStorage.contains(false)) {
            fail();
        }
        this.clickShortStorage.clear();
    }

    void check(WrappedInArmAnimationPacket wrappedInArmAnimationPacket) {
        this.clickerTicks = 0;

        long difference = System.currentTimeMillis() - this.lastSwing;

        if (difference > 250) {
            if (!this.clickLongStorage.isEmpty()) {
                this.clickLongStorage.remove(this.clickLongStorage.size() - 1);

                if (this.clickLongStorage.contains(false)) {
                    this.clickLongStorage.clear();
                }
            }

            if (!this.clickShortStorage.isEmpty()) {
                this.clickShortStorage.remove(this.clickShortStorage.size() - 1);

                if (this.clickShortStorage.contains(false)) {
                    this.clickShortStorage.clear();
                }
            }
        }
        this.lastSwing = System.currentTimeMillis();
    }
}
