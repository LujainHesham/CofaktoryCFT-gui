package com.cofaktory.footprint.service;

import com.itextpdf.layout.properties.BaseDirection;

import java.util.Locale;
import java.util.ResourceBundle;

public class LanguageService {
    private static Locale currentLocale = Locale.of("en");
    private static ResourceBundle messages = ResourceBundle.getBundle("messages", currentLocale);

    public static void setLanguage(String languageCode) {
        currentLocale = Locale.of("en");
        messages = ResourceBundle.getBundle("messages", currentLocale);
    }

    public static String getMessage(String key) {
        return messages.getString(key);
    }

    public String getCurrentLanguage() {
        return currentLocale.getLanguage();
    }

    // For date/number formatting based on locale
    public String formatNumber(double number) {
        // Implementation would vary based on locale
        return String.format(currentLocale, "%.2f", number);
    }

    public boolean isRTL() {
        return currentLocale.getLanguage().equals("ar");
    }


}