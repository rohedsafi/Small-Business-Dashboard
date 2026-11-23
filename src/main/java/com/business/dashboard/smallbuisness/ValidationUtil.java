package com.business.dashboard.smallbuisness;



public class ValidationUtil {

    public static String validateRequired(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            return fieldName + " is required";
        }
        return null;
    }

    public static String validatePositiveNumber(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            return fieldName + " is required";
        }
        try {
            double number = Double.parseDouble(value);
            if (number <= 0) {
                return fieldName + " must be greater than 0";
            }
        } catch (NumberFormatException e) {
            return fieldName + " must be a valid number";
        }
        return null;
    }

    public static String validatePositiveInteger(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            return fieldName + " is required";
        }
        try {
            int number = Integer.parseInt(value);
            if (number <= 0) {
                return fieldName + " must be greater than 0";
            }
        } catch (NumberFormatException e) {
            return fieldName + " must be a valid whole number";
        }
        return null;
    }
}
