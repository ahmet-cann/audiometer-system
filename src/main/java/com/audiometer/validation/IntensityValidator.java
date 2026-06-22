package com.audiometer.validation;

public class IntensityValidator {

    public static boolean isValidIntensityDb(int db) {
        return db >= 0 && db <= 120;
    }
}
