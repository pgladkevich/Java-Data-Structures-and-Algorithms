package enigma;

import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.Timeout;
import static org.junit.Assert.*;

import static enigma.TestUtils.*;

/** The suite of all JUnit tests for the Permutation class.
 *  @author Pavel Gladkevich
 */
public class PermutationTest {

    /** Testing time limit. */
    @Rule
    public Timeout globalTimeout = Timeout.seconds(5);

    /* ***** TESTING UTILITIES ***** */

    private Permutation perm;
    private String alpha = UPPER_STRING;

    /** Check that perm has an alphabet whose size is that of
     *  FROMALPHA and TOALPHA and that maps each character of
     *  FROMALPHA to the corresponding character of FROMALPHA, and
     *  vice-versa. TESTID is used in error messages. */
    private void checkPerm(String testId,
                           String fromAlpha, String toAlpha) {
        int N = fromAlpha.length();
        assertEquals(testId + " (wrong length)", N, perm.size());
        for (int i = 0; i < N; i += 1) {
            char c = fromAlpha.charAt(i), e = toAlpha.charAt(i);
            assertEquals(msg(testId, "wrong translation of '%c'", c),
                         e, perm.permute(c));
            assertEquals(msg(testId, "wrong inverse of '%c'", e),
                         c, perm.invert(e));
            int ci = alpha.indexOf(c), ei = alpha.indexOf(e);
            assertEquals(msg(testId, "wrong translation of %d", ci),
                         ei, perm.permute(ci));
            assertEquals(msg(testId, "wrong inverse of %d", ei),
                         ci, perm.invert(ei));
        }
    }

    Permutation getNewPermutation(String cycles, Alphabet alphabet) {
        return new Permutation(cycles, alphabet);
    }

    Alphabet getNewAlphabet(String chars) {
        return new Alphabet(chars);
    }
    Alphabet getNewAlphabet() {
        return new Alphabet();
    }

    /* ***** TESTS ***** */

    @Test
    public void testAlphabet() {
        Alphabet a = getNewAlphabet();
        assertEquals(7,a.toInt('H'));
        assertEquals(8,a.toInt('I'));
        assertEquals(0,a.toInt('A'));
        assertEquals(25,a.toInt('Z'));
    }

    @Test
    public void checkIdTransform() {
        perm = new Permutation("", UPPER);
        checkPerm("identity", UPPER_STRING, UPPER_STRING);
    }

    @Test
    public void testGetAlphabet() {
        Alphabet a = getNewAlphabet();
        Permutation p = getNewPermutation("", a);
        assertEquals(a,p.alphabet());

        Alphabet a1 = getNewAlphabet("");
        Permutation p1 = getNewPermutation("",a1);
        assertEquals(a1,p1.alphabet());

        Alphabet a2 = getNewAlphabet("@#$%");
        Permutation p2 = getNewPermutation("",a2);
        assertEquals(a2,p2.alphabet());
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
        assertEquals(8, p.permute(-2));
        assertEquals(1, p.permute(9));

        Permutation p2 = getNewPermutation( "(AB!)       (&)",
                getNewAlphabet("AB!&"));
        assertEquals(0, p2.permute(2));
        assertEquals(3, p2.permute(3));
        assertEquals(3, p2.permute(-1));
        assertEquals(1, p2.permute(4));
    }

    @Test
    public void testInvertInt() {
        Permutation p = getNewPermutation("(HIL) (FNGR) (!^)",
                getNewAlphabet("HILFNGR!^"));
        assertEquals(0, p.invert(1));
        assertEquals(8, p.invert(7));
        assertEquals(7, p.invert(-1));
        assertEquals(2, p.invert(9));

        Permutation p2 = getNewPermutation( "(AB!)       (&)",
                getNewAlphabet("AB!&"));
        assertEquals(2, p2.invert(0));
        assertEquals(3, p2.invert(3));
        assertEquals(2, p2.invert(-4));
        assertEquals(1, p2.invert(6));
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
        Permutation p6 = getNewPermutation("(3D)(!C)",
                getNewAlphabet("!3CD"));
        assertEquals(true, p6.derangement());
        Permutation p3 = getNewPermutation("(!&C)",
                getNewAlphabet("!&CD"));
        assertEquals(false, p3.derangement());
        Permutation p4 = getNewPermutation( "",
                getNewAlphabet("ABCX0"));
        assertEquals(false, p4.derangement());
        Permutation p5 = getNewPermutation( "",
                getNewAlphabet(""));
        assertEquals(true, p5.derangement());
        Permutation p7 = getNewPermutation( "(X)",
                getNewAlphabet("ABCX0"));
        assertEquals(false, p7.derangement());
        Permutation p8 = getNewPermutation( "(@)",
                getNewAlphabet("@"));
        assertEquals(false, p8.derangement());
        Permutation p9 = getNewPermutation( "(@) (5) (7) (9)",
                getNewAlphabet("@"));
        assertEquals(false, p8.derangement());
    }

}
