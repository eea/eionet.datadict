package eionet.util;

import eionet.meta.ActionBeanUtils;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.unitils.spring.annotation.SpringApplicationContext;

/**
 * Created by enver on 1.04.14.
 */
@SpringApplicationContext("mock-spring-context.xml")
public class PropsLanguagesTest {

//    @Test
//    public void testLanguagesWhenEverythingIsOk() {
//        String[] labels =
//                new String[] {"Bulgarian", "Czech", "Danish", "German", "Greek", "English", "Spanish", "Estonian", "Finnish",
//                        "French", "Irish", "Hungarian", "Italian", "Lithuanian", "Latvian", "Maltese", "Dutch", "Norwegian",
//                        "Polish", "Portuguese", "Romanian", "Slovak", "Slovenian", "Swedish"};
//        String[] codes =
//                new String[] {"bg", "cs", "da", "de", "el", "en", "es", "et", "fi", "fr", "ga", "hu", "it", "lt", "lv", "mt",
//                        "nl", "no", "pl", "pt", "ro", "sk", "sl", "sv"};
//
//        PropsLanguages[] langs = PropsLanguages.getPropertyValues();
//
//        Assert.assertEquals("Size does not match", labels.length, langs.length);
//        for (int i = 0; i < langs.length; i++) {
//            Assert.assertEquals("Label does not match", labels[i], langs[i].getLabel());
//            Assert.assertEquals("Code does not match", codes[i], langs[i].getCode());
//        }
//    }

    @BeforeClass
    public static void setup(){
        ActionBeanUtils.getServletContext();
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

//    @Test
//    public void testLanguagesWhenFileIsEmpty() {
//        PropsLanguages[] langs = PropsLanguages.getPropertyValues();
//        Assert.assertEquals("Language array is not empty", 0, langs.length);
//    }
//
//    @Test
//    public void testLanguagesWhenFileHasNotEqualLabelsAndCodes() {
//        PropsLanguages[] langs = PropsLanguages.getPropertyValues();
//        Assert.assertEquals("Language array is not empty", 0, langs.length);
//    }
//
//    @Test
//    public void testLanguagesWhenFileContainsOnlyCommas() {
//        PropsLanguages[] langs = PropsLanguages.getPropertyValues();
//        Assert.assertEquals("Language array is not empty", 0, langs.length);
//    }
//
//    @Test
//    public void testLanguagesWhenThereAreSpacesAndTabs() {
//        String[] labels =
//                new String[] {"Bulgarian", "Czech", "Danish", "German", "Greek", "English", "Spanish", "Estonian", "Finnish",
//                        "French", "Irish", "Hungarian", "Italian", "Lithuanian", "Latvian", "Maltese", "Dutch", "Norwegian",
//                        "Polish", "Portuguese", "Romanian", "Slovak", "Slovenian", "Swedish"};
//        String[] codes =
//                new String[] {"bg", "cs", "da", "de", "el", "en", "es", "et", "fi", "fr", "ga", "hu", "it", "lt", "lv", "mt",
//                        "nl", "no", "pl", "pt", "ro", "sk", "sl", "sv"};
//
//        PropsLanguages[] langs = PropsLanguages.getPropertyValues();
//
//        Assert.assertEquals("Size does not match", labels.length, langs.length);
//        for (int i = 0; i < langs.length; i++) {
//            Assert.assertEquals("Label does not match", labels[i], langs[i].getLabel());
//            Assert.assertEquals("Code does not match", codes[i], langs[i].getCode());
//        }
//    }
//
//    @Test
//    public void testLanguagesWhenThereAreSpacesAndTabsAndEmptyElements() {
//        String[] labels =
//                new String[] {"Bulgarian", "Czech", "Danish", "German", "Greek", "English", "Spanish", "Estonian", "Finnish",
//                        "French", "Irish", "Hungarian", "Italian", "Lithuanian", "Latvian", "Maltese", "Dutch", "Norwegian",
//                        "Polish", "Portuguese", "Romanian", "Slovak", "Slovenian", "Swedish"};
//        String[] codes =
//                new String[] {"bg", "cs", "da", "de", "el", "en", "es", "et", "fi", "fr", "ga", "hu", "it", "lt", "lv", "mt",
//                        "nl", "no", "pl", "pt", "ro", "sk", "sl", "sv"};
//
//        PropsLanguages[] langs = PropsLanguages.getPropertyValues();
//
//        Assert.assertEquals("Size does not match", labels.length, langs.length);
//        for (int i = 0; i < langs.length; i++) {
//            Assert.assertEquals("Label does not match", labels[i], langs[i].getLabel());
//            Assert.assertEquals("Code does not match", codes[i], langs[i].getCode());
//        }
//    }
}
