package eionet.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.commons.lang.StringUtils;

/**
 *
 * Datadictionary languages.
 *
 */
public final class PropsLanguages {
    /* Constants */
    /** properties file name. */
    private static final String PROP_FILE_NAME = "languages";
    /** language label key. */
    private static final String LANGUAGE_LABEL_KEYS = "languages.labels";
    /** language code key. */
    private static final String LANGUAGE_CODE_KEYS = "languages.codes";
    /** split regex for lines in properties file. */
    private static final String SPLIT_REGEX = "\\s*,\\s*";

    /** Single instance language values load only once, since property file change requires re-deploy. */
    private static PropsLanguages[] values = null;

    /** Label. */
    private String label;

    /** Code. */
    private String code;

    /**
     * Class constructor.
     *
     * @param label
     *            label
     * @param code
     *            code
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
     * Reads and returns values from defined properties file (it caches if it already read it before).
     *
     * @return languages from defined properties file
     */
    public static synchronized PropsLanguages[] getValues() {
        if (PropsLanguages.values == null) {
            PropsLanguages.values = getValuesFrom(PropsLanguages.PROP_FILE_NAME);
        }
        return Arrays.copyOf(PropsLanguages.values, PropsLanguages.values.length);
    } // end of static method getValues

    /**
     * Reads and returns values from given properties file. This method re-parse file every time called.
     *
     * @param bundleFile
     *            bundle file name
     * @return languages from given properties file
     */
    public static synchronized PropsLanguages[] getValuesFrom(String bundleFile) {
        ResourceBundle bundle = ResourceBundle.getBundle(bundleFile);
        String[] labels = bundle.getString(PropsLanguages.LANGUAGE_LABEL_KEYS).split(PropsLanguages.SPLIT_REGEX);
        String[] codes = bundle.getString(PropsLanguages.LANGUAGE_CODE_KEYS).split(PropsLanguages.SPLIT_REGEX);
        List<PropsLanguages> languageList = new ArrayList<PropsLanguages>();
        if (labels.length == codes.length) {
            for (int i = 0; i < codes.length; i++) {
                labels[i] = StringUtils.trimToEmpty(labels[i]);
                codes[i] = StringUtils.trimToEmpty(codes[i]);
                if (StringUtils.isNotEmpty(labels[i]) && StringUtils.isNotEmpty(codes[i])) {
                    languageList.add(new PropsLanguages(labels[i], codes[i]));
                }
            }
        }
        PropsLanguages[] languageValues = new PropsLanguages[languageList.size()];
        languageList.toArray(languageValues);

        return languageValues;
    } // end of static method getValuesFrom

} // end of class PropsLanguages
