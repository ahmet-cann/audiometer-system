package com.audiometer.algorithm;

import com.audiometer.model.Response;
import com.audiometer.model.TestState;
import java.util.Optional;

public class AudiometrySessionEngine {

    public static TestState handleResponse(TestState initialState, Response response) {
        TestState intermediateState = HughsonWestlakeEngine.processResponse(initialState, response);
        Optional<Integer> detectedThreshold = ThresholdDetector.detectThreshold(intermediateState.responses());

        return new TestState(
                intermediateState.ear(),
                intermediateState.frequencyHz(),
                intermediateState.currentDb(),
                intermediateState.responses(),
                detectedThreshold
        );
    }
}
