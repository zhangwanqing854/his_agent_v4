package com.hospital.handover.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;

public class AgeCalculator {

    public static Integer calculateAge(LocalDateTime birthDate) {
        if (birthDate == null) {
            return null;
        }
        LocalDate birthLocalDate = birthDate.toLocalDate();
        LocalDate today = LocalDate.now();
        return Period.between(birthLocalDate, today).getYears();
    }

    public static Integer calculateAgeOrDefault(LocalDateTime birthDate, Integer defaultAge) {
        Integer age = calculateAge(birthDate);
        return age != null ? age : defaultAge;
    }
}