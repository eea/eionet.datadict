package eionet.util;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author Dimitrios Papadimitriou <dp@eworx.gr>
 */
public class StringOrdinalComparatorTest {

    @Test
    public void testComparingSameStringValues() {
        String ordinal1 = "aaa";
        String ordinal2 = "aaa";
        StringOrdinalComparator stringOrdinalComparator = new StringOrdinalComparator();
        boolean expectedValue = true;
        boolean actualValue;

        actualValue = stringOrdinalComparator.compare(ordinal1, ordinal2) == 0;
        assertEquals(expectedValue, actualValue);
    }

    @Test
    public void testComparingSameNumericValues() {
        String ordinal1 = "1";
        String ordinal2 = "1";
        StringOrdinalComparator stringOrdinalComparator = new StringOrdinalComparator();
        boolean expectedValue = true;
        boolean actualValue;

        actualValue = stringOrdinalComparator.compare(ordinal1, ordinal2) == 0;
        assertEquals(expectedValue, actualValue);
    }

    @Test
    public void testComparingValuesOfSymbols() {
        String ordinal1 = "!@#$";
        String ordinal2 = "!@#$";
        StringOrdinalComparator stringOrdinalComparator = new StringOrdinalComparator();
        boolean expectedValue = true;
        boolean actualValue;

        actualValue = stringOrdinalComparator.compare(ordinal1, ordinal2) == 0;
        assertEquals(expectedValue, actualValue);
    }

    @Test
    public void testComparingEmptyStringValues() {
        String ordinal1 = "";
        String ordinal2 = "";
        StringOrdinalComparator stringOrdinalComparator = new StringOrdinalComparator();
        boolean expectedValue = true;
        boolean actualValue;

        actualValue = stringOrdinalComparator.compare(ordinal1, ordinal2) == 0;
        assertEquals(expectedValue, actualValue);
    }

    @Test
    public void testComparingDifferentStringValues() {
        String ordinal1 = "a";
        String ordinal2 = "b";
        StringOrdinalComparator stringOrdinalComparator = new StringOrdinalComparator();
        boolean expectedValue = true;
        boolean actualValue;

        actualValue = stringOrdinalComparator.compare(ordinal1, ordinal2) < 0;
        assertEquals(expectedValue, actualValue);
    }

    @Test
    public void testComparingDifferentStringsWithNumericValues() {
        String ordinal1 = "12";
        String ordinal2 = "34";
        StringOrdinalComparator stringOrdinalComparator = new StringOrdinalComparator();
        boolean expectedValue = true;
        boolean actualValue;

        actualValue = stringOrdinalComparator.compare(ordinal1, ordinal2) < 0;
        assertEquals(expectedValue, actualValue);
    }

    @Test
    public void testComparingDifferentStringsContainingOnlySymbols() {
        String ordinal1 = "!@#$";
        String ordinal2 = "!@#$%";
        StringOrdinalComparator stringOrdinalComparator = new StringOrdinalComparator();
        boolean expectedValue = true;
        boolean actualValue;

        actualValue = stringOrdinalComparator.compare(ordinal1, ordinal2) < 0;
        assertEquals(expectedValue, actualValue);
    }

    @Test
    public void testComparingDifferentStringContainingMixedValues() {
        String ordinal1 = "!@#$as df123*&()";
        String ordinal2 = "!@#$as df123*&()$1";
        StringOrdinalComparator stringOrdinalComparator = new StringOrdinalComparator();
        boolean expectedValue = true;
        boolean actualValue;

        actualValue = stringOrdinalComparator.compare(ordinal1, ordinal2) < 0;
        assertEquals(expectedValue, actualValue);
    }

    @Test
    public void testComparingEmptyStringWithSpaceCharacter() {
        String ordinal1 = "";
        String ordinal2 = " ";
        StringOrdinalComparator stringOrdinalComparator = new StringOrdinalComparator();
        boolean expectedValue = true;
        boolean actualValue;

        actualValue = stringOrdinalComparator.compare(ordinal1, ordinal2) < 0;
        assertEquals(expectedValue, actualValue);
    }

    @Test
    public void testComparingEmptyStringWithOrdinal() {
        String ordinal1 = "";
        String ordinal2 = "asdf";
        StringOrdinalComparator stringOrdinalComparator = new StringOrdinalComparator();
        boolean expectedValue = true;
        boolean actualValue;

        actualValue = stringOrdinalComparator.compare(ordinal1, ordinal2) < 0;
        assertEquals(expectedValue, actualValue);

        ordinal2 = "1124";
        actualValue = stringOrdinalComparator.compare(ordinal1, ordinal2) < 0;
        assertEquals(expectedValue, actualValue);
    }

    // Tests with first string ordinal being of greater value

    @Test
    public void testComparingStringOrdinalsWithTheFirstHavingGreaterValue() {
        String ordinal1 = "z";
        String ordinal2 = "b";
        StringOrdinalComparator stringOrdinalComparator = new StringOrdinalComparator();
        boolean expectedValue = true;
        boolean actualValue;

        actualValue = stringOrdinalComparator.compare(ordinal1, ordinal2) > 0;
        assertEquals(expectedValue, actualValue);
    }

    @Test
    public void testComparingNumericStringOrdinalsWithTheFirstHavingGreaterValue() {
        String ordinal1 = "21";
        String ordinal2 = "3";
        StringOrdinalComparator stringOrdinalComparator = new StringOrdinalComparator();
        boolean expectedValue = true;
        boolean actualValue;

        actualValue = stringOrdinalComparator.compare(ordinal1, ordinal2) > 0;
        assertEquals(expectedValue, actualValue);
    }

    @Test
    public void testComparingStringOrdinalsWithSymbolsFirstHavingGreaterValue() {
        String ordinal1 = "!@#$%$";
        String ordinal2 = "!@#$";
        StringOrdinalComparator stringOrdinalComparator = new StringOrdinalComparator();
        boolean expectedValue = true;
        boolean actualValue;

        actualValue = stringOrdinalComparator.compare(ordinal1, ordinal2) > 0;
        assertEquals(expectedValue, actualValue);
    }

    @Test
    public void testComparingStringOrdinalsWithOnlySpacesFirstOrdinalHasGreaterValue() {
        String ordinal1 = "     ";
        String ordinal2 = "  ";
        StringOrdinalComparator stringOrdinalComparator = new StringOrdinalComparator();
        boolean expectedValue = true;
        boolean actualValue;

        actualValue = stringOrdinalComparator.compare(ordinal1, ordinal2) > 0;
        assertEquals(expectedValue, actualValue);
    }

    @Test
    public void testComparingMixedStringOrdinalsFirstOrdinalHasGreaterValue() {
        String ordinal1 = "!@#$as df123*&()$234";
        String ordinal2 = "!@#$as df123*&()$1";
        StringOrdinalComparator stringOrdinalComparator = new StringOrdinalComparator();
        boolean expectedValue = true;
        boolean actualValue;

        actualValue = stringOrdinalComparator.compare(ordinal1, ordinal2) > 0;
        assertEquals(expectedValue, actualValue);
    }

    @Test
    public void testComparingNumericStringOrdinalWithOrdinalOfSpaces() {
        // Strings containing only spaces have greater value than ordinals with numeric values

        String ordinal1 = "  ";
        String ordinal2 = "123";
        StringOrdinalComparator stringOrdinalComparator = new StringOrdinalComparator();
        boolean expectedValue = true;
        boolean actualValue;

        actualValue = stringOrdinalComparator.compare(ordinal1, ordinal2) > 0;
        assertEquals(expectedValue, actualValue);
    }

    @Test
    public void testComparingSpaceCharacterOrdinalWithOrdinalThatContainsLetterCharacters() {
        // Strings containing only letters (or have letter prefixes followed by symbols or numbers)
        // have greater value than ordinals with only space characters

        String ordinal1 = "asdf";
        String ordinal2 = "  ";
        StringOrdinalComparator stringOrdinalComparator = new StringOrdinalComparator();
        boolean expectedValue = true;
        boolean actualValue;

        actualValue = stringOrdinalComparator.compare(ordinal1, ordinal2) > 0;
        assertEquals(expectedValue, actualValue);
    }


    @Test
    public void testCompareValuesWithSamePrefix() {
        String ordinal1;
        String ordinal2;
        StringOrdinalComparator stringOrdinalComparator = new StringOrdinalComparator();
        boolean expectedValue = true;
        boolean actualValue;

        // Values with different string postfix
        ordinal1 = "aasdf";
        ordinal2 = "aasdfasdf";
        actualValue = stringOrdinalComparator.compare(ordinal1, ordinal2) < 0;
        assertEquals(expectedValue, actualValue);

        // Values with string prefix and numeric postfix
        ordinal1 = "a";
        ordinal2 = "a1";
        actualValue = stringOrdinalComparator.compare(ordinal1, ordinal2) < 0;
        assertEquals(expectedValue, actualValue);

        // Values with numeric prefix and string postfix
        ordinal1 = "1aas";
        ordinal2 = "1aassdf";
        actualValue = stringOrdinalComparator.compare(ordinal1, ordinal2) < 0;
        assertEquals(expectedValue, actualValue);

        // Values with numeric prefix and symbol postfix
        ordinal1 = "1!";
        ordinal2 = "1!@";
        actualValue = stringOrdinalComparator.compare(ordinal1, ordinal2) < 0;
        assertEquals(expectedValue, actualValue);

        // Values with string prefix and symbol postfix
        ordinal1 = "a";
        ordinal2 = "a!@#";
        actualValue = stringOrdinalComparator.compare(ordinal1, ordinal2) < 0;
        assertEquals(expectedValue, actualValue);

        // Values with symbol prefix and string postfix
        ordinal1 = "!@#a";
        ordinal2 = "!@#b";
        actualValue = stringOrdinalComparator.compare(ordinal1, ordinal2) < 0;
        assertEquals(expectedValue, actualValue);

        // Values with symbol prefix and numeric postfix
        ordinal1 = "_1";
        ordinal2 = "_12";
        actualValue = stringOrdinalComparator.compare(ordinal1, ordinal2) < 0;
        assertEquals(expectedValue, actualValue);

        //Values with space prefix
        ordinal1 = " ";
        ordinal2 = " 1";
        actualValue = stringOrdinalComparator.compare(ordinal1, ordinal2) < 0;
        assertEquals(expectedValue, actualValue);
    }

    @Test
    public void testCompareMixedOrdinalWithSamePrefixFirstHavingLowerValue() {
        String ordinal1 = "!@#$as ad234f123*&()";
        String ordinal2 = "!@#$as dc234f223*&()43";
        StringOrdinalComparator stringOrdinalComparator = new StringOrdinalComparator();
        boolean expectedValue = true;
        boolean actualValue;

        actualValue = stringOrdinalComparator.compare(ordinal1, ordinal2) < 0;
        assertEquals(expectedValue, actualValue);
    }
}
