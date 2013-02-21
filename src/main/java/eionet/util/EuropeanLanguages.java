package eionet.util;

public enum EuropeanLanguages {

    /** Languages. */
    BG("Bulgarian", "bg"),
    CS("Czech", "cs"),
    DA("Danish", "da"),
    DE("German", "de"),
    EL("Greek", "el"),
    EN("English", "en"),
    ES("Spanish", "es"),
    ET("Estonian", "et"),
    FI("Finnish", "fi"),
    FR("French", "fr"),
    GA("Irish", "ga"),
    HU("Hungarian", "hu"),
    IT("Italian", "it"),
    LT("Lithuanian", "lt"),
    LV("Latvian", "lv"),
    MT("Maltese", "mt"),
    NL("Dutch", "nl"),
    PL("Polish", "pl"),
    PT("Portuguese", "pt"),
    RO("Romanian", "ro"),
    SK("Slovak", "sk"),
    SL("Slovenian", "sl"),
    SV("Swedish", "sv");

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
    private EuropeanLanguages(String label, String code) {
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

}
