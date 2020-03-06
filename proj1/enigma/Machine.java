package enigma;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Collection;
import java.util.ListIterator;

import static enigma.EnigmaException.*;

/** Class that represents a complete enigma machine.
 *  @author Pavel Gladkevich
 */
class Machine {

    /** A new Enigma machine with alphabet ALPHA, 1 < NUMROTORS rotor slots,
     *  and 0 <= PAWLS < NUMROTORS pawls.  ALLROTORS contains all the
     *  available rotors. */
    Machine(Alphabet alpha, int numRotors, int pawls,
            Collection<Rotor> allRotors) {
        _alphabet = alpha;
        _numRotors = numRotors;
        _pawls = pawls;
        _allRotors = (ArrayList) allRotors;
        _selectedRotors = new HashMap<>();
    }

    /** Return the number of rotor slots I have. */
    int numRotors() {
        return _numRotors;
    }

    /** Return the number pawls (and thus rotating rotors) I have. */
    int numPawls() {
        return _pawls;
    }

    /** Set my rotor slots to the rotors named ROTORS from my set of
     *  available rotors (ROTORS[0] names the reflector).
     *  Initially, all rotors are set at their 0 setting. */
    void insertRotors(String[] rotors) {
        int S = 0;
        int P = 0;

        if (rotors.length != numRotors()) {
            throw error("Incorrect number of rotors was provided "
                    + "as input into insertRotors");
        }

        for (int i = 0; i < rotors.length; i += 1) {
            String name = rotors[0];
            _allRotorIterator = _allRotors.listIterator();
            int index = _allRotors.size();
            while (_allRotorIterator.hasNext()) {
                Rotor curr = _allRotorIterator.next();
                index -= 1;
                if (!curr.name().equals(name) && index == 0) {
                    throw error("The rotor name was not found in" +
                            "allRotors that was taken from the configuration" +
                            "file.");
                }
                if (curr.name().equals(name)) {
                    if (i == 0) {
                        if (!curr.reflecting()) {
                            throw error("The first rotor was not a "
                                    + "reflector, this is an incorrect input for"
                                    + "Insert Rotors");
                        }
                        _selectedRotors.put(name, curr);
                        S += 1; break;

                    }
                    else if (i == numRotors()) {
                        if (!curr.rotates()) {
                            throw error("The right most rotor was " +
                                    "not a moving rotor, this is an incorrect" +
                                    "input for Insert Rotors");
                        }
                        _selectedRotors.put(name, curr);
                        S += 1; P += 1; break;
                    }
                    else {
                        if (i+1 > 2 && i+1 <= numRotors() - numPawls() &&
                                (curr.rotates() || curr.rotates()) {

                        }
                        S += 1; P += 1;
                    }
                }
            }
        }
        if (P != numPawls() || S != numRotors()) {
            throw error("You put in the wrong number of moving" +
                    "rotors as specified by the configuration, or something" +
                    "went wrong with inserting rotors.");
        }
    }

    /** Once a rotor has been selected as a part of a configuration return
     * it from the _selectedRotors hashmap by indexing it via its NAME */
    Rotor returnSelectedRotor(String name) {
        return _selectedRotors.get(name);
    }

    /** Set my rotors according to SETTING, which must be a string of
     *  numRotors()-1 characters in my alphabet. The first letter refers
     *  to the leftmost rotor setting (not counting the reflector).  */
    void setRotors(String setting) {
        // FIXME
    }

    /** Set the plugboard to PLUGBOARD. */
    void setPlugboard(Permutation plugboard) {
        // FIXME
    }

    /** Returns the result of converting the input character C (as an
     *  index in the range 0..alphabet size - 1), after first advancing

     *  the machine. */
    int convert(int c) {
        return 0; // FIXME
    }

    /** Returns the encoding/decoding of MSG, updating the state of
     *  the rotors accordingly. */
    String convert(String msg) {
        return ""; // FIXME
    }

    /** Common alphabet of my rotors. */
    private final Alphabet _alphabet;
    private int _numRotors;
    private int _pawls;
    private  ArrayList<Rotor> _allRotors;
    private HashMap<String,Rotor> _selectedRotors;
    private ListIterator<Rotor> _allRotorIterator;
}
