package eionet.meta.exports.pdf;

import java.util.Hashtable;
import java.util.Vector;

public class Sectioning {

    public static final String MODE_ORD = "ord";
    public static final String MODE_GIS = "gis";

    private static final String NR = "NR";
    private static final String LEVEL = "LEVEL";
    private static final String TITLE = "TITLE";

    private Vector toc = null;
    private int[] levelCounters = null;
    private int curLevel = 1;
    private String mode = MODE_ORD;

    private String refCodelists     = "4";
    private String refIllustrations = "5";

    public Sectioning() {
        toc = new Vector();
        levelCounters = new int[10];
        for (int i = 0; i < levelCounters.length; i++)
            levelCounters[i] = 0;
    }

    public String inc(String title, boolean addtoc) {
        levelCounters[curLevel] = levelCounters[curLevel] + 1;
        String nr = getNumber();
        if (addtoc) addToTOC(title, nr, curLevel);
        return nr;
    }

    public String down(String title, boolean addtoc) {
        curLevel++;
        levelCounters[curLevel] = levelCounters[curLevel] + 1;
        String nr = getNumber();
        if (addtoc) addToTOC(title, nr, curLevel);
        return nr;
    }

    public String up(String title, boolean addtoc) {
        levelCounters[curLevel] = 0;
        curLevel--;
        levelCounters[curLevel] = levelCounters[curLevel] + 1;
        String nr = getNumber();
        if (addtoc) addToTOC(title, nr, curLevel);
        return nr;
    }

    public String up(String title, int toLevel, boolean addtoc) {

        if (toLevel >= curLevel || toLevel < 1)
            return null;

        for (int i = curLevel; i > toLevel; i--)
            levelCounters[i] = 0;

        curLevel = toLevel;
        levelCounters[curLevel] = levelCounters[curLevel] + 1;
        String nr = getNumber();
        if (addtoc) addToTOC(title, nr, curLevel);
        return nr;
    }

    public String level(String title, int lv) {
        return level(title, lv, true);
    }

    public String level(String title, int lv, boolean addtoc) {

        if (lv < 1)
            return null;

        if (lv > curLevel) {
            if (lv - curLevel > 1)
                return null;
            else
                return down(title, addtoc);
        } else if (lv == curLevel)
            return inc(title, addtoc);
        else if (curLevel - lv == 1)
            return up(title, addtoc);
        else
            return up(title, lv, addtoc);
    }

    public Vector getTOC() {
        return toc;
    }

    public Vector getTOCformatted(String leveller) {

        Vector v = new Vector();
        for (int i = 0; i < toc.size(); i++) {
            Hashtable hash = (Hashtable) toc.get(i);
            String title = (String) hash.get(TITLE);
            String nr = (String) hash.get(NR);
            Integer level = (Integer) hash.get(LEVEL);

            StringBuffer buf = new StringBuffer();
            for (int j = 0; leveller != null && j < level.intValue() - 1; j++)
                buf.append(leveller);

            buf.append(nr).append(" ").append(title);
            v.add(buf.toString());
        }

        return v;
    }

    String getNumber() {
        StringBuffer buf = new StringBuffer();
        for (int i = 1; i <= curLevel; i++) {
            buf.append(levelCounters[i]);
            if (i != curLevel || curLevel == 1)
                buf.append(".");
        }

        return buf.toString();
    }

    private void addToTOC(String title, String nr, int level) {
        Hashtable hash = new Hashtable();
        hash.put(TITLE, title);
        hash.put(NR, nr);
        hash.put(LEVEL, new Integer(level));
        toc.add(hash);
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getMode() {
        return mode;
    }

    protected Object clone() throws CloneNotSupportedException {
        Sectioning sect = new Sectioning();
        sect.curLevel = curLevel;
        sect.mode = mode;
        if (toc != null)
            sect.toc = (Vector) toc.clone();
        if (levelCounters != null) {
            sect.levelCounters = new int[levelCounters.length];
            for (int i = 0; i < levelCounters.length; i++)
                sect.levelCounters[i] = levelCounters[i];
        }

        return sect;
    }

    public void setRefCodelists(String ref) {
        this.refCodelists = ref;
    }

    public String getRefCodelists() {
        return this.refCodelists;
    }

    public void setRefIllustrations(String ref) {
        this.refIllustrations = ref;
    }

    public String getRefIllustrations() {
        return this.refIllustrations;
    }
}
