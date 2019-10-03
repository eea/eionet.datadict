package eionet.web.util;

/**
 * @author Jaanus Heinlaid
 */
public class Tab {

    /** */
    private String title;
    private String href;
    private String hint;
    private boolean selected;

    /**
     * 
     */
    public Tab() {
        // default constructor
    }

    /**
     * @param title
     * @param href
     * @param hint
     * @param selected
     */
    public Tab(String title, String href, String hint, boolean selected) {
        this.title = title;
        this.href = href;
        this.hint = hint;
        this.selected = selected;
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @return the href
     */
    public String getHref() {
        return href;
    }

    /**
     * @return the hint
     */
    public String getHint() {
        return hint;
    }

    /**
     * @return the selected
     */
    public boolean isSelected() {
        return selected;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @param href the href to set
     */
    public void setHref(String href) {
        this.href = href;
    }

    /**
     * @param hint the hint to set
     */
    public void setHint(String hint) {
        this.hint = hint;
    }

    /**
     * @param selected the selected to set
     */
    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
