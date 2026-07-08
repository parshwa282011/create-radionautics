package com.parshwa.create.radionautics.block;

import net.minecraft.util.StringRepresentable;

public enum AstronauticalRadioLinkPart implements StringRepresentable {
    LOWER("lower"),
    MIDDLE("middle"),
    UPPER("upper");

    private final String serializedName;

    AstronauticalRadioLinkPart(String serializedName) {
        this.serializedName = serializedName;
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }
}
