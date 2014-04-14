package eionet.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by enver on 1.04.14.
 */
public class PropsLanguagesTest {

    @Test
    public void testLanguagesWhenEverythingIsOk() {
        String[] labels =
                new String[] {"Bulgarian", "Czech", "Danish", "German", "Greek", "English", "Spanish", "Estonian", "Finnish",
                        "French", "Irish", "Hungarian", "Italian", "Lithuanian", "Latvian", "Maltese", "Dutch", "Norwegian",
                        "Polish", "Portuguese", "Romanian", "Slovak", "Slovenian", "Swedish"};
        String[] codes =
                new String[] {"bg", "cs", "da", "de", "el", "en", "es", "et", "fi", "fr", "ga", "hu", "it", "lt", "lv", "mt",
                        "nl", "no", "pl", "pt", "ro", "sk", "sl", "sv"};

        PropsLanguages[] langs = PropsLanguages.getValuesFrom("testlanguages");

        Assert.assertEquals("Size does not match", labels.length, langs.length);
        for (int i = 0; i < langs.length; i++) {
            Assert.assertEquals("Label does not match", labels[i], langs[i].getLabel());
            Assert.assertEquals("Code does not match", codes[i], langs[i].getCode());
        }
    }

    @Test
    public void testLanguagesFromLanguagesProperties() {
        String[] labels =
                new String[] {"Arabic", "Bulgarian", "Czech", "Danish", "German", "Greek", "English", "Spanish", "Estonian",
                        "Finnish", "French", "Irish", "Hungarian", "Italian", "Lithuanian", "Latvian", "Maltese", "Dutch",
                        "Norwegian", "Polish", "Portuguese", "Romanian", "Slovak", "Slovenian", "Swedish"};
        String[] codes =
                new String[] {"ar", "bg", "cs", "da", "de", "el", "en", "es", "et", "fi", "fr", "ga", "hu", "it", "lt", "lv",
                        "mt", "nl", "no", "pl", "pt", "ro", "sk", "sl", "sv"};

        PropsLanguages[] langs = PropsLanguages.getValues();

        Assert.assertEquals("Size does not match", labels.length, langs.length);
        for (int i = 0; i < langs.length; i++) {
            Assert.assertEquals("Label does not match", labels[i], langs[i].getLabel());
            Assert.assertEquals("Code does not match", codes[i], langs[i].getCode());
        }
    }

    @Test
    public void testLanguagesWhenFileIsEmpty() {
        PropsLanguages[] langs = PropsLanguages.getValuesFrom("testlanguagesempty");
        Assert.assertEquals("Language array is not empty", 0, langs.length);
    }

    @Test
    public void testLanguagesWhenFileHasNotEqualLabelsAndCodes() {
        PropsLanguages[] langs = PropsLanguages.getValuesFrom("testlanguagesnotequalcodelabelsize");
        Assert.assertEquals("Language array is not empty", 0, langs.length);
    }

    @Test
    public void testLanguagesWhenFileContainsOnlyCommas() {
        PropsLanguages[] langs = PropsLanguages.getValuesFrom("testlanguagesonlycommas");
        Assert.assertEquals("Language array is not empty", 0, langs.length);
    }

    @Test
    public void testLanguagesWhenThereAreSpacesAndTabs() {
        String[] labels =
                new String[] {"Bulgarian", "Czech", "Danish", "German", "Greek", "English", "Spanish", "Estonian", "Finnish",
                        "French", "Irish", "Hungarian", "Italian", "Lithuanian", "Latvian", "Maltese", "Dutch", "Norwegian",
                        "Polish", "Portuguese", "Romanian", "Slovak", "Slovenian", "Swedish"};
        String[] codes =
                new String[] {"bg", "cs", "da", "de", "el", "en", "es", "et", "fi", "fr", "ga", "hu", "it", "lt", "lv", "mt",
                        "nl", "no", "pl", "pt", "ro", "sk", "sl", "sv"};

        PropsLanguages[] langs = PropsLanguages.getValuesFrom("testlanguageswithsomespaces");

        Assert.assertEquals("Size does not match", labels.length, langs.length);
        for (int i = 0; i < langs.length; i++) {
            Assert.assertEquals("Label does not match", labels[i], langs[i].getLabel());
            Assert.assertEquals("Code does not match", codes[i], langs[i].getCode());
        }
    }

    @Test
    public void testLanguagesWhenThereAreSpacesAndTabsAndEmptyElements() {
        String[] labels =
                new String[] {"Bulgarian", "Czech", "Danish", "German", "Greek", "English", "Spanish", "Estonian", "Finnish",
                        "French", "Irish", "Hungarian", "Italian", "Lithuanian", "Latvian", "Maltese", "Dutch", "Norwegian",
                        "Polish", "Portuguese", "Romanian", "Slovak", "Slovenian", "Swedish"};
        String[] codes =
                new String[] {"bg", "cs", "da", "de", "el", "en", "es", "et", "fi", "fr", "ga", "hu", "it", "lt", "lv", "mt",
                        "nl", "no", "pl", "pt", "ro", "sk", "sl", "sv"};

        PropsLanguages[] langs = PropsLanguages.getValuesFrom("testlanguageswithsomespacesandempty");

        Assert.assertEquals("Size does not match", labels.length, langs.length);
        for (int i = 0; i < langs.length; i++) {
            Assert.assertEquals("Label does not match", labels[i], langs[i].getLabel());
            Assert.assertEquals("Code does not match", codes[i], langs[i].getCode());
        }
    }
}
