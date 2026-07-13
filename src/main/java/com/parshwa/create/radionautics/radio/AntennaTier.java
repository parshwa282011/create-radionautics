package com.parshwa.create.radionautics.radio;

public enum AntennaTier {
    ANDESITE("andesite", 2_000, 1, false),
    COPPER("copper", 5_000, 2, false),
    BRASS("brass", -1, 5, false),
    ASTRONAUTICAL("astronautical", -1, 8, true),
    MEGA("mega", -1, Integer.MAX_VALUE, true);

    private final String serializedName;
    private final int rangeBlocks;
    private final int maxBoundFrequencies;
    private final boolean crossDimensional;

    AntennaTier(String serializedName, int rangeBlocks, int maxBoundFrequencies, boolean crossDimensional) {
        this.serializedName = serializedName;
        this.rangeBlocks = rangeBlocks;
        this.maxBoundFrequencies = maxBoundFrequencies;
        this.crossDimensional = crossDimensional;
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

    public boolean crossDimensional() {
        return crossDimensional;
    }
}
