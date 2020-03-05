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
    private Collection rotorCollection;

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

    /* ***** TESTS ***** */
    public void main(String[] Args){
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
        Machine m = new Machine(UPPER,5,3,
                navalRotors);
        assertEquals(null,m.returnSelectedRotor("I"));
        String[] trivial = new String[] {"B", "Beta", "I", "II", "III"};
        m.insertRotors(trivial);
        assertEquals(5,m.numRotors());
        assertEquals(3,m.numPawls());
        Rotor testI = m.returnSelectedRotor("I");
        assertEquals(true, testI.rotates());
        // "EKMFLGDQVZNTOWYHXUSPAIBRCJ"
        assertEquals(10,testI.convertForward(1));
    }

    // There will always be one moving rotor right, the rightmost one? Else, we should throw an error? --> YES
    // Fixed rotors don't have pawls. You can have only fixed rotors and no moving rotors and therefore you'd have no pawls.
    // For insertRotors, except for making sure that rotor[0] is a reflector, should we also check whether rotor[numRotors - pawls] to rotor[numRotors - 1] are all moving rotors?
    // Akshit Annadi 2 days ago Yes, you should also check that the remaining rotors are fixed too,
}
