package com.audiometer.algorithm;

import com.audiometer.model.Response;
import java.util.List;
import java.util.Optional;

public class ThresholdDetector {

    public static Optional<Integer> detectThreshold(List<Response> responses) {
        if (responses == null || responses.isEmpty()) return Optional.empty();

        int targetDb = responses.get(responses.size() - 1).intensityDb();

        long heardCountAtDb = responses.stream()
                .filter(r -> r.intensityDb() == targetDb && r.heard())
                .count();

        return heardCountAtDb >= 2 ? Optional.of(targetDb) : Optional.empty();
    }
}
