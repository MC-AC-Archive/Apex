/*
 * Copyright (c) 2018 NGXDEV.COM. Licensed under MIT.
 */

package com.rasturize.anticheat.utils;

import com.rasturize.anticheat.data.playerdata.PlayerData;

/**
 * A timer based on
 * */
public class PlayerTimer {
    private PlayerData playerData;
    public int startTime;
    public int resetStreak;

    public PlayerTimer(PlayerData playerData) {
        this.playerData = playerData;
        this.reset();
    }

    public boolean wasReset() {
        return this.startTime == playerData.currentTick;
    }

    public boolean wasNotReset() {
        return this.startTime != playerData.currentTick;
    }

    public void reset() {
        if (getPassed() == 1) resetStreak++;
        else resetStreak = 0;
        this.startTime = playerData.currentTick;
    }

    public int getResetStreak() {
        return wasNotReset() ? 0 : resetStreak;
    }

    public long getPassed() {
        return playerData.currentTick - this.startTime;
    }

    public void add(int amount) {
        this.startTime -= amount;
    }

    public boolean hasPassed(long toPass) {
        return this.getPassed() >= toPass;
    }

    public boolean hasNotPassed(long toPass) {
        return this.getPassed() < toPass;
    }

    public boolean hasPassed(long toPass, boolean reset) {
        boolean passed = this.getPassed() >= toPass;
        if (passed && reset) reset();
        return passed;
    }

    public static boolean hasPassed(long startTime, long toPass) {
        return (System.currentTimeMillis() - startTime) >= toPass;
    }
}
