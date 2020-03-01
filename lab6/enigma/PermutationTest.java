package enigma;

import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.Timeout;
import static org.junit.Assert.*;

import static enigma.TestUtils.*;

/**
 * The suite of all JUnit tests for the Permutation class. For the purposes of
 * this lab (in order to test) this is an abstract class, but in proj1, it will
 * be a concrete class. If you want to copy your tests for proj1, you can make
 * this class concrete by removing the 4 abstract keywords and implementing the
 * 3 abstract methods.
 *
 *  @author Pavel Gladkevich
 */
public abstract class PermutationTest {

    /**
     * For this lab, you must use this to get a new Permutation,
     * the equivalent to:
     * new Permutation(cycles, alphabet)
     * @return a Permutation with cycles as its cycles and alphabet as
     * its alphabet
     * @see Permutation for description of the Permutation conctructor
     */
    abstract Permutation getNewPermutation(String cycles, Alphabet alphabet);

    /**
     * For this lab, you must use this to get a new Alphabet,
     * the equivalent to:
     * new Alphabet(chars)
     * @return an Alphabet with chars as its characters
     * @see Alphabet for description of the Alphabet constructor
     */
    abstract Alphabet getNewAlphabet(String chars);

    /**
     * For this lab, you must use this to get a new Alphabet,
     * the equivalent to:
     * new Alphabet()
     * @return a default Alphabet with characters ABCD...Z
     * @see Alphabet for description of the Alphabet constructor
     */
    abstract Alphabet getNewAlphabet();

    /** Testing time limit. */
    @Rule
    public Timeout globalTimeout = Timeout.seconds(5);

    /** Check that PERM has an ALPHABET whose size is that of
     *  FROMALPHA and TOALPHA and that maps each character of
     *  FROMALPHA to the corresponding character of FROMALPHA, and
     *  vice-versa. TESTID is used in error messages. */
    private void checkPerm(String testId,
                           String fromAlpha, String toAlpha,
                           Permutation perm, Alphabet alpha) {
        int N = fromAlpha.length();
        assertEquals(testId + " (wrong length)", N, perm.size());
        for (int i = 0; i < N; i += 1) {
            char c = fromAlpha.charAt(i), e = toAlpha.charAt(i);
            assertEquals(msg(testId, "wrong translation of '%c'", c),
                         e, perm.permute(c));
            assertEquals(msg(testId, "wrong inverse of '%c'", e),
                         c, perm.invert(e));
            int ci = alpha.toInt(c), ei = alpha.toInt(e);
            assertEquals(msg(testId, "wrong translation of %d", ci),
                         ei, perm.permute(ci));
            assertEquals(msg(testId, "wrong inverse of %d", ei),
                         ci, perm.invert(ei));
        }
    }

    /* ***** TESTS ***** */

    @Test
    public void checkIdTransform() {
        Alphabet alpha = getNewAlphabet();
        Permutation perm = getNewPermutation("", alpha);
        checkPerm("identity", UPPER_STRING, UPPER_STRING, perm, alpha);
    }

    @Test
    public void testGetAlphabet() {
        Alphabet a = getNewAlphabet();
        Permutation p = getNewPermutation("", a);
        assertEquals(a,p.alphabet());

        Alphabet a1 = getNewAlphabet("");
        Permutation p1 = getNewPermutation("",a1);
        assertEquals(a1,p1.alphabet());
    }
    @Test
    public void testSize() {
        Permutation p = getNewPermutation("(0)",
                getNewAlphabet("0"));
        assertEquals(1, p.size());

        Permutation p2 = getNewPermutation("", getNewAlphabet());
        assertEquals(26, p2.size());

        Permutation p3 = getNewPermutation("(1?)",
                getNewAlphabet("1?0R"));
        assertEquals(4, p3.size());

        Permutation p4 = getNewPermutation("", getNewAlphabet(""));
        assertEquals(0, p4.size());
    }

    @Test
    public void testPermuteInt() {
        Permutation p = getNewPermutation("(HIL) (FNGR) (!^)",
                getNewAlphabet("HILFNGR!^"));
        assertEquals(1, p.permute(0));
        assertEquals(7, p.permute(8));

        Permutation p2 = getNewPermutation( "(AB!)       (&)",
                getNewAlphabet("AB!&"));
        assertEquals(0, p2.permute(2));
        assertEquals(3, p2.permute(3));
    }

    @Test
    public void testInvertInt() {
        Permutation p = getNewPermutation("(HIL) (FNGR) (!^)",
                getNewAlphabet("HILFNGR!^"));
        assertEquals(0, p.invert(1));
        assertEquals(8, p.invert(7));

        Permutation p2 = getNewPermutation( "(AB!)       (&)",
                getNewAlphabet("AB!&"));
        assertEquals(2, p2.invert(0));
        assertEquals(3, p2.invert(3));
    }

    @Test
    public void testPermuteChar() {
        Permutation p = getNewPermutation("(BACD)",
                getNewAlphabet("ABCD"));
        assertEquals('C',p.permute('A'));
        assertEquals('D',p.permute('C'));
        assertEquals('B',p.permute('D'));

        Permutation p2 = getNewPermutation( "(AB!)       (&)",
                getNewAlphabet("AB!&"));
        assertEquals('A', p2.permute('!'));
        assertEquals('B', p2.permute('A'));
        assertEquals('!', p2.permute('B'));
        assertEquals('&', p2.permute('&'));

        Permutation p3 = getNewPermutation( "(AB) (0)       (X)",
                getNewAlphabet("ABCX0"));
        assertEquals('A', p3.permute('B'));
        assertEquals('B', p3.permute('A'));
        assertEquals('C', p3.permute('C'));
        assertEquals('0', p3.permute('0'));

        Permutation p4 = getNewPermutation("",
                getNewAlphabet("A9CD"));
        assertEquals('9', p4.permute('9'));

    }

    @Test
    public void testInvertChar() {
        Permutation p = getNewPermutation("(BACD)",
                getNewAlphabet("ABCD"));
        assertEquals('B',p.invert('A'));
        assertEquals('D',p.invert('B'));
        assertEquals('A',p.invert('C'));
        assertEquals('C',p.invert('D'));

        Permutation p2 = getNewPermutation( "(AB!)       (&)",
                getNewAlphabet("AB!&"));
        assertEquals('B', p2.invert('!'));
        assertEquals('!', p2.invert('A'));
        assertEquals('A', p2.invert('B'));
        assertEquals('&', p2.invert('&'));

        Permutation p3 = getNewPermutation( "(AB) (0)       (X)",
                getNewAlphabet("ABCX0"));
        assertEquals('A', p3.invert('B'));
        assertEquals('B', p3.invert('A'));
        assertEquals('C', p3.invert('C'));
        assertEquals('0', p3.invert('0'));

        Permutation p4 = getNewPermutation("",
                getNewAlphabet("A9CD"));
        assertEquals('9', p4.invert('9'));
    }

    @Test
    public void testDerangement() {
        Permutation p = getNewPermutation("(BA)     (CD)",
                getNewAlphabet("ABCD"));
        assertEquals(true, p.derangement());
        Permutation p2 = getNewPermutation("(BA)(CD)",
                getNewAlphabet("ABCD"));
        assertEquals(true, p2.derangement());

        Permutation p3 = getNewPermutation("(!&C)",
                getNewAlphabet("!&CD"));
        assertEquals(false, p3.derangement());
        Permutation p4 = getNewPermutation( "",
                getNewAlphabet("ABCX0"));
        assertEquals(false, p4.derangement());
    }
//    @Test(expected = IllegalArgumentException.class)
//    public void testNoAlphabet() {
//        Permutation p = getNewPermutation("",
//                getNewAlphabet(""));
//        if (p.size()==0) {
//            throw new IllegalArgumentException("No alphabet.");
//        }
//
//    }
//    @Test(expected = EnigmaException.class)
//    public void testNotInAlphabet() {
//        Permutation p = getNewPermutation("(ABCD)",
//                getNewAlphabet("ABC"));
//
//    }
//
//    @Test(expected = EnigmaException.class)
//    public void testInAlphabetTwice() {
//        Permutation p = getNewPermutation("(ABD)",
//                getNewAlphabet("ABBD"));
//
//    }
//    @Test(expected = EnigmaException.class)
//    public void testInCyclesTwice() {
//        Permutation p = getNewPermutation("(ABBD)",
//                getNewAlphabet("ABD"));
//
//    }
//    @Test(expected = IllegalArgumentException.class)
//    public void testInvalidLetter() {
//        Alphabet a = getNewAlphabet("A*");
//        Alphabet a1 = getNewAlphabet("A(");
//        Alphabet a2 = getNewAlphabet("A)");
//        if (a.contains('*') || a1.contains('(') || a2.contains(')')) {
//            throw new IllegalArgumentException("Alphabet can't contain *, (, " +
//                    "or ) characters");
//        }
//    }
//    @Test(expected = EnigmaException.class)
//    public void testInvalidCycleForm() {
//        Permutation p = getNewPermutation("((A))",
//                getNewAlphabet("AB"));
//    }



}
