package com.audiometer.model;

import java.util.List;
import java.util.Optional;

public class TestState {
    private final Ear ear;
    private final int frequencyHz;
    private final int currentDb;
    private final List<Response> responses;
    private final Optional<Integer> thresholdDb;

    public TestState(Ear ear, int frequencyHz, int currentDb,
                     List<Response> responses, Optional<Integer> thresholdDb) {
        this.ear         = ear;
        this.frequencyHz = frequencyHz;
        this.currentDb   = currentDb;
        this.responses   = responses;
        this.thresholdDb = thresholdDb;
    }

    public Ear              ear()         { return ear; }
    public int              frequencyHz() { return frequencyHz; }
    public int              currentDb()   { return currentDb; }
    public List<Response>   responses()   { return responses; }
    public Optional<Integer> thresholdDb(){ return thresholdDb; }
}
