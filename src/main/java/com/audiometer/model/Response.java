package com.audiometer.model;

public class Response {
    private final int frequencyHz;
    private final int intensityDb;
    private final boolean heard;

    public Response(int frequencyHz, int intensityDb, boolean heard) {
        this.frequencyHz = frequencyHz;
        this.intensityDb = intensityDb;
        this.heard       = heard;
    }

    public int     frequencyHz() { return frequencyHz; }
    public int     intensityDb() { return intensityDb; }
    public boolean heard()       { return heard; }
}
