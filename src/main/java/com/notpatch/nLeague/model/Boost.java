package com.notpatch.nLeague.model;

import lombok.Data;

@Data
public class Boost {

    private double multiplier;
    private int remainingSeconds;

    public Boost(double multiplier, int remainingSeconds) {
        this.multiplier = multiplier;
        this.remainingSeconds = remainingSeconds;
    }

    public boolean hasBoost() {
        return remainingSeconds > 0 && multiplier > 1.0;
    }
}
