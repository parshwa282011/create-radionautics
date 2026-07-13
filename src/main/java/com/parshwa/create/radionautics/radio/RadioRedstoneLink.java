package com.parshwa.create.radionautics.radio;

public interface RadioRedstoneLink extends RadioEndpoint, RadioFrequencyConfigurable {
    boolean isReceiver();

    int outputStrength();

    void configure(String frequency, boolean receiver);

    @Override
    default void configureFrequency(String frequency) {
        configure(frequency, isReceiver());
    }

    void receiveRadioStrength(int strength);

    AntennaTier radioTier();

    boolean isRemoved();
}
