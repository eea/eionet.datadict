package eionet.util;

import java.math.BigInteger;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Dimitrios Papadimitriou <dp@eworx.gr>
 */
public class StringOrdinalComparator implements Comparator<String> {

    public StringOrdinalComparator() {
    }

    private static final Pattern PATTERN = Pattern.compile("(\\D*)(\\d*)");

    @Override
    public int compare(String ordinal1, String ordinal2) {
        String value1 = ordinal1.toUpperCase();
        String value2 = ordinal2.toUpperCase();

        Matcher matcher1 = PATTERN.matcher(value1);
        Matcher matcher2 = PATTERN.matcher(value2);

        while (matcher1.find() && matcher2.find()) {
            // matcher.group(1) fetches any non-digits captured by the first parentheses in PATTERN.
            int nonDigitCompare = matcher1.group(1).compareTo(matcher2.group(1));
            if (0 != nonDigitCompare) {
                return nonDigitCompare;
            }

            // matcher.group(2) fetches any digits captured by the second parentheses in PATTERN.
            if (matcher1.group(2).isEmpty()) {
                return matcher2.group(2).isEmpty() ? 0 : -1;
            } else if (matcher2.group(2).isEmpty()) {
                return +1;
            }

            BigInteger numericValue1 = new BigInteger(matcher1.group(2));
            BigInteger numericValue2 = new BigInteger(matcher2.group(2));
            int numberCompare = numericValue1.compareTo(numericValue2);
            if (0 != numberCompare) {
                return numberCompare;
            }
        }

        // Handle if one string is a prefix of the other.
        // Nothing comes before something.
        return matcher1.hitEnd() && matcher2.hitEnd() ? 0
                : matcher1.hitEnd() ? -1 : +1;
    }
}
