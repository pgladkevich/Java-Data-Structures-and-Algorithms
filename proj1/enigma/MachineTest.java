package enigma;

import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.Timeout;
import static org.junit.Assert.*;

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

    @Test
    public void checkNumRotorsAndPawls() {
        I = returnMovingRotor("I", NAVALA, "Q");
        II = returnMovingRotor("II", NAVALA, "E");
        III = returnMovingRotor("III", NAVALA, "V");
        IV = returnMovingRotor("IV", NAVALA, "J");
        V = returnMovingRotor("V", NAVALA, "Z");
        VI = returnMovingRotor("VI", NAVALA, "ZM");
        VII = returnMovingRotor("VII", NAVALA, "ZM");
        VIII = returnMovingRotor("VIII", NAVALA, "ZM");
        Beta = returnFixedRotor("I", NAVALA);
        Gamma = returnFixedRotor("I", NAVALA);
        B = returnReflector("I", NAVALA);
        C = returnReflector("I", NAVALA);
         new HashMap<String,Rotor>();



        Machine m = new Machine(UPPER,5,3,);
    }

}
