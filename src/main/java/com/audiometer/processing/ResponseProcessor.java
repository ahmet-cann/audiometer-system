package com.audiometer.processing;

import com.audiometer.model.Response;
import java.util.List;
import java.util.stream.Collectors;

public class ResponseProcessor {

    public static long countHeardResponses(List<Response> responses) {
        return responses.stream().filter(Response::heard).count();
    }

    public static double calculateHeardRatio(List<Response> responses) {
        if (responses.isEmpty()) return 0.0;
        return (double) countHeardResponses(responses) / responses.size();
    }

    public static List<Integer> extractHeardIntensityValues(List<Response> responses) {
        return responses.stream()
                .filter(Response::heard)
                .map(Response::intensityDb)
                .collect(Collectors.toList());
    }

    public static int sumHeardIntensityValues(List<Response> responses) {
        return responses.stream()
                .filter(Response::heard)
                .map(Response::intensityDb)
                .reduce(0, Integer::sum);
    }
}
