package enigma;

import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.Timeout;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import static enigma.TestUtils.*;

public class MachineTest {
    /** Testing time limit. */
    @Rule
    public Timeout globalTimeout = Timeout.seconds(5);

    /* ***** TESTING UTILITIES ***** */
    private String alpha = UPPER_STRING;
    private MovingRotor I;
    private MovingRotor II;
    private MovingRotor III;
    private MovingRotor IV;
    private MovingRotor V;
    private MovingRotor VI;
    private MovingRotor VII;
    private MovingRotor VIII;
    private FixedRotor Beta;
    private FixedRotor Gamma;
    private Reflector B;
    private Reflector C;
    private ArrayList<Rotor> navalRotors;
    private ArrayList<Rotor> navalRotorsB;

    /** Set the rotor to the one with given NAME and permutation as
     *  specified by the NAME entry in ROTORS, with given NOTCHES. */
    private MovingRotor returnMovingRotor(String name, HashMap<String, String> rotors,
                          String notches) {
        return new MovingRotor(name, new Permutation(rotors.get(name), UPPER),
                notches);
    }

    /** Set a basic rotor with the given NAME entry in ROTORS */
    private Rotor returnBasicRotor(String name, HashMap<String, String> rotors) {
        return new Rotor(name, new Permutation(rotors.get(name), UPPER));
    }
    /** Set a reflector rotor with the given NAME entry in ROTORS */
    private Reflector returnReflector(String name, HashMap<String, String> rotors) {
        return new Reflector(name, new Permutation(rotors.get(name), UPPER));
    }
    /** Set a Fixed rotor with the given NAME entry in ROTORS */
    private FixedRotor returnFixedRotor(String name, HashMap<String, String> rotors) {
        return new FixedRotor(name, new Permutation(rotors.get(name), UPPER));
    }

    private void CreateNavalA() {
        I = returnMovingRotor("I", NAVALA, "Q");
        II = returnMovingRotor("II", NAVALA, "E");
        III = returnMovingRotor("III", NAVALA, "V");
        IV = returnMovingRotor("IV", NAVALA, "J");
        V = returnMovingRotor("V", NAVALA, "Z");
        VI = returnMovingRotor("VI", NAVALA, "ZM");
        VII = returnMovingRotor("VII", NAVALA, "ZM");
        VIII = returnMovingRotor("VIII", NAVALA, "ZM");
        Beta = returnFixedRotor("Beta", NAVALA);
        Gamma = returnFixedRotor("Gamma", NAVALA);
        B = returnReflector("B", NAVALA);
        C = returnReflector("C", NAVALA);

        navalRotors = new ArrayList<Rotor>();
        navalRotors.add(I);
        navalRotors.add(II);
        navalRotors.add(III);
        navalRotors.add(IV);
        navalRotors.add(V);
        navalRotors.add(VI);
        navalRotors.add(VII);
        navalRotors.add(VIII);
        navalRotors.add(Beta);
        navalRotors.add(Gamma);
        navalRotors.add(B);
        navalRotors.add(C);
    }

    private void CreateNavalB() {

        I = returnMovingRotor("I", NAVALB, "Q");
        II = returnMovingRotor("II", NAVALB, "E");
        III = returnMovingRotor("III", NAVALB, "V");
        IV = returnMovingRotor("IV", NAVALB, "J");
        V = returnMovingRotor("V", NAVALB, "Z");
        VI = returnMovingRotor("VI", NAVALB, "ZM");
        VII = returnMovingRotor("VII", NAVALB, "ZM");
        VIII = returnMovingRotor("VIII", NAVALB, "ZM");
        Beta = returnFixedRotor("Beta", NAVALB);
        Gamma = returnFixedRotor("Gamma", NAVALB);
        B = returnReflector("B", NAVALB);
        C = returnReflector("C", NAVALB);

        navalRotorsB = new ArrayList<Rotor>();
        navalRotorsB.add(I);
        navalRotorsB.add(II);
        navalRotorsB.add(III);
        navalRotorsB.add(IV);
        navalRotorsB.add(V);
        navalRotorsB.add(VI);
        navalRotorsB.add(VII);
        navalRotorsB.add(VIII);
        navalRotorsB.add(Beta);
        navalRotorsB.add(Gamma);
        navalRotorsB.add(B);
        navalRotorsB.add(C);
    }

    /* ***** TESTS ***** */
    public void main(String[] Args){
//        HashMap<String,Rotor> navalRotors = new HashMap<String,Rotor>();
//        navalRotors.put("I", I);
//        navalRotors.put("II", II);
//        navalRotors.put("III", III);
//        navalRotors.put("IV", IV);
//        navalRotors.put("V", V);
//        navalRotors.put("VI", VI);
//        navalRotors.put("VII", VII);
//        navalRotors.put("Beta", Beta);
//        navalRotors.put("Gamma", Gamma);
//        navalRotors.put("B", B);
//        navalRotors.put("C", C);
    }

    @Test
    public void checkNumRotorsAndPawls() {
        Machine m = new Machine(UPPER,5,3,
                navalRotors);
        assertEquals(5,m.numRotors());
        assertEquals(3,m.numPawls());
    }

    @Test
    public void checkInsertRotors() {
        CreateNavalA();
        Machine m = new Machine(UPPER,5,3,
                navalRotors);
        assertEquals(null,m.returnSelectedRotor("I"));
        String[] trivial = new String[] {"B", "Beta", "I", "II", "III"};
        m.insertRotors(trivial);
        assertEquals(5,m.numRotors());
        assertEquals(3,m.numPawls());
        Rotor testI = m.returnSelectedRotor("I");
        assertEquals(true, testI.rotates());
        assertEquals(10,testI.convertForward(1));
        assertEquals(9,testI.convertForward(25));
        assertEquals(10,testI.convertForward(27));
        assertEquals(9,testI.convertForward(51));
    }

    @Test(expected = EnigmaException.class)
    public void checkTooManyRotorsInsert() {
        CreateNavalA();
        Machine m = new Machine(UPPER,5,3,
                navalRotors);
        String[] trivial = new String[] {"B", "C", "Beta", "I", "II", "III"};
        m.insertRotors(trivial);

    }
    @Test(expected = EnigmaException.class)
    public void checkTooFewRotorsInsert() {
        CreateNavalA();
        Machine m = new Machine(UPPER,5,3,
                navalRotors);
        String[] trivial = new String[] {"Beta", "I", "II", "III"};
        m.insertRotors(trivial);
    }
    @Test(expected = EnigmaException.class)
    public void checkFirstNotReflector() {
        CreateNavalA();
        Machine m = new Machine(UPPER,5,3,
                navalRotors);
        String[] trivial = new String[] {"I", "Beta", "C", "II", "III"};
        m.insertRotors(trivial);
    }
    @Test(expected = EnigmaException.class)
    public void checkRotorNotInMachine() {
        CreateNavalA();
        Machine m = new Machine(UPPER,5,3,
                navalRotors);
        String[] trivial = new String[] {"B", "Beta", "D", "II", "III"};
        m.insertRotors(trivial);
    }
    @Test(expected = EnigmaException.class)
    public void checkNonMovingRotorRightmost() {
        CreateNavalA();
        Machine m = new Machine(UPPER,5,3,
                navalRotors);
        String[] trivial = new String[] {"B", "Beta", "I", "II", "Gamma"};
        m.insertRotors(trivial);
    }
    @Test(expected = EnigmaException.class)
    public void checkMovingRotorWrongSpot() {
        CreateNavalA();
        Machine m = new Machine(UPPER,5,3,
                navalRotors);
        String[] trivial = new String[] {"B", "I", "Beta", "II", "III"};
        m.insertRotors(trivial);
    }
    @Test(expected = EnigmaException.class)
    public void checkReflectorWrongSpot() {
        CreateNavalA();
        Machine m = new Machine(UPPER,5,3,
                navalRotors);
        String[] trivial = new String[] {"B", "Beta", "C", "II", "III"};
        m.insertRotors(trivial);
    }

    @Test
    public void checkSetRotors() {
        CreateNavalA();
        CreateNavalB();
        Machine m = new Machine(UPPER,5,3,
                navalRotors);
        String[] trivial = new String[] {"B", "Beta", "I", "II", "III"};
        m.insertRotors(trivial);
        m.setRotors("BBBB");
        Rotor testB = m.returnSelectedRotor("B");
        assertEquals(0, testB.setting());
        Rotor testI = m.returnSelectedRotor("I");
        assertEquals(1, testI.setting());
        // JLEKFCPUYMSNVXGWTROZHAQBID
        assertEquals(9,testI.convertForward(0));
        assertEquals(8,testI.convertForward(24));
        assertEquals(3,testI.convertForward(-1));
        assertEquals(8,testI.convertForward(50));

    }

    // There will always be one moving rotor right, the rightmost one? Else, we should throw an error? --> YES
    // Fixed rotors don't have pawls. You can have only fixed rotors and no moving rotors and therefore you'd have no pawls --> Contradicts previous statement?
    // For insertRotors, except for making sure that rotor[0] is a reflector, should we also check whether rotor[numRotors - pawls] to rotor[numRotors - 1] are all moving rotors?
    // Akshit Annadi 2 days ago Yes, you should also check that the remaining rotors are fixed too,
}
