package enigma;

import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.Timeout;
import static org.junit.Assert.*;

import java.util.HashMap;

import static enigma.TestUtils.*;

/** The suite of all JUnit tests for the Permutation class.
 *  @author Pavel Gladkevich
 */
public class MovingRotorTest {

    /** Testing time limit. */
    @Rule
    public Timeout globalTimeout = Timeout.seconds(5);

    /* ***** TESTING UTILITIES ***** */

    private Rotor rotor;
    private String alpha = UPPER_STRING;

    /** Check that rotor has an alphabet whose size is that of
     *  FROMALPHA and TOALPHA and that maps each character of
     *  FROMALPHA to the corresponding character of FROMALPHA, and
     *  vice-versa. TESTID is used in error messages. */
    private void checkRotor(String testId,
                            String fromAlpha, String toAlpha) {
        int N = fromAlpha.length();
        assertEquals(testId + " (wrong length)", N, rotor.size());
        for (int i = 0; i < N; i += 1) {
            char c = fromAlpha.charAt(i), e = toAlpha.charAt(i);
            int ci = alpha.indexOf(c), ei = alpha.indexOf(e);
            assertEquals(msg(testId, "wrong translation of %d (%c)", ci, c),
                         ei, rotor.convertForward(ci));
            assertEquals(msg(testId, "wrong inverse of %d (%c)", ei, e),
                         ci, rotor.convertBackward(ei));
        }
    }

    /** Set the rotor to the one with given NAME and permutation as
     *  specified by the NAME entry in ROTORS, with given NOTCHES. */
    private void setRotor(String name, HashMap<String, String> rotors,
                          String notches) {
        rotor = new MovingRotor(name, new Permutation(rotors.get(name), UPPER),
                                notches);
    }

    /** Set a basic rotor with the given NAME entry in ROTORS */
    private void setBasicRotor(String name, HashMap<String, String> rotors) {
        rotor = new Rotor(name, new Permutation(rotors.get(name), UPPER));
    }
    /** Set a reflector rotor with the given NAME entry in ROTORS */
    private void setReflectorRotor(String name,
                                   HashMap<String, String> rotors) {
        rotor = new Reflector(name, new Permutation(rotors.get(name), UPPER));
    }
    /** Set a Fixed rotor with the given NAME entry in ROTORS */
    private void setFixedRotor(String name, HashMap<String, String> rotors) {
        rotor = new FixedRotor(name, new Permutation(rotors.get(name), UPPER));
    }

    /* ***** TESTS ***** */
    @Test
    public void checkBasicRotor() {
        setBasicRotor("I", NAVALA);
        Alphabet a = rotor.alphabet();
        assertEquals(0, rotor.setting());
        assertEquals(26, rotor.size());
        assertEquals('E', a.toChar(rotor.convertForward(0)));

        rotor.set(1);
        assertEquals(1, rotor.setting());
        assertEquals('J', a.toChar(rotor.convertForward(0)));
        assertEquals('T', a.toChar(rotor.convertBackward(25)));

        rotor.set('C');
        assertEquals(2, rotor.setting());
    }

    @Test
    public void checkReflecting() {
        setReflectorRotor("B", NAVALA);
        assertEquals(true, rotor.reflecting());
        rotor.set(0);
    }

    @Test
    public void checkReflector() {
        setReflectorRotor("B", NAVALA);
        assertEquals(4, rotor.convertForward(0));
    }

    @Test(expected = EnigmaException.class)
    public void checkReflectorSet() {
        setReflectorRotor("B", NAVALA);
        rotor.set(1);
    }

    @Test(expected = EnigmaException.class)
    public void checkReflectorSuper() {
        rotor = new Reflector("R", new Permutation("(AE) (BN) "
                + "(CK) (DQ) (FU) (GY) (HW) (IJ) (LO) (MP) (RX) (SZ) (TV) (x)",
                new Alphabet("AEBNCKDQFUGYHWIJLOMPRXSZTVX")));
    }

    @Test
    public void checkFixedRotor() {
        setFixedRotor("Beta", NAVALA);
        checkRotor("Rotor Beta (A)", UPPER_STRING,
                NAVALA_MAP.get("Beta"));
        rotor.set(1);
        checkRotor("Rotor Beta (B)", UPPER_STRING,
                NAVALB_MAP.get("Beta"));
        rotor.set('Z');
        checkRotor("Rotor Beta (Z)", UPPER_STRING,
                NAVALZ_MAP.get("Beta"));
    }

    @Test
    public void checkRotorAtA() {
        setRotor("I", NAVALA, "");
        checkRotor("Rotor I (A)", UPPER_STRING, NAVALA_MAP.get("I"));
    }

    @Test
    public void checkRotorAdvance() {
        setRotor("I", NAVALA, "");
        rotor.advance();
        checkRotor("Rotor I advanced", UPPER_STRING, NAVALB_MAP.get("I"));
    }

    @Test
    public void checkRotorSet() {
        setRotor("I", NAVALA, "");
        rotor.set(25);
        checkRotor("Rotor I set", UPPER_STRING, NAVALZ_MAP.get("I"));
    }

    @Test
    public void checkMovingRing() {
        setRotor("III", NAVALA, "");
        rotor.set('A');
        assertEquals(19, rotor.convertForward(9));
        assertEquals(9, rotor.convertBackward(19));
        rotor.setRING('B');
        rotor.set('B');
        assertEquals(19, rotor.convertForward(9));
        assertEquals(9, rotor.convertBackward(19));

    }
}
