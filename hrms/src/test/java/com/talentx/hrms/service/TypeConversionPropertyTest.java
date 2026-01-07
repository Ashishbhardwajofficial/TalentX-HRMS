package com.talentx.hrms.service;

import com.talentx.hrms.entity.enums.Gender;
import com.talentx.hrms.entity.enums.HolidayType;
import com.talentx.hrms.entity.recruitment.Interview;
import net.jqwik.api.*;
import org.junit.jupiter.api.DisplayName;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based tests for type conversions in the HRMS backend
 * Feature: backend-compilation-fix
 */
@DisplayName("Type Conversion Property Tests")
public class TypeConversionPropertyTest {

    /**
     * Property 1: Type Conversion Preserves Values
     * Validates: Requirements 2.2
     * 
     * For any Integer value, converting to BigDecimal using BigDecimal.valueOf()
     * and back to Integer should preserve the original value.
     */
    @Property(tries = 100)
    @Label("Feature: backend-compilation-fix, Property 1: Type Conversion Preserves Values")
    void integerToBigDecimalRoundTrip(@ForAll Integer value) {
        // Convert Integer to BigDecimal
        BigDecimal decimal = BigDecimal.valueOf(value);
        
        // Convert back to Integer
        int converted = decimal.intValue();
        
        // Verify the value is preserved
        assertEquals(value, converted, 
            "Integer to BigDecimal conversion should preserve the original value");
    }

    /**
     * Property 2: Date Conversion Round Trip
     * Validates: Requirements 2.4
     * 
     * For any LocalDate value, converting to java.sql.Date using Date.valueOf()
     * and back to LocalDate should preserve the original date.
     */
    @Property(tries = 100)
    @Label("Feature: backend-compilation-fix, Property 2: Date Conversion Round Trip")
    void localDateToSqlDateRoundTrip(@ForAll LocalDate date) {
        // Convert LocalDate to java.sql.Date
        Date sqlDate = Date.valueOf(date);
        
        // Convert back to LocalDate
        LocalDate converted = sqlDate.toLocalDate();
        
        // Verify the date is preserved
        assertEquals(date, converted,
            "LocalDate to Date conversion should preserve the original date");
    }

    /**
     * Property 3: String to Enum Conversion
     * Validates: Requirements 2.6
     * 
     * For any valid enum constant name as a String, converting to the enum type
     * using valueOf() should produce the correct enum constant.
     */
    @Property(tries = 100)
    @Label("Feature: backend-compilation-fix, Property 3: String to Enum Conversion - Gender")
    void stringToGenderEnumConversion(@ForAll("genderStrings") String genderString) {
        // Convert String to Gender enum
        Gender gender = Gender.valueOf(genderString);
        
        // Convert back to String
        String converted = gender.name();
        
        // Verify the conversion is correct
        assertEquals(genderString, converted,
            "String to Gender enum conversion should preserve the enum constant name");
    }

    @Property(tries = 100)
    @Label("Feature: backend-compilation-fix, Property 3: String to Enum Conversion - HolidayType")
    void stringToHolidayTypeEnumConversion(@ForAll("holidayTypeStrings") String holidayTypeString) {
        // Convert String to HolidayType enum
        HolidayType holidayType = HolidayType.valueOf(holidayTypeString);
        
        // Convert back to String
        String converted = holidayType.name();
        
        // Verify the conversion is correct
        assertEquals(holidayTypeString, converted,
            "String to HolidayType enum conversion should preserve the enum constant name");
    }

    @Property(tries = 100)
    @Label("Feature: backend-compilation-fix, Property 3: String to Enum Conversion - InterviewRecommendation")
    void stringToInterviewRecommendationEnumConversion(@ForAll("interviewRecommendationStrings") String recommendationString) {
        // Convert String to InterviewRecommendation enum
        Interview.InterviewRecommendation recommendation = Interview.InterviewRecommendation.valueOf(recommendationString);
        
        // Convert back to String
        String converted = recommendation.name();
        
        // Verify the conversion is correct
        assertEquals(recommendationString, converted,
            "String to InterviewRecommendation enum conversion should preserve the enum constant name");
    }

    @Property(tries = 100)
    @Label("Feature: backend-compilation-fix, Property 3: String to Enum Conversion - InterviewType")
    void stringToInterviewTypeEnumConversion(@ForAll("interviewTypeStrings") String interviewTypeString) {
        // Convert String to InterviewType enum
        Interview.InterviewType interviewType = Interview.InterviewType.valueOf(interviewTypeString);
        
        // Convert back to String
        String converted = interviewType.name();
        
        // Verify the conversion is correct
        assertEquals(interviewTypeString, converted,
            "String to InterviewType enum conversion should preserve the enum constant name");
    }

    /**
     * Property 4: Instant to Timestamp Round Trip
     * Validates: Requirements 3.2, 3.3
     * 
     * For any Instant value, converting to Timestamp using Timestamp.from()
     * and back to Instant using toInstant() should preserve the original value.
     */
    @Property(tries = 100)
    @Label("Feature: backend-compilation-fix, Property 4: Instant to Timestamp Round Trip")
    void instantToTimestampRoundTrip(@ForAll Instant instant) {
        // Convert Instant to Timestamp
        Timestamp timestamp = Timestamp.from(instant);
        
        // Convert back to Instant
        Instant converted = timestamp.toInstant();
        
        // Verify the value is preserved
        assertEquals(instant, converted,
            "Instant to Timestamp conversion should preserve the original value");
    }

    // Providers for enum string values
    @Provide
    Arbitrary<String> genderStrings() {
        return Arbitraries.of("MALE", "FEMALE", "OTHER", "PREFER_NOT_TO_SAY");
    }

    @Provide
    Arbitrary<String> holidayTypeStrings() {
        return Arbitraries.of("NATIONAL", "REGIONAL", "OPTIONAL", "COMPANY");
    }

    @Provide
    Arbitrary<String> interviewRecommendationStrings() {
        return Arbitraries.of("STRONG_HIRE", "HIRE", "NO_HIRE", "STRONG_NO_HIRE");
    }

    @Provide
    Arbitrary<String> interviewTypeStrings() {
        return Arbitraries.of("PHONE_SCREEN", "TECHNICAL", "BEHAVIORAL", "PANEL", "FINAL");
    }
}
