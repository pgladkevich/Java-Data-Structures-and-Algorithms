package enigma;

import static enigma.EnigmaException.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/** Represents a permutation of a range of integers starting at 0 corresponding
 *  to the characters of an alphabet.
 *  @author Pavel Gladkevich
 */
class Permutation {

    /** Set this Permutation to that specified by CYCLES, a string in the
     *  form "(cccc) (cc) ..." where the c's are characters in ALPHABET, which
     *  is interpreted as a permutation in cycle notation.  Characters in the
     *  alphabet that are not included in any cycle map to themselves.
     *  Whitespace is ignored. */
    Permutation(String cycles, Alphabet alphabet) {
        _alphabet = alphabet;
        cycles = cycles.replace(")(", ") (").replace(
                "(", "").replace(")",
                "").trim().replaceAll(" +", " ");
        _cycles = cycles.split(" ");
        _pHM = new HashMap<Integer, Integer>();
        for (String e : _cycles) {
            for (int i = 0; i < e.length(); i += 1) {
                if (!alphabet.contains(e.charAt(i))) {
                    throw error("Character in cycle not found"
                            + "in  the alphabet.");
                }
            }
            addCycle(e);
        }
        for (Map.Entry entry : alphabet._hm.entrySet()) {
            if (!_pHM.containsKey(entry.getKey())) {
                _pHM.put((int) entry.getKey(), (int) entry.getKey());
            }
        }
    }

    /** Add the cycle c0->c1->...->cm->c0 to the permutation, where CYCLE is
     *  c0c1...cm. */
    private void addCycle(String cycle) {
        for (int i = 0; i < cycle.length(); i += 1) {
            int k = this._alphabet.toInt(cycle.charAt(i));
            if (i == cycle.length() - 1) {
                int v = this._alphabet.toInt(cycle.charAt(0));
                _pHM.put(k, v);
            } else {
                _pHM.put(k, this._alphabet.toInt(cycle.charAt(i + 1)));
            }
        }
    }

    /** Return the value of P modulo the size of this permutation. */
    final int wrap(int p) {
        int r = p % size();
        if (r < 0) {
            r += size();
        }
        return r;
    }

    /** Returns the size of the alphabet I permute. */
    int size() {
        return _alphabet.size();
    }

    /** Return the result of applying this permutation to P modulo the
     *  alphabet size. */
    int permute(int p) {
        int pWrap = wrap(p);
        return _pHM.get(pWrap);
    }

    /** Return the result of applying the inverse of this permutation
     *  to  C modulo the alphabet size. */
    int invert(int c) {
        int cWrap = wrap(c);
        for (Map.Entry entry : _pHM.entrySet()) {
            if (Objects.equals(cWrap, entry.getValue())) {
                return (int) entry.getKey();
            }
        }
        return -1;
    }

    /** Return the result of applying this permutation to the index of P
     *  in ALPHABET, and converting the result to a character of ALPHABET. */
    char permute(char p) {
        int pWrap = wrap(_alphabet.toInt(p));
        int kP = this.permute(pWrap);
        return this._alphabet.toChar(kP);
    }

    /** Return the result of applying the inverse of this permutation to C. */
    char invert(char c) {
        int cWrap = wrap(_alphabet.toInt(c));
        int kC = this.invert(cWrap);
        return this._alphabet.toChar(kC);
    }

    /** Return the alphabet used to initialize this Permutation. */
    Alphabet alphabet() {
        return _alphabet;
    }

    /** Return true iff this permutation is a derangement (i.e., a
     *  permutation for which no value maps to itself). */
    boolean derangement() {
        for (Map.Entry entry : _pHM.entrySet()) {
            if (Objects.equals(entry.getKey(), entry.getValue())) {
                return false;
            }
        }
        return true;
    }

    /** Alphabet of this permutation. */
    private Alphabet _alphabet;
    /** Mapping of every letter to its converted letter. */
    private HashMap<Integer, Integer> _pHM;
    /** Cycles that are input as a part of the permutation. */
    private String[] _cycles;
}
