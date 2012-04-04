package eionet.web.util;

/**
 * 
 * @author Jaanus Heinlaid
 *
 */
public class DropdownOperation {

    /** */
    private String href;
    private String title;

    /**
     * @param href
     * @param title
     */
    public DropdownOperation(String href, String title) {
        this.href = href;
        this.title = title;
    }

    /**
     * @return the href
     */
    public String getHref() {
        return href;
    }

    /**
     * @param href the href to set
     */
    public void setHref(String href) {
        this.href = href;
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }
}
