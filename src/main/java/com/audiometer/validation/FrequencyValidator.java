package com.audiometer.validation;

import java.util.Set;

public class FrequencyValidator {
    private static final Set<Integer> CLINICAL_FREQUENCIES = Set.of(250, 500, 1000, 2000, 4000, 8000);

    public static boolean isValidClinicalFrequency(int frequencyHz) {
        return CLINICAL_FREQUENCIES.contains(frequencyHz);
    }
}
