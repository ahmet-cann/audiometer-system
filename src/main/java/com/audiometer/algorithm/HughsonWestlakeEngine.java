package com.audiometer.algorithm;

import com.audiometer.model.Response;
import com.audiometer.model.TestState;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class HughsonWestlakeEngine {

    public static int nextIntensity(int currentDb, boolean heard) {
        return heard ? currentDb - 10 : currentDb + 5;
    }

    public static TestState processResponse(TestState initialState, Response response) {
        List<Response> updatedResponses = new ArrayList<>(initialState.responses());
        updatedResponses.add(response);

        int nextDb = nextIntensity(initialState.currentDb(), response.heard());

        return new TestState(
                initialState.ear(),
                initialState.frequencyHz(),
                nextDb,
                updatedResponses,
                initialState.thresholdDb()
        );
    }
}
