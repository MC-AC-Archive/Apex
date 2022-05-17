package com.rasturize.anticheat.api.event;

public class PlayerTakeVelocityEvent {
    public double deltaH, deltaV;

    public PlayerTakeVelocityEvent(double deltaH, double deltaV) {
        this.deltaH = deltaH;
        this.deltaV = deltaV;
    }
}
