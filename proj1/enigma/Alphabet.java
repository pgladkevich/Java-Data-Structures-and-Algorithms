package enigma;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/** An alphabet of encodable characters.  Provides a mapping from characters
 *  to and from indices into the alphabet.
 *  @author Pavel Gladkevich
 */
class Alphabet {
    /** A hashmap of all of the characters. */
    private HashMap<Integer, Character> _hm;
    /** A new alphabet containing CHARS.  Character number #k has index
     *  K (numbering from 0). No character may be duplicated. */
    Alphabet(String chars) {
        _hm = new HashMap<Integer, Character>();
        for (int i = 0; i < chars.length(); i += 1) {
            _hm.put(i, chars.charAt(i));
        }
    }
    /** Return the HashMap of ints and chars.
     * @return*/
    HashMap<Integer, Character> returnHM() {
        return this._hm;
    }

    /** A default alphabet of all upper-case characters. */
    Alphabet() {
        this("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
    }

    /** Returns the size of the alphabet. */
    int size() {
        return _hm.size();
    }

    /** Returns true if CH is in this alphabet. */
    boolean contains(char ch) {
        return _hm.containsValue(ch);
    }

    /** Returns character number INDEX in the alphabet, where
     *  0 <= INDEX < size(). */
    char toChar(int index) {
        return _hm.get(index);
    }

    /** Returns the index of character CH which must be in
     *  the alphabet. This is the inverse of toChar(). */
    int toInt(char ch) {
        for (Map.Entry entry : _hm.entrySet()) {
            if (Objects.equals(ch, entry.getValue())) {
                return (int) entry.getKey();
            }
        }
        return -1;
    }
}
