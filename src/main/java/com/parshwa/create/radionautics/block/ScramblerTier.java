package com.parshwa.create.radionautics.block;

public enum ScramblerTier {
    COPPER(100),
    BRASS(1000);

    private final int radiusBlocks;

    ScramblerTier(int radiusBlocks) {
        this.radiusBlocks = radiusBlocks;
    }

    public int radiusBlocks() {
        return radiusBlocks;
    }
}
