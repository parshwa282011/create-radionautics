package com.parshwa.create.radionautics.radio;

public interface RadioRedstoneLink extends RadioEndpoint {
    String frequency();

    boolean isReceiver();

    int outputStrength();

    void configure(String frequency, boolean receiver);

    void receiveRadioStrength(int strength);

    AntennaTier radioTier();

    boolean isRemoved();
}
