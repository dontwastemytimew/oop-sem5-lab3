package com.lab.notes.i18n;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class AppLanguageTest {

    @Test
    public void fromTag_returnsMatchingLanguage() {
        assertEquals(AppLanguage.UKRAINIAN, AppLanguage.fromTag("uk"));
        assertEquals(AppLanguage.ENGLISH, AppLanguage.fromTag("en"));
    }

    @Test
    public void fromTag_returnsEnglish_whenTagIsUnknown() {
        assertEquals(AppLanguage.ENGLISH, AppLanguage.fromTag("de"));
    }

    @Test
    public void fromTag_returnsEnglish_whenTagIsNull() {
        assertEquals(AppLanguage.ENGLISH, AppLanguage.fromTag(null));
    }
}