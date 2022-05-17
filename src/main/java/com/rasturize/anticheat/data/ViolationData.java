/*
 * Copyright (c) 2018 NGXDEV.COM. Licensed under MIT.
 */

package com.rasturize.anticheat.data;

import com.rasturize.anticheat.data.playerdata.PlayerData;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.LinkedList;

@RequiredArgsConstructor
public class ViolationData {
    @NonNull public final PlayerData playerData;
    @Getter private LinkedList<Integer> violations = new LinkedList<>();
    @Getter private long lastTime;

    @Getter private int totalViolationCount = 0;

    public int getViolation(long time) {
        violations.add(playerData.currentTick);

        totalViolationCount++;

        if (time != -1) violations.removeIf(l -> playerData.currentTick - l > time);

        lastTime = time;

        return violations.size();
    }

    public int getViolationCount() {
        if (lastTime != 0)
            violations.removeIf(l -> playerData.currentTick - l > lastTime);

        return violations.size();
    }

    public void removeFirst() {
        if (!violations.isEmpty())
            violations.removeFirst();
    }

    public void clearViolations() {
        violations.clear();
    }
}
