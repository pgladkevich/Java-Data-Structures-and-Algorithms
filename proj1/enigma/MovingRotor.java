package enigma;

import java.util.ArrayList;

import static enigma.EnigmaException.*;

/** Class that represents a rotating rotor in the enigma machine.
 *  @author Pavel Gladkevich
 */
class MovingRotor extends Rotor {

    /** A rotor named NAME whose permutation in its default setting is
     *  PERM, and whose notches are at the positions indicated in NOTCHES.
     *  The Rotor is initally in its 0 setting (first character of its
     *  alphabet).
     */
    MovingRotor(String name, Permutation perm, String notches) {
        super(name, perm);
        if (notches != null) {
            _notches = new ArrayList<Character>(notches.length());
            for (int i = 0; i < notches.length(); i += 1) {
                _notches.add(notches.charAt(i));
            }
        }
    }

    @Override
    boolean rotates() {
        return true;
    }

    @Override
    void advance() {
        this.set(this.permutation().wrap(this.setting() + 1));
    }

    @Override
    boolean atNotch() {
        char cposn = this.alphabet().toChar(this.setting());
        return _notches.contains(cposn);
    }

    /** The notches of the rotor. */
    private ArrayList<Character> _notches;

}
