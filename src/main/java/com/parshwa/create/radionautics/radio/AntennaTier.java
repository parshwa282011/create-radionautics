package com.parshwa.create.radionautics.radio;

public enum AntennaTier {
    ANDESITE("andesite", 2_000, 1),
    COPPER("copper", 5_000, 2),
    BRASS("brass", -1, 5);

    private final String serializedName;
    private final int rangeBlocks;
    private final int maxBoundFrequencies;

    AntennaTier(String serializedName, int rangeBlocks, int maxBoundFrequencies) {
        this.serializedName = serializedName;
        this.rangeBlocks = rangeBlocks;
        this.maxBoundFrequencies = maxBoundFrequencies;
    }

    public String serializedName() {
        return serializedName;
    }

    public int rangeBlocks() {
        return rangeBlocks;
    }

    public int maxBoundFrequencies() {
        return maxBoundFrequencies;
    }

    public boolean infinite() {
        return rangeBlocks < 0;
    }
}
