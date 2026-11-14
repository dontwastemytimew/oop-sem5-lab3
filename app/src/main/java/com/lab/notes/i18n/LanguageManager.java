package com.lab.notes.i18n;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

import java.util.Locale;

/**
 * Reads and changes the language of the running application.
 */
public final class LanguageManager {

    private LanguageManager() {
    }

    /**
     * Switches the application to the given language. Open screens are recreated automatically.
     */
    public static void apply(AppLanguage language) {
        AppCompatDelegate.setApplicationLocales(
                LocaleListCompat.forLanguageTags(language.getLanguageTag()));
    }

    /**
     * Returns the language that the application currently uses.
     */
    public static AppLanguage getCurrent() {
        LocaleListCompat appLocales = AppCompatDelegate.getApplicationLocales();
        Locale locale = appLocales.isEmpty() ? Locale.getDefault() : appLocales.get(0);
        return AppLanguage.fromTag(locale == null ? null : locale.getLanguage());
    }
}