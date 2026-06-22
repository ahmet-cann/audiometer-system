package com.audiometer.processing;

import com.audiometer.model.Response;
import com.audiometer.validation.FrequencyValidator;
import com.audiometer.validation.IntensityValidator;
import java.util.Optional;

public class ResponseMessageParser {

    public static Optional<Response> parse(String messageType, int frequencyHz, int intensityDb) {
        if (!"RESPONSE".equals(messageType))                          return Optional.empty();
        if (!FrequencyValidator.isValidClinicalFrequency(frequencyHz)) return Optional.empty();
        if (!IntensityValidator.isValidIntensityDb(intensityDb))       return Optional.empty();
        return Optional.of(new Response(frequencyHz, intensityDb, true));
    }
}
