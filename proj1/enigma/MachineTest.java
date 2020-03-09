package enigma;

import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.Timeout;

import static enigma.TestUtils.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;

public class MachineTest {
    /** Testing time limit. */
    @Rule
    public Timeout globalTimeout = Timeout.seconds(5);

    /* ***** TESTING UTILITIES ***** */
    private String alpha = UPPER_STRING;
    private MovingRotor _I;
    private MovingRotor _II;
    private MovingRotor _III;
    private MovingRotor _IV;
    private MovingRotor _V;
    private MovingRotor _VI;
    private MovingRotor _VII;
    private MovingRotor _VIII;
    private FixedRotor _Beta;
    private FixedRotor _Gamma;
    private Reflector _B;
    private Reflector _C;
    private ArrayList<Rotor> navalRotors;
    private ArrayList<Rotor> navalRotorsB;

    /** Set the rotor to the one with given NAME and permutation as
     *  specified by the NAME entry in ROTORS, with given NOTCHES. */
    private MovingRotor returnMovingRotor(String name, HashMap<String,
            String> rotors, String notches) {
        return new MovingRotor(name, new Permutation(rotors.get(name), UPPER),
                notches);
    }

    /** Set a basic rotor with the given NAME entry in ROTORS */
    private Rotor returnBasicRotor(String name, HashMap<String,
            String> rotors) {
        return new Rotor(name, new Permutation(rotors.get(name), UPPER));
    }
    /** Set a reflector rotor with the given NAME entry in ROTORS */
    private Reflector returnReflector(String name, HashMap<String,
            String> rotors) {
        return new Reflector(name, new Permutation(rotors.get(name), UPPER));
    }
    /** Set a Fixed rotor with the given NAME entry in ROTORS */
    private FixedRotor returnFixedRotor(String name, HashMap<String,
            String> rotors) {
        return new FixedRotor(name, new Permutation(rotors.get(name), UPPER));
    }

    private void createNavalA() {
        _I = returnMovingRotor("I", NAVALA, "Q");
        _II = returnMovingRotor("II", NAVALA, "E");
        _III = returnMovingRotor("III", NAVALA, "V");
        _IV = returnMovingRotor("IV", NAVALA, "J");
        _V = returnMovingRotor("V", NAVALA, "Z");
        _VI = returnMovingRotor("VI", NAVALA, "ZM");
        _VII = returnMovingRotor("VII", NAVALA, "ZM");
        _VIII = returnMovingRotor("VIII", NAVALA, "ZM");
        _Beta = returnFixedRotor("Beta", NAVALA);
        _Gamma = returnFixedRotor("Gamma", NAVALA);
        _B = returnReflector("B", NAVALA);
        _C = returnReflector("C", NAVALA);

        navalRotors = new ArrayList<Rotor>();
        navalRotors.add(_I);
        navalRotors.add(_II);
        navalRotors.add(_III);
        navalRotors.add(_IV);
        navalRotors.add(_V);
        navalRotors.add(_VI);
        navalRotors.add(_VII);
        navalRotors.add(_VIII);
        navalRotors.add(_Beta);
        navalRotors.add(_Gamma);
        navalRotors.add(_B);
        navalRotors.add(_C);
    }

    private void createNavalB() {

        _I = returnMovingRotor("I", NAVALB, "Q");
        _II = returnMovingRotor("II", NAVALB, "E");
        _III = returnMovingRotor("III", NAVALB, "V");
        _IV = returnMovingRotor("IV", NAVALB, "J");
        _V = returnMovingRotor("V", NAVALB, "Z");
        _VI = returnMovingRotor("VI", NAVALB, "ZM");
        _VII = returnMovingRotor("VII", NAVALB, "ZM");
        _VIII = returnMovingRotor("VIII", NAVALB, "ZM");
        _Beta = returnFixedRotor("Beta", NAVALB);
        _Gamma = returnFixedRotor("Gamma", NAVALB);
        _B = returnReflector("B", NAVALB);
        _C = returnReflector("C", NAVALB);

        navalRotorsB = new ArrayList<Rotor>();
        navalRotorsB.add(_I);
        navalRotorsB.add(_II);
        navalRotorsB.add(_III);
        navalRotorsB.add(_IV);
        navalRotorsB.add(_V);
        navalRotorsB.add(_VI);
        navalRotorsB.add(_VII);
        navalRotorsB.add(_VIII);
        navalRotorsB.add(_Beta);
        navalRotorsB.add(_Gamma);
        navalRotorsB.add(_B);
        navalRotorsB.add(_C);
    }

    /* ***** TESTS ***** */

    @Test
    public void checkNumRotorsAndPawls() {
        Machine m = new Machine(UPPER, 5, 3, navalRotors);
        assertEquals(5, m.numRotors());
        assertEquals(3, m.numPawls());
    }

    @Test
    public void checkInsertRotors() {
        createNavalA();
        Machine m = new Machine(UPPER, 5, 3, navalRotors);
        assertEquals(null, m.returnSelectedRotor("I"));
        String[] trivial = new String[] {"B", "Beta", "I", "II", "III"};
        m.insertRotors(trivial);
        assertEquals(5, m.numRotors());
        assertEquals(3, m.numPawls());
        Rotor testI = m.returnSelectedRotor("I");
        assertEquals(true, testI.rotates());
        assertEquals(10, testI.convertForward(1));
        assertEquals(9, testI.convertForward(25));
        assertEquals(10, testI.convertForward(27));
        assertEquals(9, testI.convertForward(51));
    }

    @Test(expected = EnigmaException.class)
    public void checkTooManyRotorsInsert() {
        createNavalA();
        Machine m = new Machine(UPPER, 5, 3, navalRotors);
        String[] trivial = new String[] {"B", "C", "Beta", "I", "II", "III"};
        m.insertRotors(trivial);

    }
    @Test(expected = EnigmaException.class)
    public void checkTooFewRotorsInsert() {
        createNavalA();
        Machine m = new Machine(UPPER, 5, 3, navalRotors);
        String[] trivial = new String[] {"Beta", "I", "II", "III"};
        m.insertRotors(trivial);
    }
    @Test(expected = EnigmaException.class)
    public void checkFirstNotReflector() {
        createNavalA();
        Machine m = new Machine(UPPER, 5, 3, navalRotors);
        String[] trivial = new String[] {"I", "Beta", "C", "II", "III"};
        m.insertRotors(trivial);
    }
    @Test(expected = EnigmaException.class)
    public void checkRotorNotInMachine() {
        createNavalA();
        Machine m = new Machine(UPPER, 5, 3, navalRotors);
        String[] trivial = new String[] {"B", "Beta", "D", "II", "III"};
        m.insertRotors(trivial);
    }
    @Test(expected = EnigmaException.class)
    public void checkNonMovingRotorRightmost() {
        createNavalA();
        Machine m = new Machine(UPPER, 5, 3, navalRotors);
        String[] trivial = new String[] {"B", "Beta", "I", "II", "Gamma"};
        m.insertRotors(trivial);
    }
    @Test(expected = EnigmaException.class)
    public void checkMovingRotorWrongSpot() {
        createNavalA();
        Machine m = new Machine(UPPER, 5, 3, navalRotors);
        String[] trivial = new String[] {"B", "I", "Beta", "II", "III"};
        m.insertRotors(trivial);
    }
    @Test(expected = EnigmaException.class)
    public void checkReflectorWrongSpot() {
        createNavalA();
        Machine m = new Machine(UPPER, 5, 3, navalRotors);
        String[] trivial = new String[] {"B", "Beta", "C", "II", "III"};
        m.insertRotors(trivial);
    }

    @Test
    public void checkSetRotors() {
        createNavalA();
        createNavalB();
        Machine m = new Machine(UPPER, 5, 3, navalRotors);
        String[] trivial = new String[] {"B", "Beta", "I", "II", "III"};
        m.insertRotors(trivial);
        m.setRotors("BBBB");
        Rotor testB = m.returnSelectedRotor("B");
        assertEquals(0, testB.setting());
        Rotor testI = m.returnSelectedRotor("I");
        assertEquals(1, testI.setting());
        assertEquals(9, testI.convertForward(0));
        assertEquals(8, testI.convertForward(24));
        assertEquals(3, testI.convertForward(-1));
        assertEquals(8, testI.convertForward(50));
    }

    @Test
    public void checkSetPlugBoard() {
        createNavalA();
        Machine m = new Machine(UPPER, 5, 3, navalRotors);
        String[] trivial = new String[] {"B", "Beta", "I", "II", "III"};
        m.insertRotors(trivial);
        Alphabet a = new Alphabet();
        Permutation p = new Permutation("(AB) (CD) (EF)", a);
        m.setPlugboard(p);
    }

    @Test
    public void checkConvert() {
        createNavalA();
        Machine m = new Machine(UPPER, 5, 3, navalRotors);
        String[] trivial = new String[] {"B", "Beta", "III", "IV", "I"};
        m.insertRotors(trivial);
        m.setRotors("AXLE");
        m.setPlugboard(new Permutation("(YF) (ZH)", new Alphabet()));
        assertEquals(4, m.returnSelectedRotorSettings()[4]);
        assertEquals(25, m.convert(24));
        assertEquals(5, m.returnSelectedRotorSettings()[4]);

        int[] size11PLUSONESO12 = new int[11];
        for (int i : size11PLUSONESO12) {
            m.convert(0);
        }
        int[] a = new int[] {0, 0, 23, 11, 16};
        for (int i = 0; i < a.length; i += 1) {
            assertEquals(a[i], m.returnSelectedRotorSettings()[i]);
        }
        m.convert(0);
        assertEquals(12, m.returnSelectedRotorSettings()[3]);
        assertEquals(17, m.returnSelectedRotorSettings()[4]);

        int[] size597 = new int[597];
        for (int i : size597) {
            m.convert(0);
        }
        int[] a1 = new int[] {0, 0, 23, 8, 16};
        for (int i = 0; i < a.length; i += 1) {
            assertEquals(a1[i], m.returnSelectedRotorSettings()[i]);
        }

        m.convert(0);
        m.convert(1);
        int[] a2 = new int[] {0, 0, 24, 10, 18};
        for (int i = 0; i < a.length; i += 1) {
            assertEquals(a2[i], m.returnSelectedRotorSettings()[i]);
        }
        m.setRotors("AVKS");
        m.convert(0);
        int[] a3 = new int[] {0, 0, 21, 10, 19};
        for (int i = 0; i < a.length; i += 1) {
            assertEquals(a3[i], m.returnSelectedRotorSettings()[i]);
        }
    }

    @Test
    public void checkConvertMessageTrivial() {
        createNavalA();
        Machine m = new Machine(UPPER, 5, 3, navalRotors);
        String[] trivial = new String[]{"B", "Beta", "I", "II", "III" };
        m.insertRotors(trivial);
        m.setRotors("AAAA");
        assertEquals("ILBDA AMTAZ", m.convert("HELLO WORLD"));
        m.setRotors("AAAA");
        assertEquals("HELLO WORLD", m.convert("ILBDA AMTAZ"));
    }

    @Test
    public void checkConvertMessageWithTail() {
        createNavalA();
        Machine m = new Machine(UPPER, 5, 3, navalRotors);
        String[] trivial = new String[]{"B", "Beta", "III", "IV", "I" };
        m.insertRotors(trivial);
        m.setRotors("AXLE");
        m.setPlugboard(new Permutation("(HQ) (EX) (IP) (TR) (BY)",
                new Alphabet()));
        assertEquals("QVPQS OKOIL PUBKJ ZPISF XDW",
                m.convert("FROM HIS SHOULDER HIAWATHA"));
    }

    @Test
    public void checkSwapRotors() {
        createNavalA();
        Machine m = new Machine(UPPER, 5, 3, navalRotors);
        String[] trivial = new String[]{"B", "Beta", "III", "IV", "I" };
        m.insertRotors(trivial);
        m.setRotors("AXLE");
        m.setPlugboard(new Permutation("(HQ) (EX) (IP) (TR) (BY)",
                new Alphabet()));
        String[] basic = new String[]{"B", "Beta", "I", "II", "III" };
        m.insertRotors(basic);
        m.setRotors("AAAA");
        m.setPlugboard(null);
        assertEquals("HELLO WORLD", m.convert("ILBDA AMTAZ"));
    }
}
