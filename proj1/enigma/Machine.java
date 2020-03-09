package enigma;

import java.util.HashMap;
import java.util.ArrayList;
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
        _allRotors = (ArrayList<Rotor>) allRotors;
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
        int S = 0; int P = 0; _rotorOrder = rotors;
        if (rotors.length != numRotors()) {
            throw error("Incorrect number of rotors was provided "
                    + "as input into insertRotors");
        }
        for (int i = 0; i < rotors.length; i += 1) {
            String name = rotors[i];
            _allRotorIterator = _allRotors.listIterator();
            int index = _allRotors.size();
            while (_allRotorIterator.hasNext()) {
                Rotor curr = _allRotorIterator.next(); index -= 1;
                if (!(curr.name().compareTo(name) == 0) && index == 0) {
                    for (int j = 0; j < _allRotors.size(); j += 1) {
                        System.out.println(_allRotors.get(j).name());
                    }
                    throw error("Rotor not found in allRotors.");
                }
                if (curr.name().compareTo(name) == 0) {
                    if (i == 0) {
                        if (!curr.reflecting()) {
                            throw error("First rotor != reflector.");
                        }
                        _selectedRotors.put(name, curr); S += 1; break;
                    } else if (i == numRotors() - 1) {
                        if (!curr.rotates()) {
                            throw error("S rotor != moving.");
                        }
                        _selectedRotors.put(name, curr); S += 1; P += 1; break;
                    } else {
                        if (i+1 <= _numRotors - _pawls) {
                            if (curr.rotates() || curr.reflecting()) {
                                throw error("There was a reflector "
                                        + " or moving rotor where it should "
                                        + "be fixed.");
                            }
                            _selectedRotors.put(name,curr); S += 1; break;
                        }
                        if (!curr.rotates()) {
                            throw error("There was a non-moving"
                                    + "rotor where it should be moving rotor.");
                        }
                        _selectedRotors.put(name,curr);
                        S += 1; P += 1; break;
                    }
                }
            }
        }
        if (P != _pawls || S != _numRotors) {
            throw error("You put in the wrong number of moving"
                    + "rotors as specified by the configuration, or something"
                    + "went wrong with inserting rotors.");
        }
    }

    /** Once a rotor has been selected as a part of a configuration return
     * it from the _selectedRotors hashmap by indexing it via its NAME. */
    Rotor returnSelectedRotor(String name) {
        return _selectedRotors.get(name);
    }

    /** Once a set of rotors has been selected as a part of a configuration
     * return their current settings as a int[]. */
    int[] returnSelectedRotorSettings() {
        int[] settings = new int[_rotorOrder.length];
        for (int i = 0; i < _rotorOrder.length; i += 1) {
            Rotor r = _selectedRotors.get(_rotorOrder[i]);
            int rs = r.setting();
            settings[i] = rs;
        }
        return settings;
    }

    /** Set my rotors according to SETTING, which must be a string of
     *  numRotors()-1 characters in my alphabet. The first letter refers
     *  to the leftmost rotor setting (not counting the reflector).  */
    void setRotors(String setting) {
        if (setting.length() != numRotors() - 1) {
            throw error("There were either too many or not"
                    + "enough letters to set the needed number of rotors.");
        }
        for (int j = 0, i = 1; j < setting.length(); j += 1, i += 1) {
            String cRNAME = _rotorOrder[i];
            Character pLetter = setting.charAt(j);
            if (!_alphabet.contains(pLetter)) {
                throw error("There was a letter not in the" +
                        "configured alphabet in the settings string.");
            }
            _selectedRotors.get(cRNAME).set(pLetter);
        }
    }

    /** Set the plugboard to PLUGBOARD. */
    void setPlugboard(Permutation plugboard) {
        _plugboard = plugboard;
    }

    /** Returns the result of converting the input character C (as an
     *  index in the range 0..alphabet size - 1), after first advancing

     *  the machine. */
    int convert(int c) {
        boolean[] willRotate = new boolean[_numRotors];
        willRotate[_numRotors - 1] = true;
        for (int i = _numRotors - 1; i > (_numRotors - _pawls); i -= 1) {
            Rotor r = _selectedRotors.get(_rotorOrder[i]);
            if (r.atNotch()) {
                willRotate[i] = true;
                willRotate[i-1] = true;
            }
        }
        for (int i = 1; i < _rotorOrder.length; i += 1) {
            if (willRotate[i]) {
                _selectedRotors.get(_rotorOrder[i]).advance();
            }
        }
        if (_plugboard != null) { c = _plugboard.permute(c); }

        for (int i = _rotorOrder.length; i > 0; i -= 1) {
            c = _selectedRotors.get(_rotorOrder[i-1]).convertForward(c);
        }
        for (int i = 1; i < _rotorOrder.length; i += 1) {
            c = _selectedRotors.get(_rotorOrder[i]).convertBackward(c);
        }
        if (_plugboard != null) { c = _plugboard.permute(c); }
        return c;
    }


    /** Returns the encoding/decoding of MSG, updating the state of
     *  the rotors accordingly. */
    String convert(String msg) {
        ArrayList<Character> converted = new ArrayList<Character>();
        String[] message = msg.split(" ");
        StringBuilder msgOUTPUT = new StringBuilder();
        for (String word : message) {
            for (int i = 0; i < word.length(); i +=1) {
                int k = this._alphabet.toInt(word.charAt(i));
                int cINT = this.convert(k);
                Character c = this._alphabet.toChar(cINT);
                converted.add(c);
            }
        }

        for (int i = 0; i < converted.size(); i += 1) {
            if (i % 5 == 0 && i != 0) {
                msgOUTPUT.append(" ");
            }
            msgOUTPUT.append(converted.get(i));
        }
        return msgOUTPUT.toString();
    }

    /** Common alphabet of my rotors. */
    private final Alphabet _alphabet;
    private int _numRotors;
    private int _pawls;
    private  ArrayList<Rotor> _allRotors;
    private HashMap<String,Rotor> _selectedRotors;
    private ListIterator<Rotor> _allRotorIterator;
    private String[] _rotorOrder;
    private Permutation _plugboard;
}
