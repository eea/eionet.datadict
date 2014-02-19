package eionet.util;

import java.util.ResourceBundle;

/**
 *
 * Datadictionary languages
 *
 */
public class PropsLanguages {
    /* Constants */
    /** properties file name */
    private static String PROP_FILE_NAME = "languages";
    /** language label key */
    private static String LANGUAGE_LABEL_KEYS = "languages.labels";
    /** language code key */
    private static String LANGUAGE_CODE_KEYS = "languages.codes";

    /** Single instance if language values load only once since property file change requires re-deploy */
    private static PropsLanguages[] values = null;

    /** Label. */
    private String label;

    /** Code. */
    private String code;

    /**
     * Class constructor.
     *
     * @param label
     * @param code
     */
    private PropsLanguages(String label, String code) {
        this.label = label;
        this.code = code;
    }

    /**
     * @return the label
     */
    public String getLabel() {
        return label;
    }

    /**
     * @return the code
     */
    public String getCode() {
        return code;
    }

    /**
     * Reads and returns values from properties file
     *
     * @return languages from properties file
     */
    public static PropsLanguages[] getValues() {
        if (PropsLanguages.values == null) {
            ResourceBundle bundle = ResourceBundle.getBundle(PropsLanguages.PROP_FILE_NAME);
            String[] labels = bundle.getString(PropsLanguages.LANGUAGE_LABEL_KEYS).split("[,]");
            String[] codes = bundle.getString(PropsLanguages.LANGUAGE_CODE_KEYS).split("[,]");
            PropsLanguages.values = new PropsLanguages[codes.length];
            for (int i = 0; i < codes.length; i++) {
                PropsLanguages.values[i] = new PropsLanguages(labels[i], codes[i]);
            }
        }
        return PropsLanguages.values;
    }// end of static method getValues

}// end of class PropsLanguages
