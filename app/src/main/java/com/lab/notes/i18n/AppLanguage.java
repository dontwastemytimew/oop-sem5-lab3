package com.lab.notes.i18n;

/**
 * Languages that the user can choose in the application.
 */
public enum AppLanguage {
    ENGLISH("en", "English"),
    UKRAINIAN("uk", "Українська");

    private final String languageTag;
    private final String displayName;

    AppLanguage(String languageTag, String displayName) {
        this.languageTag = languageTag;
        this.displayName = displayName;
    }

    public String getLanguageTag() {
        return languageTag;
    }

    /**
     * Returns the name of the language written in that language itself.
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Finds the language by its tag, or returns English if the tag is not supported.
     */
    public static AppLanguage fromTag(String languageTag) {
        for (AppLanguage language : values()) {
            if (language.languageTag.equals(languageTag)) {
                return language;
            }
        }
        return ENGLISH;
    }
}